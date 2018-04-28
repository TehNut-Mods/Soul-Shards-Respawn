package info.tehnut.soulshardsrespawn.core.util;

import com.google.gson.JsonObject;
import info.tehnut.soulshardsrespawn.SoulShards;
import net.minecraftforge.common.crafting.IConditionFactory;
import net.minecraftforge.common.crafting.JsonContext;

import java.util.function.BooleanSupplier;

public class ConditionResetEnabled implements IConditionFactory {

    @Override
    public BooleanSupplier parse(JsonContext context, JsonObject json) {
        return () -> SoulShards.CONFIG.addShardResetRecipe();
    }
}
