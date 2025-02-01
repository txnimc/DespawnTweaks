package toni.despawntweaks.foundation;

import net.minecraft.world.level.levelgen.structure.Structure;

import java.util.Set;

public interface IDespawnTweaksMob {
    Set<Structure> despawnTweaker$getSpawnStructures();
    void despawnTweaker$setSpawnStructures(Set<Structure> structureFeature);
}