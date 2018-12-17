package info.tehnut.soulshards.api;

import net.fabricmc.fabric.util.HandlerList;
import net.fabricmc.fabric.util.HandlerRegistry;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;

public interface CageSpawnEvent {

    HandlerRegistry<CageSpawnEvent> CAGE_SPAWN = new HandlerList<>();

    ActionResult onCageSpawn(IBinding binding, ItemStack shardStack, LivingEntity toSpawn);
}
