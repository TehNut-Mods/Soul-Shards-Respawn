package info.tehnut.soulshards.core.mixin;

import net.minecraft.world.MobSpawnerEntry;
import net.minecraft.world.MobSpawnerLogic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MobSpawnerLogic.class)
public interface MobSpawnerLogicEntityId {
    @Accessor("spawnEntry")
    MobSpawnerEntry getSpawnEntry();
}
