package info.tehnut.soulshards.item;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.recipe.Ingredient;
import net.minecraft.util.LazyCachedSupplier;
import info.tehnut.soulshards.api.ISoulWeapon;
import info.tehnut.soulshards.core.RegistrarSoulShards;

public class ItemVileSword extends SwordItem implements ISoulWeapon {

    public static final ToolMaterial MATERIAL_VILE = new MaterialVile();

    public ItemVileSword() {
        super(MATERIAL_VILE, 3, -2.4F, new Settings().itemGroup(ItemGroup.COMBAT));
    }

    @Override
    public int getSoulBonus(ItemStack stack, PlayerEntity player, LivingEntity killedEntity) {
        return 1;
    }

    public static class MaterialVile implements ToolMaterial {

        private final LazyCachedSupplier<Ingredient> ingredient;

        public MaterialVile() {
            this.ingredient = new LazyCachedSupplier<>(() -> Ingredient.ofStacks(new ItemStack(RegistrarSoulShards.CORRUPTED_INGOT)));
        }

        @Override
        public int getDurability() {
            return ToolMaterials.IRON.getDurability();
        }

        @Override
        public float getBlockBreakingSpeed() {
            return ToolMaterials.IRON.getBlockBreakingSpeed();
        }

        @Override
        public float getAttackDamage() {
            return ToolMaterials.IRON.getAttackDamage();
        }

        @Override
        public int getMiningLevel() {
            return ToolMaterials.IRON.getMiningLevel();
        }

        @Override
        public int getEnchantability() {
            return ToolMaterials.IRON.getEnchantability();
        }

        @Override
        public Ingredient getRepairIngredient() {
            return ingredient.get();
        }

    }
}
