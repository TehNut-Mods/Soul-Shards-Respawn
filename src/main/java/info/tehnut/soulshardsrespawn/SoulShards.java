package info.tehnut.soulshardsrespawn;

import info.tehnut.soulshardsrespawn.command.CommandSoulShards;
import info.tehnut.soulshardsrespawn.core.ConfigHandler;
import info.tehnut.soulshardsrespawn.core.RegistrarSoulShards;
import info.tehnut.soulshardsrespawn.core.data.Binding;
import info.tehnut.soulshardsrespawn.core.data.Tier;
import info.tehnut.soulshardsrespawn.item.ItemSoulShard;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

import java.io.File;

@Mod(modid = SoulShards.MODID, name = SoulShards.NAME, version = SoulShards.VERSION, guiFactory = "info.tehnut.soulshardsrespawn.client.ConfigGui$Factory")
public class SoulShards {

    public static final String MODID = "soulshardsrespawn";
    public static final String NAME = "Soul Shards Respawn";
    public static final String VERSION = "@VERSION@";
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
        config = new ConfigHandler(new File(event.getModConfigurationDirectory(), MODID));
        config.syncConfig();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        config.syncEntityList();
        config.syncMultiblock();
    }

    @Mod.EventHandler
    public void serverStart(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandSoulShards());
    }
}
