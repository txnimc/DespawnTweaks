package toni.despawntweaks.mixins;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import toni.despawntweaks.foundation.IDespawnTweaksMob;
import toni.despawntweaks.foundation.config.AllConfigs;

#if mc >= 211
import toni.despawntweaks.foundation.DataComponentRegistry;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.core.component.DataComponents;
#endif

#if FABRIC
import toni.despawntweaks.foundation.FabricSpawnEvents;
#endif

import java.util.Collections;
import java.util.Set;

@Mixin(Mob.class)
public abstract class MobMixin extends LivingEntity implements IDespawnTweaksMob {
    @Shadow private boolean persistenceRequired;

    @Shadow public abstract @NotNull ItemStack getItemBySlot(@NotNull EquipmentSlot arg);

    protected MobMixin(EntityType<? extends LivingEntity> arg, Level arg2) {
        super(arg, arg2);
    }

    @Inject(method = "setItemSlotAndDropWhenKilled", at = @At("TAIL"))
    private void onSetItemSlotAndDropWhenKilled(EquipmentSlot arg, ItemStack itemStack, CallbackInfo ci) {
        #if mc <= 201
        if (AllConfigs.common().ENABLE_LET_ME_DESPAWN_OPTIMIZATION.get()) {
            EquipmentSlot equipmentSlot = Mob.getEquipmentSlotForItem(itemStack);
            ItemStack stack = this.getItemBySlot(equipmentSlot);
            stack.getOrCreateTag().putBoolean("DespawnTweakerPicked", true);
            this.addTag("despawnTweaker.pickedItems");
            this.persistenceRequired = this.hasCustomName();
        }

        #else
        if (AllConfigs.common().ENABLE_LET_ME_DESPAWN_OPTIMIZATION.get()) {
            ItemStack stack = getItemBySlot(arg);
            stack.set(DataComponentRegistry.get(), Boolean.valueOf(true));
            addTag("despawnTweaker.pickedItems");
            this.persistenceRequired = hasCustomName();
        }
        #endif
    }

    #if FABRIC
    @Inject(method = "checkDespawn", at = @At("HEAD"), cancellable = true)
    private void onCheckDespawn(CallbackInfo ci) {
        var mob = (Mob) (Object) this;
        var ret = FabricSpawnEvents.onCheckDespawn(mob);
        if (!ret)
        {
            mob.setNoActionTime(0);
            ci.cancel();
        }
    }
    #endif

    @Inject(method = "checkDespawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;discard()V", shift = At.Shift.AFTER))
    private void onDespawn(CallbackInfo ci) {
        if (AllConfigs.common().ENABLE_LET_ME_DESPAWN_OPTIMIZATION.get() && this.getTags().contains("despawnTweaker.pickedItems")) {
            this.despawnTweaker$dropEquipmentOnDespawn();
        }
    }

    @Unique
    private static final EquipmentSlot[] EQUIPMENT_SLOTS = EquipmentSlot.values();

    @Unique
    private void despawnTweaker$dropEquipmentOnDespawn() {
        #if mc <= 201
        if (!AllConfigs.common().ALLOW_EQUIPMENT_DROPS.get()) return;
        for (EquipmentSlot equipmentSlot : EQUIPMENT_SLOTS) {
            ItemStack itemStack = this.getItemBySlot(equipmentSlot);
            CompoundTag tag = itemStack.getTag();
            boolean tagPresent = tag != null;
            if (!itemStack.isEmpty() && !(tagPresent && tag.toString().contains("vanishing_curse"))) {
                if (tagPresent && tag.getBoolean("DespawnTweakerPicked")) itemStack.removeTagKey("Picked");
                this.spawnAtLocation(itemStack);
                this.setItemSlot(equipmentSlot, ItemStack.EMPTY);
            }
        }

        #else
        if (!(AllConfigs.common().ALLOW_EQUIPMENT_DROPS.get()).booleanValue())
            return;
        for (EquipmentSlot equipmentSlot : EQUIPMENT_SLOTS) {
            ItemStack itemStack = getItemBySlot(equipmentSlot);
            boolean tagPresent = itemStack.has(DataComponentRegistry.get());
            if (!itemStack.isEmpty() && (!tagPresent || !(itemStack.get(DataComponents.ENCHANTMENTS)).toString().contains("vanishing_curse"))) {
                if (tagPresent && (itemStack.get(DataComponentRegistry.get())).booleanValue())
                    itemStack.remove(DataComponentRegistry.get());
                spawnAtLocation(itemStack);
                setItemSlot(equipmentSlot, ItemStack.EMPTY);
            }
        }
        #endif
    }

    @Unique
    private void despawnTweaker$removeTagOnDeath() {
        #if mc <= 201
        for (EquipmentSlot equipmentSlot : EQUIPMENT_SLOTS) {
            ItemStack itemStack = this.getItemBySlot(equipmentSlot);
            CompoundTag tag = itemStack.getTag();
            boolean tagPresent = tag != null;
            if (tagPresent && !itemStack.isEmpty() && !tag.toString().contains("vanishing_curse") && tag.getBoolean("DespawnTweakerPicked")) {
                itemStack.removeTagKey("DespawnTweakerPicked");
            }
        }
        #else
        for (EquipmentSlot equipmentSlot : EQUIPMENT_SLOTS) {
            ItemStack itemStack = getItemBySlot(equipmentSlot);
            boolean tagPresent = itemStack.has(DataComponentRegistry.get());
            if (tagPresent && !itemStack.isEmpty() && !(itemStack.get(DataComponents.ENCHANTMENTS)).toString().contains("vanishing_curse") && ((Boolean)itemStack.get(DataComponentRegistry.get())).booleanValue())
                itemStack.remove(DataComponentRegistry.get());
        }
        #endif
    }

    @Inject(method = {"dropFromLootTable", "dropCustomDeathLoot"}, at = @At("HEAD"))
    private void onDropFromLootTable(CallbackInfo ci) {
        if (this.getTags().contains("despawnTweaker.pickedItems")) this.despawnTweaker$removeTagOnDeath();
    }

    @Unique private @Nullable Set<Structure> despawnTweaker$spawnStructures = null;

    @Override
    public @NotNull Set<Structure> despawnTweaker$getSpawnStructures() {
        return this.despawnTweaker$spawnStructures == null ? Collections.emptySet() : this.despawnTweaker$spawnStructures;
    }

    @Override
    public void despawnTweaker$setSpawnStructures(Set<Structure> structureFeature) {
        this.despawnTweaker$spawnStructures = structureFeature;
    }
}
