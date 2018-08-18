package info.tehnut.soulshardsrespawn.core.data;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.reflect.TypeToken;
import info.tehnut.soulshardsrespawn.SoulShards;
import info.tehnut.soulshardsrespawn.api.IShardTier;
import info.tehnut.soulshardsrespawn.core.util.JsonUtil;
import net.minecraftforge.fml.common.Loader;

import java.io.File;
import java.util.List;
import java.util.TreeMap;

public class Tier implements IShardTier {

    public static final TreeMap<Integer, IShardTier> TIERS = Maps.newTreeMap();
    public static final List<IShardTier> INDEXED = Lists.newArrayList();
    public static int maxKills;

    // How many kills required to reach this tier.
    private final int killRequirement;
    // If this tier should require a player nearby
    private final boolean checkPlayer;
    // If this tier should ignore light levels
    private final boolean checkLight;
    // If this tier should allow redstone control
    private final boolean checkRedstone;
    // The max amount of mobs to spawn each time
    private final int spawnAmount;
    // The time (in ticks) between each spawn attempt
    private final int cooldown;

    public Tier(int killRequirement, boolean checkPlayer, boolean checkLight, boolean checkRedstone, int spawnAmount, int cooldown) {
        this.killRequirement = killRequirement;
        this.checkPlayer = checkPlayer;
        this.checkLight = checkLight;
        this.checkRedstone = checkRedstone;
        this.spawnAmount = spawnAmount;
        this.cooldown = cooldown;
    }

    @Override
    public int getKillRequirement() {
        return killRequirement;
    }

    @Override
    public boolean checkPlayer() {
        return checkPlayer;
    }

    @Override
    public boolean checkLight() {
        return checkLight;
    }

    @Override
    public boolean checkRedstone() {
        return checkRedstone;
    }

    @Override
    public int getSpawnAmount() {
        return spawnAmount;
    }

    @Override
    public int getCooldown() {
        return cooldown;
    }

    @Override
    public int getIndex() {
        return INDEXED.indexOf(this);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof Tier)) return false;

        Tier tier = (Tier) object;

        return killRequirement == tier.killRequirement;
    }

    @Override
    public int hashCode() {
        return killRequirement;
    }

    public static void readTiers() {
        Tier[] tiers = JsonUtil.fromJson(TypeToken.get(Tier[].class), new File(Loader.instance().getConfigDir(), SoulShards.MODID + "/tiers.json"), generateDefaults());
        for (Tier tier : tiers) {
            TIERS.put(tier.killRequirement, tier);
            INDEXED.add(tier);
            if (tier.getKillRequirement() > maxKills)
                maxKills = tier.getKillRequirement();
        }
    }

    private static Tier[] generateDefaults() {
        return new Tier[] {
                new Tier(0, true, false, false, 0, 0),
                new Tier(64, true, true, false, 2, 20 * 20),
                new Tier(128, true, true, false, 4, 10 * 20),
                new Tier(256, false, true, false, 4, 5 * 20),
                new Tier(512, false, false, false, 4, 5 * 20),
                new Tier(1024, false, false, true, 6, 2 * 20)
        };
    }
}
