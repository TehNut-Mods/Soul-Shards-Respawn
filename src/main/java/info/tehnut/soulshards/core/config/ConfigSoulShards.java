package info.tehnut.soulshards.core.config;

import info.tehnut.soulshards.core.data.MultiblockPattern;

public class ConfigSoulShards {

    private static MultiblockPattern multiblock;

    private ConfigBalance balance;
    private ConfigClient client;
    private ConfigEntityList entityList;

    public ConfigSoulShards(ConfigBalance balance, ConfigClient client, ConfigEntityList entityList) {
        this.balance = balance;
        this.client = client;
        this.entityList = entityList;
    }

    public ConfigSoulShards() {
        this(new ConfigBalance(), new ConfigClient(), new ConfigEntityList());
    }

    public ConfigBalance getBalance() {
        return balance;
    }

    public ConfigClient getClient() {
        return client;
    }

    public ConfigEntityList getEntityList() {
        return entityList;
    }

    public static void handleMultiblock() {
        // FIXME parsing is currently broke
//        File multiblockFile = new File(FabricLoader.INSTANCE.getConfigDirectory(), SoulShards.MODID + "/multiblock.json");
//        if (!multiblockFile.exists()) {
//            try {
//                FileUtils.copyInputStreamToFile(ConfigSoulShards.class.getResourceAsStream("/data/multiblock.json"), multiblockFile);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//
//        multiblock = JsonUtil.fromJson(TypeToken.get(MultiblockPattern.class), multiblockFile);
        if (multiblock == null)
            multiblock = MultiblockPattern.DEFAULT;
    }

    public static MultiblockPattern getMultiblock() {
        if (multiblock == null)
            handleMultiblock();

        return multiblock;
    }
}
