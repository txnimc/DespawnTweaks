package toni.despawntweaks.foundation.config;


#if FABRIC
    #if AFTER_21_1
    import net.neoforged.neoforge.common.ModConfigSpec.*;
    #endif

    #if CURRENT_20_1
	import net.minecraftforge.common.ForgeConfigSpec.*;
    #endif
#endif

#if FORGE
import net.minecraftforge.common.ForgeConfigSpec.*;
#endif

#if NEO
import net.neoforged.neoforge.common.ModConfigSpec.*;
#endif

import toni.lib.config.ConfigBase;

import java.util.ArrayList;
import java.util.List;

public class CCommon extends ConfigBase {

    public final ConfigBool ALLOW_MOBS_SPAWNED_BY_SPAWNERS_TO_DESPAWN = b(true, "allowMobsSpawnedBySpawnersToDespawn", "Turn this off to disable the despawn of mobs spawned by spawners");
    public final ConfigBool ENABLE_LET_ME_DESPAWN_OPTIMIZATION = b(true, "enableLetMeDespawnOptimization", "Turn this off to disable the despawn optimizations.");
    public final ConfigBool ALLOW_EQUIPMENT_DROPS = b(true, "allowEquipmentDrops", "Turn this off to disable the equipments drop on mobs despawn");

    public final CValue<List<? extends String>, ConfigValue<List<? extends String>>> STRUCTURES_MODS
        = new CValue<>("StructuresMods", builder -> builder.define("StructuresMods", new ArrayList<>()), "If you add modIDs to this list, only mobs in the structures of the mods will be affected by DespawnTweaker.");

    public final CValue<List<? extends String>, ConfigValue<List<? extends String>>> STRUCTURES
        = new CValue<>("Structures", builder -> builder.define("Structures", new ArrayList<>()), "If you add sturctures registry names to list, only mobs in the structures will be affected by DespawnTweaker.");

    @Override
    public String getName() {
        return "common";
    }
}
