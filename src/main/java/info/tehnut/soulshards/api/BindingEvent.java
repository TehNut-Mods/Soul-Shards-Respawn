package info.tehnut.soulshards.api;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;

import java.util.ArrayList;

public class BindingEvent {

    public static final ArrayList<NewBinding> NEW_BINDING = new ArrayList<>();
    public static final ArrayList<GainSouls> GAIN_SOULS = new ArrayList<>();
    public static final ArrayList<GetEntityName> GET_ENTITY_NAME = new ArrayList<>();

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
