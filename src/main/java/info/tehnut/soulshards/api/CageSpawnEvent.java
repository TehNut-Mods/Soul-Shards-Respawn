package info.tehnut.soulshards.api;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;

import java.util.ArrayList;

public interface CageSpawnEvent {

    ArrayList<CageSpawnEvent> CAGE_SPAWN = new ArrayList<>();

    ActionResult onCageSpawn(IBinding binding, ItemStack shardStack, LivingEntity toSpawn);
}
