package info.tehnut.soulshardsrespawn.core;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.reflect.TypeToken;
import info.tehnut.soulshardsrespawn.SoulShards;
import info.tehnut.soulshardsrespawn.core.data.MultiblockPattern;
import info.tehnut.soulshardsrespawn.core.util.JsonUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.Map;
import java.util.Set;

public class ConfigHandler {

    private static final Set<String> DEFAULT_DISABLES = Sets.newHashSet(
            "minecraft:armor_stand",
            "minecraft:elder_guardian",
            "minecraft:ender_dragon",
            "minecraft:wither"
    );

    private final Configuration config;
    private final Map<ResourceLocation, Boolean> entityMap;

    // Balance
    private int spawnCap;
    private boolean allowBossSpawns;
    private boolean allowSpawnerAbsorption;
    private int absorptionBonus;
    private boolean dropExperience;
    private boolean countCageBornForShard;
    private boolean addShardResetRecipe;
    private boolean allowShardCombination;
    private boolean allowFakePlayers;
    private MultiblockPattern multiblockPattern;

    // Server
    private boolean requireOwnerOnline;
    private boolean requireRedstoneSignal;

    // Client
    private boolean displayDurabilityBar;
    private boolean explodeCreativeTab;
    private boolean ignoreBlacklistForTab;

    public ConfigHandler(File configDir) {
        this.config = new Configuration(new File(configDir, SoulShards.MODID + ".cfg"));
        this.entityMap = Maps.newHashMap();

        MinecraftForge.EVENT_BUS.register(this);
    }

    public void syncConfig() {
        // Balance
        spawnCap = config.getInt("spawnCap", "balance", 30, 0, 256, "The maximum amount of cage born mobs that a cage can have in a 16 block radius before shutting off.");
        allowBossSpawns = config.getBoolean("allowBossSpawns", "balance", false, "Allows boss mobs to be spawned by cages.");
        allowSpawnerAbsorption = config.getBoolean("allowSpawnerAbsorption", "balance", true, "Allow shards to absorb Mob Spawners of the same entity type.");
        absorptionBonus = config.getInt("absorptionBonus", "balance", 200, 0, Integer.MAX_VALUE, "The amount of souls to absorb from Mob Spawners.");
        dropExperience = config.getBoolean("dropExperience", "balance", true, "Mobs spawned via the cage will drop experience.");
        countCageBornForShard = config.getBoolean("countCageBornForShard", "balance", false, "Mobs spawned via the cage should count toward shard kills.");
        addShardResetRecipe = config.getBoolean("addShardResetRecipe", "balance", true, "Adds a shapeless recipe that allows shards to be reset fresh.");
        allowShardCombination = config.getBoolean("allowShardCombination", "balance", true, "Allows shards of the same entity to be combined in an anvil for a direct kill addition.");
        allowFakePlayers = config.getBoolean("allowFakePlayers", "balance", false, "Allows fake players to grow Shards in their inventories.");

        // Server
        requireOwnerOnline = config.getBoolean("requireOwnerOnline", "server", false, "Requires the shard owner to be online. The owner is the first person to obtain a kill with the shard.");
        requireRedstoneSignal = config.getBoolean("requireRedstoneSignal", "server", false, "Requires a redstone signal to activate cages regardless of tier.");

        // Client
        displayDurabilityBar = config.getBoolean("displayDurabilityBar", "client", true, "Displays a durability bar for how full the shard is compared to the max tier available.");
        explodeCreativeTab = config.getBoolean("explodeCreativeTab", "client", false, "Displays a shard for every enabled entity in the creative tab.");
        ignoreBlacklistForTab = config.getBoolean("ignoreBlacklistForTab", "client", false, "Adds blacklisted entities to the creative tab if \"explodeCreativeTab\" is true.");

        config.save();
    }

    public void syncEntityList() {
        entityMap.clear();
        ForgeRegistries.ENTITIES.getEntries()
                .stream()
                .filter(e -> EntityLivingBase.class.isAssignableFrom(e.getValue().getEntityClass()))
                .forEach(e -> entityMap.put(e.getKey(), config.getBoolean(e.getKey().toString(), "entity_list", !DEFAULT_DISABLES.contains(e.getKey().toString()), "Allows the gathering and spawning of " + e.getValue().getName())));
    }

    public void syncMultiblock() {
        File multiblockFile = new File(config.getConfigFile().getParent(), "multiblock.json");
        if (!multiblockFile.exists()) {
            try {
                FileUtils.copyInputStreamToFile(ConfigHandler.class.getResourceAsStream("/assets/soulshardsrespawn/multiblock.json"), multiblockFile);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        multiblockPattern = JsonUtil.fromJson(TypeToken.get(MultiblockPattern.class), multiblockFile);
        if (multiblockPattern == null)
            multiblockPattern = MultiblockPattern.DEFAULT;
    }

    public boolean isEntityEnabled(ResourceLocation entityId) {
        return entityMap.getOrDefault(entityId, false);
    }

    public boolean isEntityEnabled(Class<? extends Entity> entityClass) {
        return isEntityEnabled(EntityList.getKey(entityClass));
    }

    public Configuration getConfig() {
        return config;
    }

    public Map<ResourceLocation, Boolean> getEntityMap() {
        return entityMap;
    }

    public int getSpawnCap() {
        return spawnCap;
    }

    public boolean allowBossSpawns() {
        return allowBossSpawns;
    }

    public boolean allowSpawnerAbsorption() {
        return allowSpawnerAbsorption;
    }

    public int getAbsorptionBonus() {
        return absorptionBonus;
    }

    public boolean dropExperience() {
        return dropExperience;
    }

    public boolean countCageBornForShard() {
        return countCageBornForShard;
    }

    public boolean addShardResetRecipe() {
        return addShardResetRecipe;
    }

    public boolean allowShardCombination() {
        return allowShardCombination;
    }

    public boolean allowFakePlayers() {
        return allowFakePlayers;
    }

    public MultiblockPattern getMultiblockPattern() {
        return multiblockPattern;
    }

    public boolean requireOwnerOnline() {
        return requireOwnerOnline;
    }

    public boolean requireRedstoneSignal() {
        return requireRedstoneSignal;
    }

    public boolean displayDurabilityBar() {
        return displayDurabilityBar;
    }

    public boolean explodeCreativeTab() {
        return explodeCreativeTab;
    }

    public boolean ignoreBlacklistForTab() {
        return ignoreBlacklistForTab;
    }

    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.getModID().equals(SoulShards.MODID)) {
            syncConfig();
            syncEntityList();
            syncMultiblock();
        }
    }
}
