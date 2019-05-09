package info.tehnut.soulshards.core.mixin.client;

import com.mojang.blaze3d.platform.GlStateManager;
import info.tehnut.soulshards.SoulShards;
import info.tehnut.soulshards.core.data.Binding;
import info.tehnut.soulshards.core.data.Tier;
import info.tehnut.soulshards.item.ItemSoulShard;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public abstract class MixinItemRenderer {

    @Inject(method = "renderGuiItemOverlay(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;II)V", at = @At("RETURN"))
    private void renderShardFullness(TextRenderer textRenderer, ItemStack stack, int x, int y, CallbackInfo ci) {
        if (!SoulShards.CONFIG.getClient().displayDurabilityBar())
            return;

        if (stack.isEmpty() || !(stack.getItem() instanceof ItemSoulShard))
            return;

        ItemSoulShard shard = (ItemSoulShard) stack.getItem();
        Binding binding = shard.getBinding(stack);

        if (binding == null || binding.getKills() >= Tier.maxKills)
            return;

        GlStateManager.disableLighting();
        GlStateManager.disableDepthTest();
        GlStateManager.disableTexture();
        GlStateManager.disableAlphaTest();
        GlStateManager.disableBlend();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBufferBuilder();
        float current = (float) binding.getKills();
        float max = (float) Tier.maxKills;
        float percentage = current / max;
        int color = 0x9F63ED;
        buffer.begin(7, VertexFormats.POSITION_COLOR);
        prepareQuad(buffer, x + 2, y + 13, 13, 2, 0, 0, 0, 255);
        prepareQuad(buffer, x + 2, y + 13, (int) (percentage * 13), 1, color >> 16 & 255, color >> 8 & 255, color & 255, 255);
        Tessellator.getInstance().draw();
        GlStateManager.enableBlend();
        GlStateManager.enableAlphaTest();
        GlStateManager.enableTexture();
        GlStateManager.enableLighting();
        GlStateManager.enableDepthTest();
    }

    private static void prepareQuad(BufferBuilder buffer, int x, int y, int width, int height, int r, int g, int b, int alpha) {
        buffer.vertex((double) (x), (double) (y), 0.0D).color(r, g, b, alpha).next();
        buffer.vertex((double) (x), (double) (y + height), 0.0D).color(r, g, b, alpha).next();
        buffer.vertex((double) (x + width), (double) (y + height), 0.0D).color(r, g, b, alpha).next();
        buffer.vertex((double) (x + width), (double) (y), 0.0D).color(r, g, b, alpha).next();
    }
}
