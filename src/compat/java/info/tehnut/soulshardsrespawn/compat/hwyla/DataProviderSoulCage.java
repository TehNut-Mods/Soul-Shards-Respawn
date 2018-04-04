package info.tehnut.soulshardsrespawn.compat.hwyla;

import info.tehnut.soulshardsrespawn.block.TileEntitySoulCage;
import info.tehnut.soulshardsrespawn.core.data.Binding;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.UsernameCache;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import javax.annotation.Nonnull;
import java.util.List;

public class DataProviderSoulCage implements IWailaDataProvider {

    public static final IWailaDataProvider INSTANCE = new DataProviderSoulCage();

    @Nonnull
    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> tooltip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        if (!accessor.getNBTData().hasKey("cageBinding"))
            return tooltip;

        Binding binding = new Binding(accessor.getNBTData().getCompoundTag("cageBinding"));
        EntityEntry entityEntry = ForgeRegistries.ENTITIES.getValue(binding.getBoundEntity());
        if (entityEntry != null)
            tooltip.add(I18n.format("tooltip.soulshardsrespawn.bound", entityEntry.getName()));

        tooltip.add(I18n.format("tooltip.soulshardsrespawn.tier", binding.getTier().getIndex()));

        if (accessor.getNBTData().hasKey("ownerName"))
            tooltip.add(I18n.format("tooltip.soulshardsrespawn.owner", accessor.getNBTData().getString("ownerName")));

        return tooltip;
    }

    @Nonnull
    @Override
    public NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity te, NBTTagCompound tag, World world, BlockPos pos) {
        TileEntitySoulCage soulCage = (TileEntitySoulCage) te;
        Binding binding = soulCage.getBinding();
        if (binding != null) {
            tag.setTag("cageBinding", binding.serializeNBT());
            if (binding.getOwner() != null) {
                String ownerName = UsernameCache.getLastKnownUsername(binding.getOwner());
                if (ownerName != null)
                    tag.setString("ownerName", ownerName);
            }
        }
        return tag;
    }
}
