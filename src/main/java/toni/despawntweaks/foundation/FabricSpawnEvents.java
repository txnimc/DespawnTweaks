package toni.despawntweaks.foundation;

#if FABRIC

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.structure.Structure;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import toni.despawntweaks.DespawnTweaks;
import toni.despawntweaks.foundation.config.AllConfigs;

import java.util.HashSet;
import java.util.Set;

#if mc >= 211
import net.minecraft.world.level.chunk.status.ChunkStatus;
#else
import net.minecraft.world.level.chunk.ChunkStatus;
#endif

public class FabricSpawnEvents {
    public static MinecraftServer SERVER_INSTANCE;

    private static final Supplier<Set<String>> STRUCTURE_MODS = Suppliers.memoize(() -> new HashSet<>(AllConfigs.common().STRUCTURES_MODS.get()));
    private static final Supplier<Set<String>> STRUCTURES = Suppliers.memoize(() -> new HashSet<>(AllConfigs.common().STRUCTURES.get()));
    private static final Supplier<Registry<Structure>> STRUCTURES_REGISTRY = Suppliers.memoize(() -> SERVER_INSTANCE.registryAccess().registryOrThrow(Registries.STRUCTURE));

    public static void init() {
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            SERVER_INSTANCE = server;
        });

        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (entity instanceof Mob mob) {
                onSpawn(mob);
            }
        });
    }

    private static void onSpawn(Mob entity) {
        if (AllConfigs.common().ALLOW_MOBS_SPAWNED_BY_SPAWNERS_TO_DESPAWN.get()) return;
        if (!entity.level().isClientSide && !entity.getTags().contains(DespawnTweaks.ID + ".shouldNotDespawn")) {
            ((IDespawnTweaksMob) entity).despawnTweaker$setSpawnStructures(
                entity.level().getChunkAt(entity.blockPosition()).getAllReferences().keySet()
            );
            entity.addTag(DespawnTweaks.ID + ".shouldNotDespawn");
        }
    }

    public static boolean onCheckDespawn(Mob entity) {
        if (entity.level().isClientSide || !entity.getTags().contains("despawntweaks.shouldNotDespawn")) {
            return true;
        }

        ChunkAccess levelChunk = entity.level().getChunk(
            entity.blockPosition().getX() >> 4,
            entity.blockPosition().getZ() >> 4,
            ChunkStatus.FULL,
            false
        );

        if (levelChunk == null)
            return true;

        if (FabricSpawnEvents.STRUCTURE_MODS.get().isEmpty() && FabricSpawnEvents.STRUCTURES.get().isEmpty()) {
           return false;
        } else {
            for (Structure structure : ((IDespawnTweaksMob) entity).despawnTweaker$getSpawnStructures()) {
                var registryName = FabricSpawnEvents.STRUCTURES_REGISTRY.get().getKey(structure);
                if (registryName == null) continue;

                boolean canDeny = FabricSpawnEvents.STRUCTURE_MODS.get().contains(registryName.getNamespace()) ||
                    FabricSpawnEvents.STRUCTURES.get().contains(registryName.toString());

                if (canDeny) {
                    return false;
                }
            }
        }

        return true;
    }

}
#endif