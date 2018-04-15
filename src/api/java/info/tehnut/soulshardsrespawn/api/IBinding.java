package info.tehnut.soulshardsrespawn.api;

import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public interface IBinding {

    @Nullable
    UUID getOwner();

    @Nullable
    ResourceLocation getBoundEntity();

    int getKills();

    @Nonnull
    IBinding addKills(int amount);

    @Nonnull
    IShardTier getTier();
}
