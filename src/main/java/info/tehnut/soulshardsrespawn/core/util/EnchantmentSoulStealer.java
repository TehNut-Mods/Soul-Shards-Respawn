package info.tehnut.soulshardsrespawn.core.util;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentType;
import net.minecraft.inventory.EquipmentSlotType;

public class EnchantmentSoulStealer extends Enchantment {

    public EnchantmentSoulStealer() {
        super(Rarity.UNCOMMON, EnchantmentType.WEAPON, new EquipmentSlotType[] { EquipmentSlotType.MAINHAND });
    }

    @Override
    public int getMinEnchantability(int level) {
        return (level - 1) * 11;
    }

    @Override
    public int getMaxEnchantability(int level) {
        return this.getMinEnchantability(level) + 20;
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }
}
