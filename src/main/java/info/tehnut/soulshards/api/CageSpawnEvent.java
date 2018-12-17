package info.tehnut.soulshards.api;

import net.fabricmc.fabric.util.HandlerArray;
import net.fabricmc.fabric.util.HandlerRegistry;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;

public interface CageSpawnEvent {

    HandlerRegistry<CageSpawnEvent> CAGE_SPAWN = new HandlerArray<>(CageSpawnEvent.class);

    ActionResult onCageSpawn(IBinding binding, ItemStack shardStack, LivingEntity toSpawn);
}
