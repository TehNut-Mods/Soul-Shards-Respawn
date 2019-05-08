package info.tehnut.soulshards;

import com.google.gson.reflect.TypeToken;
import info.tehnut.soulshards.core.config.ConfigSoulShards;
import info.tehnut.soulshards.core.EventHandler;
import info.tehnut.soulshards.core.RegistrarSoulShards;
import info.tehnut.soulshards.core.data.Tier;
import info.tehnut.soulshards.core.util.JsonUtil;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.GameRules;

import java.io.File;

public class SoulShards implements ModInitializer {

    public static final String MODID = "soulshards";
    public static final ConfigSoulShards CONFIG = JsonUtil.fromJson(TypeToken.get(ConfigSoulShards.class), new File(FabricLoader.getInstance().getConfigDirectory(), MODID + "/" + MODID + ".json"), new ConfigSoulShards());
    public static TrackedData<Boolean> cageBornTag;

    @Override
    public void onInitialize() {
        Tier.readTiers();
        ConfigSoulShards.handleMultiblock();
        RegistrarSoulShards.registerBlocks(Registry.BLOCK);
        RegistrarSoulShards.registerItems(Registry.ITEM);
        RegistrarSoulShards.registerEnchantments(Registry.ENCHANTMENT);
        EventHandler.init();
        GameRules.getKeys().put("allowCageSpawns", new GameRules.Key("true", GameRules.Type.BOOLEAN));
    }
}
