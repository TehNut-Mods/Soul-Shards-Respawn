package info.tehnut.soulshards.core.mixin;

import info.tehnut.soulshards.SoulShards;
import info.tehnut.soulshards.core.EventHandler;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class MixinEntityLiving {

    @Inject(method = "<init>", at = @At("RETURN"))
    private static void registerDataTracker(CallbackInfo callbackInfo) {
        SoulShards.cageBornTag = DataTracker.registerData(LivingEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    }

    @Inject(method = "initDataTracker", at = @At("TAIL"))
    private void initDataTracker(CallbackInfo callbackInfo) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity instanceof PlayerEntity)
            return;

        entity.getDataTracker().startTracking(SoulShards.cageBornTag, false);
    }

    @Inject(method = "onDeath", at = @At("HEAD"))
    private void onDeathEvent(DamageSource source, CallbackInfo callbackInfo) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity instanceof PlayerEntity)
            return;

        EventHandler.onEntityDeath(entity, source);
    }
}
