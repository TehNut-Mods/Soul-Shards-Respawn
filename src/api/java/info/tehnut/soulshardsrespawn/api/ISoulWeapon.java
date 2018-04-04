package info.tehnut.soulshardsrespawn.api;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public interface ISoulWeapon {

    int getSoulBonus(ItemStack stack, EntityPlayer player, EntityLivingBase killedEntity);
}
