package info.tehnut.soulshardsrespawn;

import com.google.gson.reflect.TypeToken;
import info.tehnut.soulshardsrespawn.core.ConfigSoulShards;
import info.tehnut.soulshardsrespawn.core.RegistrarSoulShards;
import info.tehnut.soulshardsrespawn.core.data.Binding;
import info.tehnut.soulshardsrespawn.core.data.Tier;
import info.tehnut.soulshardsrespawn.core.util.JsonUtil;
import info.tehnut.soulshardsrespawn.item.ItemSoulShard;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

@Mod(SoulShards.MODID)
public class SoulShards {

    public static final String MODID = "soulshardsrespawn";
    public static final String NAME = "Soul Shards Respawn";
    public static final Logger LOGGER = LogManager.getLogger(NAME);
    public static final ConfigSoulShards CONFIG = JsonUtil.fromJson(TypeToken.get(ConfigSoulShards.class), new File(FMLPaths.CONFIGDIR.get().toFile(), MODID + "/" + MODID + ".json"), new ConfigSoulShards());
    public static final ItemGroup TAB_SS = new ItemGroup(MODID) {
        @Override
        public ItemStack createIcon() {
            ItemStack shard = new ItemStack(RegistrarSoulShards.SOUL_SHARD);
            Binding binding = new Binding(null, Tier.maxKills);
            ((ItemSoulShard) RegistrarSoulShards.SOUL_SHARD).updateBinding(shard, binding);
            return shard;
        }
    };

    public SoulShards() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setupClient);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::serverStart);
    }

    @SubscribeEvent
    public void setupClient(FMLClientSetupEvent event) {
        SoulShardsClient.initClient();
    }

    @SubscribeEvent
    public void serverStart(FMLServerStartingEvent event) {
//        event.registerServerCommand(new CommandSoulShards());
    }
}
