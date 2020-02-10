package info.tehnut.soulshardsrespawn.item;

import info.tehnut.soulshardsrespawn.api.ISoulWeapon;
import info.tehnut.soulshardsrespawn.core.RegistrarSoulShards;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.LazyValue;

public class ItemVileSword extends SwordItem implements ISoulWeapon {

    public static final IItemTier MATERIAL_VILE = new MaterialVile();

    public ItemVileSword() {
        super(MATERIAL_VILE, 3, -2.4F, new Properties().group(ItemGroup.COMBAT));
    }

    @Override
    public int getSoulBonus(ItemStack stack, PlayerEntity player, LivingEntity killedEntity) {
        return 1;
    }

    public static class MaterialVile implements IItemTier {

        private final LazyValue<Ingredient> ingredient;

        public MaterialVile() {
            this.ingredient = new LazyValue<>(() -> Ingredient.fromItems(RegistrarSoulShards.CORRUPTED_INGOT));
        }

        @Override
        public int getMaxUses() {
            return ItemTier.IRON.getMaxUses();
        }

        @Override
        public float getEfficiency() {
            return ItemTier.IRON.getEfficiency();
        }

        @Override
        public float getAttackDamage() {
            return ItemTier.IRON.getAttackDamage();
        }

        @Override
        public int getHarvestLevel() {
            return ItemTier.IRON.getHarvestLevel();
        }

        @Override
        public int getEnchantability() {
            return ItemTier.IRON.getEnchantability();
        }

        @Override
        public Ingredient getRepairMaterial() {
            return ingredient.getValue();
        }

    }
}
