package toni.despawntweaks.foundation;

#if mc >= 211

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;

#if FABRIC
import net.minecraft.core.registries.BuiltInRegistries;
import toni.lib.utils.VersionUtils;
#elif NEO
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;
#endif

public class DataComponentRegistry {
    public static final String COMPONENT_ID = "despawntweaker_picked";

    #if FABRIC
    public static DataComponentType<Boolean> DESPAWNTWEAKER_PICKED;
    #elif NEO
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS =
        DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, "despawntweaks");

    public static final Supplier<DataComponentType<Boolean>> DESPAWNTWEAKER_PICKED =
        DATA_COMPONENTS.register(COMPONENT_ID, () ->
            DataComponentType.<Boolean>builder()
                .persistent(Codec.BOOL)
                .networkSynchronized(ByteBufCodecs.BOOL)
                .build()
        );
    #endif

    public static DataComponentType<Boolean> get() {
        #if FABRIC
        return DESPAWNTWEAKER_PICKED;
        #else
        return DESPAWNTWEAKER_PICKED.get();
        #endif
    }

    public static void init() {
        #if FABRIC
        DESPAWNTWEAKER_PICKED = DataComponentType.<Boolean>builder()
            .persistent(Codec.BOOL)
            .networkSynchronized(ByteBufCodecs.BOOL)
            .build();

        Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            VersionUtils.resource("your_mod_id", COMPONENT_ID),
            DESPAWNTWEAKER_PICKED
        );
        #endif
    }
}

#endif