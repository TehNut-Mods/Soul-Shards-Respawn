package info.tehnut.soulshardsrespawn.compat.hwyla;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaEntityAccessor;
import mcp.mobius.waila.api.IWailaEntityProvider;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.List;

public class DataProviderCageBorn implements IWailaEntityProvider {

    public static final IWailaEntityProvider INSTANCE = new DataProviderCageBorn();

    @Nonnull
    @Override
    public List<String> getWailaBody(Entity entity, List<String> tooltip, IWailaEntityAccessor accessor, IWailaConfigHandler config) {
        if (accessor.getNBTData().getBoolean("cageBorn"))
            tooltip.add(I18n.format("tooltip.soulshardsrespawn.cage_born"));

        return tooltip;
    }

    @Nonnull
    @Override
    public NBTTagCompound getNBTData(EntityPlayerMP player, Entity ent, NBTTagCompound tag, World world) {
        tag.setBoolean("cageBorn", ent.getEntityData().hasKey("cageBorn"));
        return tag;
    }
}
