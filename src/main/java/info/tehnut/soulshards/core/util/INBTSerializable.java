package info.tehnut.soulshards.core.util;

import net.minecraft.nbt.Tag;

public interface INBTSerializable<T extends Tag> {

    T serializeNBT();

    void deserializeNBT(T tag);
}
