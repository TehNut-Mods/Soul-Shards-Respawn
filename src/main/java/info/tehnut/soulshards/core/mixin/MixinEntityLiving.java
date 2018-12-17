package info.tehnut.soulshards.core.mixin;

import info.tehnut.soulshards.SoulShards;
import info.tehnut.soulshards.api.BindingEvent;
import info.tehnut.soulshards.api.IBinding;
import info.tehnut.soulshards.api.ISoulWeapon;
import info.tehnut.soulshards.core.EventHandler;
import info.tehnut.soulshards.core.RegistrarSoulShards;
import info.tehnut.soulshards.item.ItemSoulShard;
import net.fabricmc.fabric.util.HandlerList;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import info.tehnut.soulshards.core.data.Binding;
import info.tehnut.soulshards.core.data.Tier;

@Mixin(LivingEntity.class)
public class MixinEntityLiving {

    @Inject(method = "initDataTracker", at = @At("TAIL"))
    private void initDataTracker(CallbackInfo callbackInfo) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity instanceof PlayerEntity)
            return;

        entity.getDataTracker().startTracking(SoulShards.CAGE_BORN_TAG, false);
    }

    @Inject(method = "onDeath", at = @At("HEAD"))
    private void onDeathEvent(DamageSource source, CallbackInfo callbackInfo) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity instanceof PlayerEntity)
            return;

        EventHandler.onEntityDeath(entity, source);
    }
}
