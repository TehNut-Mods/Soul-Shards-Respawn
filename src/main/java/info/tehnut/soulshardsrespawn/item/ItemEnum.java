package info.tehnut.soulshardsrespawn.item;

import info.tehnut.soulshardsrespawn.SoulShards;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemEnum<T extends Enum<T> & ISubItem> extends Item {

    protected final T[] types;

    public ItemEnum(Class<T> enumClass) {
        super();

        this.types = enumClass.getEnumConstants();

        setUnlocalizedName(SoulShards.MODID);
        setHasSubtypes(types.length > 1);
        setCreativeTab(SoulShards.TAB_SS);
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        return super.getUnlocalizedName(stack) + "." + getItemType(stack).getInternalName();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> subItems) {
        if (!isInCreativeTab(tab))
            return;

        for (T type : types)
            subItems.add(new ItemStack(this, 1, type.ordinal()));
    }

    public T getItemType(ItemStack stack) {
        return types[MathHelper.clamp(stack.getItemDamage(), 0, types.length - 1)];
    }
}
