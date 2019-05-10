package info.tehnut.soulshards.core.mixin;

import com.sun.istack.internal.Nullable;
import net.minecraft.util.Identifier;
import net.minecraft.world.MobSpawnerLogic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MobSpawnerLogic.class)
public interface MobSpawnerLogicEntityId
{
    @Nullable
    @Invoker("getEntityId")
    Identifier getEntityId();
}
