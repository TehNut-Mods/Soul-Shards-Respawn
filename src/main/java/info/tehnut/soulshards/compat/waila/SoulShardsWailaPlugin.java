package info.tehnut.soulshards.compat.waila;

import info.tehnut.soulshards.SoulShards;
import info.tehnut.soulshards.block.TileEntitySoulCage;
import info.tehnut.soulshards.core.data.Binding;
import mcp.mobius.waila.api.*;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.Style;
import net.minecraft.text.TextComponent;
import net.minecraft.text.TextFormat;
import net.minecraft.text.TranslatableTextComponent;
import net.minecraft.util.registry.Registry;

import java.util.List;

public class SoulShardsWailaPlugin implements IWailaPlugin {

    @Override
    public void register(IRegistrar registrar) {
        registrar.registerComponentProvider(new IEntityComponentProvider() {
            @Override
            public void appendBody(List<TextComponent> tooltip, IEntityAccessor accessor, IPluginConfig config) {
                if (accessor.getEntity().getDataTracker().get(SoulShards.cageBornTag))
                    tooltip.add(new TranslatableTextComponent("tooltip.soulshards.cage_born"));
            }
        }, TooltipPosition.BODY, LivingEntity.class);

        registrar.registerBlockDataProvider((data, player, world, blockEntity) -> {
            Binding binding = ((TileEntitySoulCage) blockEntity).getBinding();
            data.put("binding", binding.serializeNBT());
        }, TileEntitySoulCage.class);

        registrar.registerComponentProvider(new IComponentProvider() {
            @Override
            public void appendBody(List<TextComponent> tooltip, IDataAccessor accessor, IPluginConfig config) {
                Binding binding = new Binding(accessor.getServerData().getCompound("binding"));

                if (binding.getBoundEntity() != null) {
                    EntityType entityEntry = Registry.ENTITY_TYPE.get(binding.getBoundEntity());
                    if (entityEntry != null)
                        tooltip.add(new TranslatableTextComponent("tooltip.soulshards.bound", entityEntry.getTextComponent()));
                    else
                        tooltip.add(new TranslatableTextComponent("tooltip.soulshards.bound", binding.getBoundEntity().toString()).setStyle(new Style().setColor(TextFormat.RED)));
                }

                tooltip.add(new TranslatableTextComponent("tooltip.soulshards.tier", binding.getTier().getIndex()));
            }
        }, TooltipPosition.BODY, TileEntitySoulCage.class);
    }
}
