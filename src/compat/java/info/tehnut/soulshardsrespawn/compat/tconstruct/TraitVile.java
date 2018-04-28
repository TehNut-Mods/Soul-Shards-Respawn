package info.tehnut.soulshardsrespawn.compat.tconstruct;

import info.tehnut.soulshardsrespawn.SoulShards;
import info.tehnut.soulshardsrespawn.core.EventHandler;
import info.tehnut.soulshardsrespawn.core.data.Binding;
import info.tehnut.soulshardsrespawn.item.ItemSoulShard;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.FakePlayer;
import slimeknights.tconstruct.library.traits.AbstractTrait;

public class TraitVile extends AbstractTrait {

    public TraitVile() {
        super("vile", 0x7A10A7);
    }

    @Override
    public void afterHit(ItemStack tool, EntityLivingBase player, EntityLivingBase target, float damageDealt, boolean wasCritical, boolean wasHit) {
        if (!(player instanceof EntityPlayer))
            return;

        if (!SoulShards.CONFIG.allowFakePlayers() && player instanceof FakePlayer)
            return;

        if (wasHit && target.getHealth() <= 0.01) {
            ResourceLocation entityId = EntityList.getKey(target);
            ItemStack shardStack = EventHandler.getFirstShard((EntityPlayer) player, entityId);
            if (!shardStack.isEmpty()) {
                Binding binding = ((ItemSoulShard) shardStack.getItem()).getBinding(shardStack);
                if (binding == null)
                    binding = new Binding(entityId, ((EntityPlayer) player).getGameProfile().getId(), 0);

                ((ItemSoulShard) shardStack.getItem()).updateBinding(shardStack, binding.addKills(1));
            }
        }
    }
}
