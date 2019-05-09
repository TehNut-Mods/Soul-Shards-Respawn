package info.tehnut.soulshards.api;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;

public class BindingEvent {

    public static final Event<NewBinding> NEW_BINDINGS = EventFactory.createArrayBacked(NewBinding.class,
            (listeners) -> (entity, binding) -> {
                for(NewBinding newBinding : listeners) {
                    TypedActionResult<IBinding> currentResult = newBinding.onNewBinding(entity, binding);

                    if(currentResult.getResult() != ActionResult.PASS) {
                        return currentResult;
                    }
                }

                return new TypedActionResult<>(ActionResult.PASS, binding);
            }
	);

    public static final Event<GainSouls> GAIN_SOULS = EventFactory.createArrayBacked(GainSouls.class,
            (listeners) -> (entity, binding, amount) -> {
                int soulsGained = amount;

                for (GainSouls gainSouls : listeners) {
                    int newSoulsGained = gainSouls.getGainedSouls(entity, binding, amount);
                    soulsGained+=newSoulsGained;
                }

                return soulsGained;
            }
            );

    public static final Event<GetEntityName> GET_ENTITY_ID = EventFactory.createArrayBacked(GetEntityName.class,
            (listeners) -> (entity, currentName) -> {

                for(GetEntityName getEntityName : listeners) {
                    Identifier identifier = getEntityName.getEntityName(entity, currentName);
                    if(identifier != null) return identifier;
                }

                return currentName;
            });

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
