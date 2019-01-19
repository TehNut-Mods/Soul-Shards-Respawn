package info.tehnut.soulshards.api;

import net.fabricmc.fabric.util.HandlerArray;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;

public class BindingEvent {

    public static final HandlerArray<NewBinding> NEW_BINDING = new HandlerArray<>(NewBinding.class);
    public static final HandlerArray<GainSouls> GAIN_SOULS = new HandlerArray<>(GainSouls.class);
    public static final HandlerArray<GetEntityName> GET_ENTITY_NAME = new HandlerArray<>(GetEntityName.class);

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
