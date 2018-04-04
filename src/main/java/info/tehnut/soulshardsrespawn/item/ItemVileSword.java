package info.tehnut.soulshardsrespawn.item;

import info.tehnut.soulshardsrespawn.SoulShards;
import info.tehnut.soulshardsrespawn.api.ISoulWeapon;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraftforge.common.util.EnumHelper;

public class ItemVileSword extends ItemSword implements ISoulWeapon {

    @SuppressWarnings("ConstantConditions")
    public ItemVileSword() {
        super(EnumHelper.addToolMaterial("vile", ToolMaterial.IRON.getHarvestLevel(), ToolMaterial.IRON.getMaxUses(), ToolMaterial.IRON.getEfficiency(), ToolMaterial.IRON.getAttackDamage(), ToolMaterial.IRON.getEnchantability()));

        setUnlocalizedName(SoulShards.MODID + ".vile_sword");
        setCreativeTab(SoulShards.TAB_SS);
    }

    @Override
    public int getSoulBonus(ItemStack stack, EntityPlayer player, EntityLivingBase killedEntity) {
        return 1;
    }
}
