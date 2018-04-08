package info.tehnut.soulshardsrespawn.compat;

import info.tehnut.soulshardsrespawn.SoulShards;
import info.tehnut.soulshardsrespawn.core.RegistrarSoulShards;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.translation.I18n;

@JEIPlugin
public class JEICompatibilityPlugin implements IModPlugin {

    @Override
    public void register(IModRegistry registry) {
        registry.addIngredientInfo(
                new ItemStack(RegistrarSoulShards.SOUL_SHARD),
                ItemStack.class,
                I18n.translateToLocalFormatted("jei.soulshardsrespawn.soul_shard.desc", SoulShards.config.getMultiblockPattern().getCatalyst().getDisplayName())
        );
    }
}
