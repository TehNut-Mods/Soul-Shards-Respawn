package info.tehnut.soulshards;

import com.google.gson.reflect.TypeToken;
import info.tehnut.soulshards.core.EventHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.FabricLoader;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.util.registry.Registry;
import info.tehnut.soulshards.core.ConfigSoulShards;
import info.tehnut.soulshards.core.RegistrarSoulShards;
import info.tehnut.soulshards.core.data.Tier;
import info.tehnut.soulshards.core.util.JsonUtil;
import net.minecraft.world.GameRules;

import java.io.File;

public class SoulShards implements ModInitializer {

    public static final String MODID = "soulshards";
    public static final ConfigSoulShards CONFIG = JsonUtil.fromJson(TypeToken.get(ConfigSoulShards.class), new File(FabricLoader.INSTANCE.getConfigDirectory(), MODID + "/" + MODID + ".json"), new ConfigSoulShards());
    public static final TrackedData<Boolean> CAGE_BORN_TAG = DataTracker.registerData(LivingEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

    @Override
    public void onInitialize() {
        Tier.readTiers();
        ConfigSoulShards.handleMultiblock();
        RegistrarSoulShards.registerBlocks(Registry.BLOCKS);
        RegistrarSoulShards.registerItems(Registry.ITEMS);
        RegistrarSoulShards.registerEnchantments(Registry.ENCHANTMENTS);
        EventHandler.init();
        GameRules.getKeys().put("allowCageSpawns", new GameRules.Key("true", GameRules.Type.BOOLEAN));
    }
}
