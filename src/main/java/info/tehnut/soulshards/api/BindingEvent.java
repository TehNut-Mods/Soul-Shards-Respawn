package info.tehnut.soulshards.api;

import net.fabricmc.fabric.util.HandlerList;
import net.fabricmc.fabric.util.HandlerRegistry;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;

public class BindingEvent {

    public static final HandlerRegistry<NewBinding> NEW_BINDING = new HandlerList<>();
    public static final HandlerRegistry<GainSouls> GAIN_SOULS = new HandlerList<>();
    public static final HandlerRegistry<GetEntityName> GET_ENTITY_NAME = new HandlerList<>();

    public interface NewBinding {
        TypedActionResult<IBinding> onNewBinding(LivingEntity entity, IBinding binding);
    }

    public interface GainSouls {
        int getGainedSouls(LivingEntity entity, IBinding binding, int amount);
    }

    public interface GetEntityName {
        Identifier getEntityName(LivingEntity entity, Identifier currentName);
    }
}
