package info.tehnut.soulshards.core.data;

import info.tehnut.soulshards.api.IBinding;
import info.tehnut.soulshards.api.IShardTier;
import info.tehnut.soulshards.core.util.INBTSerializable;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Identifier;

import java.util.UUID;

public class Binding implements IBinding, INBTSerializable<CompoundTag> {

    private Identifier boundEntity;
    private UUID owner;
    private int kills;

    public Binding(Identifier boundEntity, UUID owner, int kills) {
        this.boundEntity = boundEntity;
        this.owner = owner;
        this.kills = kills;
    }

    public Binding(Identifier boundEntity, int kills) {
        this(boundEntity, null, kills);
    }

    public Binding(CompoundTag bindingTag) {
        deserializeNBT(bindingTag);
    }

    @Override
    public Identifier getBoundEntity() {
        return boundEntity;
    }

    public Binding setBoundEntity(Identifier boundEntity) {
        this.boundEntity = boundEntity;
        return this;
    }

    @Override
    public UUID getOwner() {
        return owner;
    }

    public Binding setOwner(UUID owner) {
        this.owner = owner;
        return this;
    }

    @Override
    public int getKills() {
        return kills;
    }

    public Binding setKills(int kills) {
        this.kills = Math.min(Tier.maxKills, kills);
        return this;
    }

    @Override
    public Binding addKills(int kills) {
        this.kills = Math.min(Tier.maxKills, this.kills + kills);
        return this;
    }

    @Override
    public IShardTier getTier() {
        return Tier.TIERS.floorEntry(kills).getValue();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();

        if (boundEntity != null)
            tag.putString("bound", boundEntity.toString());
        if (owner != null)
            tag.putString("owner", owner.toString());
        tag.putInt("kills", kills);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        if (nbt.containsKey("bound"))
            this.boundEntity = new Identifier(nbt.getString("bound"));
        if (nbt.containsKey("owner"))
            this.owner = UUID.fromString(nbt.getString("owner"));
        this.kills = nbt.getInt("kills");
    }

    public static Binding fromNBT(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.containsKey("binding"))
            return null;

        return new Binding(tag.getCompound("binding"));
    }
}
