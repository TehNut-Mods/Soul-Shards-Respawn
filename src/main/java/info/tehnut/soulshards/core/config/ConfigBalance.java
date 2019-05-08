package info.tehnut.soulshards.core.config;

public class ConfigBalance {

    private boolean allowSpawnerAbsorption;
    private int absorptionBonus;
    private boolean allowBossSpawns;
    private boolean countCageBornForShard;
    private boolean requireOwnerOnline;
    private boolean requireRedstoneSignal;
    private int spawnCap;

    public ConfigBalance(boolean allowSpawnerAbsorption, int absorptionBonus, boolean allowBossSpawns, boolean countCageBornForShard, boolean requireOwnerOnline, boolean requireRedstoneSignal, int spawnCap) {
        this.allowSpawnerAbsorption = allowSpawnerAbsorption;
        this.absorptionBonus = absorptionBonus;
        this.allowBossSpawns = allowBossSpawns;
        this.countCageBornForShard = countCageBornForShard;
        this.requireOwnerOnline = requireOwnerOnline;
        this.requireRedstoneSignal = requireRedstoneSignal;
        this.spawnCap = spawnCap;
    }

    public ConfigBalance() {
        this(true, 200, false, false, false, false, 32);
    }

    public boolean allowSpawnerAbsorption() {
        return allowSpawnerAbsorption;
    }

    public int getAbsorptionBonus() {
        return absorptionBonus;
    }

    public boolean allowBossSpawns() {
        return allowBossSpawns;
    }

    public boolean countCageBornForShard() {
        return countCageBornForShard;
    }

    public boolean requireOwnerOnline() {
        return requireOwnerOnline;
    }

    public boolean requireRedstoneSignal() {
        return requireRedstoneSignal;
    }

    public int getSpawnCap() {
        return spawnCap;
    }
}
