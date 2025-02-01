package toni.despawntweaks.foundation;

#if FORGELIKE

import toni.despawntweaks.foundation.config.AllConfigs;
import toni.despawntweaks.DespawnTweaks;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.structure.Structure;

#if FORGE
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.server.ServerLifecycleHooks;
#else
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.neoforged.neoforge.event.entity.living.MobDespawnEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
#endif

import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("resource")
public class ForgeSpawnEvents {
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onSpawn(#if FORGE MobSpawnEvent.FinalizeSpawn #else FinalizeSpawnEvent #endif event) {
        if (event.isSpawnCancelled()) return;
        if (AllConfigs.common().ALLOW_MOBS_SPAWNED_BY_SPAWNERS_TO_DESPAWN.get()) return;
        Mob entity = event.getEntity();
        if (!event.getSpawnType().equals(MobSpawnType.SPAWNER)) return;
        if (entity.level().isClientSide) return;
        ((IDespawnTweaksMob) entity).despawnTweaker$setSpawnStructures(entity.level().getChunkAt(entity.blockPosition()).getAllReferences().keySet());
        entity.addTag(DespawnTweaks.ID + ".shouldNotDespawn");
    }

    private static final Supplier<Set<String>> STRUCTURE_MODS = Suppliers.memoize(() -> new HashSet<>(AllConfigs.common().STRUCTURES_MODS.get()));
    private static final Supplier<Set<String>> STRUCTURES = Suppliers.memoize(() -> new HashSet<>(AllConfigs.common().STRUCTURES.get()));
    private static final Supplier<Registry<Structure>> STRUCTURES_REGISTRY = Suppliers.memoize(() -> ServerLifecycleHooks.getCurrentServer().registryAccess().registryOrThrow(Registries.STRUCTURE));

    @SubscribeEvent
    public static void onDespawn(#if FORGE MobSpawnEvent.AllowDespawn #else MobDespawnEvent #endif event) {
        var deny = #if FORGE Event.Result.DENY #else MobDespawnEvent.Result.DENY #endif;
        if (event.getResult().equals(deny)) return;
        Mob entity = event.getEntity();
        if (entity.level().isClientSide) return;
        if (!entity.getTags().contains(DespawnTweaks.ID + ".shouldNotDespawn")) return;
        ChunkAccess levelChunk = entity.level().getChunk(entity.blockPosition().getX() >> 4, entity.blockPosition().getZ() >> 4, ChunkStatus.FULL, false);
        if (levelChunk == null) return;
        if (STRUCTURE_MODS.get().isEmpty() && STRUCTURES.get().isEmpty()) {
            event.setResult(deny);
        } else {
            for (Structure structure : ((IDespawnTweaksMob) entity).despawnTweaker$getSpawnStructures()) {
                ResourceLocation registryName = STRUCTURES_REGISTRY.get().getKey(structure);
                if (registryName == null) continue;
                boolean canDeny = STRUCTURE_MODS.get().contains(registryName.getNamespace()) || STRUCTURES.get().contains(registryName.toString());
                if (!canDeny) continue;
                event.setResult(deny);
                break;
            }
        }
    }
}

#endif