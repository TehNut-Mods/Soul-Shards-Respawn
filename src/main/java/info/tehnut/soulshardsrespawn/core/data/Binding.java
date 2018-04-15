package info.tehnut.soulshardsrespawn.core.data;

import info.tehnut.soulshardsrespawn.api.IBinding;
import info.tehnut.soulshardsrespawn.api.IShardTier;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nullable;
import java.util.UUID;

public class Binding implements IBinding, INBTSerializable<NBTTagCompound> {

    @Nullable
    private ResourceLocation boundEntity;
    @Nullable
    private UUID owner;
    private int kills;

    public Binding(ResourceLocation boundEntity, UUID owner, int kills) {
        this.boundEntity = boundEntity;
        this.owner = owner;
        this.kills = kills;
    }

    public Binding(ResourceLocation boundEntity, int kills) {
        this(boundEntity, null, kills);
    }

    public Binding(NBTTagCompound bindingTag) {
        deserializeNBT(bindingTag);
    }

    @Nullable
    @Override
    public ResourceLocation getBoundEntity() {
        return boundEntity;
    }

    public Binding setBoundEntity(@Nullable ResourceLocation boundEntity) {
        this.boundEntity = boundEntity;
        return this;
    }

    @Nullable
    @Override
    public UUID getOwner() {
        return owner;
    }

    public Binding setOwner(@Nullable UUID owner) {
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
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();

        if (boundEntity != null)
            tag.setString("bound", boundEntity.toString());
        if (owner != null)
            tag.setString("owner", owner.toString());
        tag.setInteger("kills", kills);
        return tag;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        if (nbt.hasKey("bound"))
            this.boundEntity = new ResourceLocation(nbt.getString("bound"));
        if (nbt.hasKey("owner"))
            this.owner = UUID.fromString(nbt.getString("owner"));
        this.kills = nbt.getInteger("kills");
    }

    @Nullable
    public static Binding fromNBT(ItemStack stack) {
        if (!stack.hasTagCompound())
            return null;

        NBTTagCompound tag = stack.getTagCompound();
        if (!tag.hasKey("binding"))
            return null;

        return new Binding(tag.getCompoundTag("binding"));
    }
}
