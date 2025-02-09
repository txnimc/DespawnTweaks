package toni.despawntweaks.foundation.data;

#if FABRIC
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import toni.despawntweaks.DespawnTweaks;

public class DespawnTweaksDatagen  implements DataGeneratorEntrypoint {

    @Override
    public String getEffectiveModId() {
        return DespawnTweaks.ID;
    }

    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        var pack = fabricDataGenerator.createPack();
        pack.addProvider(ConfigLangDatagen::new);
    }
}
#endif