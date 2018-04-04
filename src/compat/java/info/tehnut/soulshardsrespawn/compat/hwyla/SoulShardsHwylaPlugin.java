package info.tehnut.soulshardsrespawn.compat.hwyla;

import info.tehnut.soulshardsrespawn.block.TileEntitySoulCage;
import mcp.mobius.waila.api.IWailaPlugin;
import mcp.mobius.waila.api.IWailaRegistrar;
import mcp.mobius.waila.api.WailaPlugin;
import net.minecraft.entity.EntityLivingBase;

@WailaPlugin
public class SoulShardsHwylaPlugin implements IWailaPlugin {

    @Override
    public void register(IWailaRegistrar registrar) {
        registrar.registerBodyProvider(DataProviderSoulCage.INSTANCE, TileEntitySoulCage.class);
        registrar.registerNBTProvider(DataProviderSoulCage.INSTANCE, TileEntitySoulCage.class);

        registrar.registerBodyProvider(DataProviderCageBorn.INSTANCE, EntityLivingBase.class);
        registrar.registerNBTProvider(DataProviderCageBorn.INSTANCE, EntityLivingBase.class);
    }
}
