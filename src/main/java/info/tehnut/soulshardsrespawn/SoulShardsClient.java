package info.tehnut.soulshardsrespawn;

import info.tehnut.soulshardsrespawn.core.RegistrarSoulShards;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;

public class SoulShardsClient {

    public static void initClient() {
        RenderTypeLookup.setRenderLayer(RegistrarSoulShards.SOUL_CAGE, RenderType.getCutout());
    }
}
