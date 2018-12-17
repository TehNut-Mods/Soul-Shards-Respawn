package info.tehnut.soulshards.api;
import net.minecraft.util.Identifier;

import java.util.UUID;

public interface IBinding {

    UUID getOwner();

    Identifier getBoundEntity();

    int getKills();

    IBinding addKills(int amount);

    IShardTier getTier();
}
