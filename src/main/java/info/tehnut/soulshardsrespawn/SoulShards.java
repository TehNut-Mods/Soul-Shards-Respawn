package info.tehnut.soulshardsrespawn;

import com.google.common.collect.Sets;
import info.tehnut.soulshardsrespawn.command.CommandSoulShards;
import info.tehnut.soulshardsrespawn.core.ConfigHandler;
import info.tehnut.soulshardsrespawn.core.RegistrarSoulShards;
import info.tehnut.soulshardsrespawn.core.data.Binding;
import info.tehnut.soulshardsrespawn.core.data.Tier;
import info.tehnut.soulshardsrespawn.core.util.compat.CompatibilityPlugin;
import info.tehnut.soulshardsrespawn.core.util.compat.ICompatibilityPlugin;
import info.tehnut.soulshardsrespawn.item.ItemSoulShard;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.Set;

@Mod(modid = SoulShards.MODID, name = SoulShards.NAME, version = SoulShards.VERSION, guiFactory = "info.tehnut.soulshardsrespawn.client.ConfigGui$Factory")
public class SoulShards {

    public static final String MODID = "soulshardsrespawn";
    public static final String NAME = "Soul Shards Respawn";
    public static final String VERSION = "@VERSION@";
    public static final Logger LOGGER = LogManager.getLogger(NAME);
    public static final Set<ICompatibilityPlugin> COMPAT_PLUGINS = Sets.newHashSet();
    public static final CreativeTabs TAB_SS = new CreativeTabs(MODID) {
        @Override
        public ItemStack getTabIconItem() {
            ItemStack shard = new ItemStack(RegistrarSoulShards.SOUL_SHARD);
            Binding binding = new Binding(null, Tier.maxKills);
            ((ItemSoulShard) RegistrarSoulShards.SOUL_SHARD).updateBinding(shard, binding);
            return shard;
        }
    };

    public static ConfigHandler config;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        COMPAT_PLUGINS.addAll(CompatibilityPlugin.Gather.gather(event.getAsmData()));
        config = new ConfigHandler(new File(event.getModConfigurationDirectory(), MODID));
        config.syncConfig();

        for (ICompatibilityPlugin plugin : COMPAT_PLUGINS)
            plugin.preInit();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        config.syncEntityList();
        config.syncMultiblock();

        for (ICompatibilityPlugin plugin : COMPAT_PLUGINS)
            plugin.init();
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        for (ICompatibilityPlugin plugin : COMPAT_PLUGINS)
            plugin.postInit();
    }

    @Mod.EventHandler
    public void serverStart(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandSoulShards());
    }
}
