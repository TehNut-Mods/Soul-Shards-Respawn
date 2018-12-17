package info.tehnut.soulshards.core.util;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;

public class EnchantmentSoulStealer extends Enchantment {

    public EnchantmentSoulStealer() {
        super(Weight.UNCOMMON, EnchantmentTarget.WEAPON, new EquipmentSlot[]{EquipmentSlot.HAND_MAIN});
    }

    @Override
    public int getMinimumPower(int level) {
        return (level - 1) * 11;
    }

    @Override
    public int getMaximumPower(int level) {
        return this.getMinimumPower(level) + 20;
    }

    @Override
    public int getMaximumLevel() {
        return 5;
    }
}
