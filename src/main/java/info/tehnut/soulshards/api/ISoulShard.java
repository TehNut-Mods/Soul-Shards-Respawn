package info.tehnut.soulshards.api;

import net.minecraft.item.ItemStack;

public interface ISoulShard {

    IBinding getBinding(ItemStack stack);
}
