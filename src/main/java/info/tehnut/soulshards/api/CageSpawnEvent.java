package info.tehnut.soulshards.api;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;

public interface CageSpawnEvent {

    public static final Event<CageSpawnEvent> CAGE_SPAWN = EventFactory.createArrayBacked(CageSpawnEvent.class,
            (listeners) -> (binding, shardStack, toSpawn) -> {
                for(CageSpawnEvent event : listeners) {
                    ActionResult result = event.onCageSpawn(binding, shardStack, toSpawn);
                    if(result != ActionResult.PASS) {
                        return result;
                    }
                }

                return ActionResult.PASS;
            }
            );

    ActionResult onCageSpawn(IBinding binding, ItemStack shardStack, LivingEntity toSpawn);
}
