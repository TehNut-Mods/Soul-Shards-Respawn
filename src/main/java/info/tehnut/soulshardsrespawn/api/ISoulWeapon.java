package info.tehnut.soulshardsrespawn.api;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public interface ISoulWeapon {

    int getSoulBonus(ItemStack stack, PlayerEntity player, LivingEntity killedEntity);
}
