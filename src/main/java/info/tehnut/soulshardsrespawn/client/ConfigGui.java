package info.tehnut.soulshardsrespawn.client;

import com.google.common.collect.Lists;
import info.tehnut.soulshardsrespawn.SoulShards;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.fml.client.IModGuiFactory;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.IConfigElement;

import java.util.List;
import java.util.Set;

public class ConfigGui extends GuiConfig {

    public ConfigGui(GuiScreen parentScreen) {
        super(parentScreen, getConfigElements(), SoulShards.MODID, false, false, SoulShards.NAME);
    }

    private static List<IConfigElement> getConfigElements() {
        return Lists.newArrayList(
                new ConfigElement(SoulShards.CONFIG.getConfig().getCategory("balance")),
                new ConfigElement(SoulShards.CONFIG.getConfig().getCategory("entity_list")),
                new ConfigElement(SoulShards.CONFIG.getConfig().getCategory("server")),
                new ConfigElement(SoulShards.CONFIG.getConfig().getCategory("client")),
                new ConfigElement(SoulShards.CONFIG.getConfig().getCategory("compat"))
        );
    }

    public static class Factory implements IModGuiFactory {
        @Override
        public void initialize(Minecraft minecraftInstance) {

        }

        @Override
        public boolean hasConfigGui() {
            return true;
        }

        @Override
        public GuiScreen createConfigGui(GuiScreen parentScreen) {
            return new ConfigGui(parentScreen);
        }

        @Override
        public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
            return null;
        }
    }
}
