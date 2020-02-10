package info.tehnut.soulshardsrespawn.api;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Cancelable
public class CageSpawnEvent extends Event {

    private final IBinding shardBinding;
    private final ItemStack shardStack;
    private final LivingEntity toSpawn;

    /**
     * This event is fired before a Soul Cage spawns an entity in the world. If cancelled, the entity will not be spawned.
     *
     * @param shardBinding The {@link IBinding} obtained from the Soul Shard.
     * @param shardStack The Soul Shard inserted into the Soul Cage.
     * @param toSpawn The entity which is about to be placed in the world.
     */
    public CageSpawnEvent(IBinding shardBinding, ItemStack shardStack, LivingEntity toSpawn) {
        this.shardBinding = shardBinding;
        this.shardStack = shardStack;
        this.toSpawn = toSpawn;
    }

    public IBinding getShardBinding() {
        return shardBinding;
    }

    public ItemStack getShardStack() {
        return shardStack;
    }

    public LivingEntity getToSpawn() {
        return toSpawn;
    }
}
