package info.tehnut.soulshardsrespawn.api;

import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;

public interface ISoulShard {

    @Nullable
    IBinding getBinding(ItemStack stack);
}
