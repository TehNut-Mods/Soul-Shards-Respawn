package info.tehnut.soulshards;

import info.tehnut.soulshards.core.RegistrarSoulShards;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.render.RenderLayer;

public class SoulShardsClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        BlockRenderLayerMap.INSTANCE.putBlock(RegistrarSoulShards.SOUL_CAGE, RenderLayer.getCutout());
    }
}
