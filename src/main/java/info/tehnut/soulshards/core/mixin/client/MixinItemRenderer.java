package info.tehnut.soulshards.core.mixin.client;

import com.mojang.blaze3d.systems.RenderSystem;
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

    @Inject(method = "renderGuiItemOverlay(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V", at = @At("RETURN"))
    private void renderShardFullness(TextRenderer textRenderer, ItemStack stack, int x, int y, String text, CallbackInfo ci) {
        if (!SoulShards.CONFIG.getClient().displayDurabilityBar())
            return;

        if (stack.isEmpty() || !(stack.getItem() instanceof ItemSoulShard))
            return;

        ItemSoulShard shard = (ItemSoulShard) stack.getItem();
        Binding binding = shard.getBinding(stack);

        if (binding == null || binding.getKills() >= Tier.maxKills)
            return;

        float current = (float) binding.getKills();
        float max = (float) Tier.maxKills;
        float percentage = current / max;
        int color = 0x9F63ED;

        RenderSystem.disableDepthTest();
        RenderSystem.disableTexture();
        RenderSystem.disableAlphaTest();
        RenderSystem.disableBlend();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(7, VertexFormats.POSITION_COLOR);
        prepareQuad(buffer, x + 2, y + 13, 13, 2, 0, 0, 0);
        prepareQuad(buffer, x + 2, y + 13, (int) (percentage * 13), 1, color >> 16 & 255, color >> 8 & 255, color & 255);
        Tessellator.getInstance().draw();
        RenderSystem.enableBlend();
        RenderSystem.enableAlphaTest();
        RenderSystem.enableTexture();
        RenderSystem.enableDepthTest();
    }

    private static void prepareQuad(BufferBuilder buffer, int x, int y, int width, int height, int r, int g, int b) {
        buffer.vertex(x, y, 0.0D).color(r, g, b, 255).next();
        buffer.vertex(x, y + height, 0.0D).color(r, g, b, 255).next();
        buffer.vertex(x + width, y + height, 0.0D).color(r, g, b, 255).next();
        buffer.vertex(x + width, y, 0.0D).color(r, g, b, 255).next();
    }
}
