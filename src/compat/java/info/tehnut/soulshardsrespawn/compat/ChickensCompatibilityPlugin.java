package info.tehnut.soulshardsrespawn.compat;

import info.tehnut.soulshardsrespawn.SoulShards;
import info.tehnut.soulshardsrespawn.api.BindingEvent;
import info.tehnut.soulshardsrespawn.core.data.Binding;
import info.tehnut.soulshardsrespawn.core.util.compat.CompatibilityPlugin;
import info.tehnut.soulshardsrespawn.core.util.compat.ICompatibilityPlugin;
import info.tehnut.soulshardsrespawn.item.ItemSoulShard;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

@CompatibilityPlugin("chickens")
public class ChickensCompatibilityPlugin implements ICompatibilityPlugin {

    private static final Class<? extends EntityChicken> CHICKEN_CLASS;
    private static final DataParameter<?> TYPE_PARAMETER;
    private static final Map<ResourceLocation, Object> CHICKEN_REGISTRY;
    private static final MethodHandle NAME_GETTER;
    static {
        try {
            SoulShards.LOGGER.info("[ChickensCompat] Beginning some spicy hacks to let shards know there's different kinds of Chickens chickens \uD83D\uDC40");
            CHICKEN_CLASS = (Class<? extends EntityChicken>) Class.forName("com.setycz.chickens.entity.EntityChickensChicken");
            Field typeParameterField = CHICKEN_CLASS.getDeclaredField("CHICKEN_TYPE");
            typeParameterField.setAccessible(true);
            TYPE_PARAMETER = (DataParameter<?>) typeParameterField.get(null);

            Class<?> chickenRegistryClass = Class.forName("com.setycz.chickens.registry.ChickensRegistry");
            Field registryField = chickenRegistryClass.getDeclaredField("items");
            registryField.setAccessible(true);
            CHICKEN_REGISTRY = (Map<ResourceLocation, Object>) registryField.get(null);

            Class<?> chickenRegistryItemClass = Class.forName("com.setycz.chickens.registry.ChickensRegistryItem");
            Method entityNameMethod = chickenRegistryItemClass.getMethod("getEntityName");
            NAME_GETTER = MethodHandles.lookup().unreflect(entityNameMethod);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get required internals of Chickens mod even though it's loaded.", e);
        }
    }

    private boolean enabled;

    @Override
    public void preInit() {
        if (enabled)
            MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void handleConfiguration(Configuration configuration) {
        this.enabled = configuration.getBoolean("differentiateBetweenChickens", "compat.chickens", true, "Runs some horribly hacky code to differentiate between the different chicken types.");
    }

    @SubscribeEvent
    public void getEntityName(BindingEvent.GetEntityName event) {
        if (event.getEntity().getClass() == CHICKEN_CLASS) {
            String chickenType = (String) event.getEntity().getDataManager().get(TYPE_PARAMETER);
            event.setEntityId(new ResourceLocation(chickenType));
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onItemTooltip(ItemTooltipEvent event) {
        if (event.getItemStack().getItem() instanceof ItemSoulShard) {
            Binding binding = Binding.fromNBT(event.getItemStack());
            if (binding == null || binding.getBoundEntity() == null)
                return;

            if (!binding.getBoundEntity().getResourceDomain().equals("chickens"))
                return;

            try {
                Object chickenThing = CHICKEN_REGISTRY.get(binding.getBoundEntity());
                if (chickenThing == null)
                    event.getToolTip().add(1, I18n.format("tooltip.soulshardsrespawn.bound", binding.getBoundEntity().toString()));
                else
                    event.getToolTip().add(1, I18n.format("tooltip.soulshardsrespawn.bound", I18n.format("entity." + NAME_GETTER.bindTo(chickenThing).invoke() + ".name")));
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }
}
