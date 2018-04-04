package info.tehnut.soulshardsrespawn.item;

import info.tehnut.soulshardsrespawn.core.RegistrarSoulShards;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.Locale;

public enum Materials implements ISubItem {

    CORRUPTED_INGOT,
    CORRUPTED_ESSENCE,
    VILE_DUST,
    ;

    @Nonnull
    @Override
    public String getInternalName() {
        return name().toLowerCase(Locale.ROOT);
    }

    @Nonnull
    @Override
    public ItemStack getStack(int count) {
        return new ItemStack(RegistrarSoulShards.MATERIALS, count, ordinal());
    }
}
