package info.tehnut.soulshardsrespawn.compat.hwyla;

import info.tehnut.soulshardsrespawn.block.TileEntitySoulCage;
import info.tehnut.soulshardsrespawn.core.data.Binding;
import mcp.mobius.waila.api.*;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

@WailaPlugin
public class HwylaCompatibilityPlugin implements IWailaPlugin {

    @Override
    public void register(IRegistrar registrar) {
        registrar.registerEntityDataProvider((data, player, world, entity) ->
                data.putBoolean("cageBorn", entity.getPersistentData().contains("cageBorn")), LivingEntity.class);

        registrar.registerComponentProvider(new IEntityComponentProvider() {
            @Override
            public void appendBody(List<ITextComponent> tooltip, IEntityAccessor accessor, IPluginConfig config) {
                if (accessor.getServerData().getBoolean("cageBorn"))
                    tooltip.add(new TranslationTextComponent("tooltip.soulshards.cage_born"));
            }
        }, TooltipPosition.BODY, LivingEntity.class);

        registrar.registerBlockDataProvider((data, player, world, blockEntity) -> {
            Binding binding = ((TileEntitySoulCage) blockEntity).getBinding();
            if (binding != null)
                data.put("binding", binding.serializeNBT());
        }, TileEntitySoulCage.class);

        registrar.registerComponentProvider(new IComponentProvider() {
            @Override
            public void appendBody(List<ITextComponent> tooltip, IDataAccessor accessor, IPluginConfig config) {
                if (!accessor.getServerData().contains("binding"))
                    return;

                Binding binding = new Binding(accessor.getServerData().getCompound("binding"));

                if (binding.getBoundEntity() != null) {
                    EntityType<?> entityEntry = ForgeRegistries.ENTITIES.getValue(binding.getBoundEntity());
                    if (entityEntry != null)
                        tooltip.add(new TranslationTextComponent("tooltip.soulshards.bound", entityEntry.getName()));
                    else
                        tooltip.add(new TranslationTextComponent("tooltip.soulshards.bound", binding.getBoundEntity().toString()).setStyle(new Style().setColor(TextFormatting.RED)));
                }

                tooltip.add(new TranslationTextComponent("tooltip.soulshards.tier", binding.getTier().getIndex()));
            }
        }, TooltipPosition.BODY, TileEntitySoulCage.class);
    }
}
