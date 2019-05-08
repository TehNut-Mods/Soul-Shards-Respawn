package info.tehnut.soulshards.core.config;

public class ConfigClient {
    private boolean displayDurabilityBar;

    public ConfigClient(boolean displayDurabilityBar) {
        this.displayDurabilityBar = displayDurabilityBar;
    }

    public ConfigClient() {
        this(true);
    }

    public boolean displayDurabilityBar() {
        return displayDurabilityBar;
    }
}
