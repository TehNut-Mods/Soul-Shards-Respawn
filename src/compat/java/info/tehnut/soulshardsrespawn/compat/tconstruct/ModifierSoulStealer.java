package info.tehnut.soulshardsrespawn.compat.tconstruct;

import com.google.common.collect.ImmutableList;
import info.tehnut.soulshardsrespawn.SoulShards;
import info.tehnut.soulshardsrespawn.core.EventHandler;
import info.tehnut.soulshardsrespawn.core.data.Binding;
import info.tehnut.soulshardsrespawn.item.ItemSoulShard;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.FakePlayer;
import slimeknights.tconstruct.library.Util;
import slimeknights.tconstruct.library.modifiers.ModifierAspect;
import slimeknights.tconstruct.library.modifiers.ModifierNBT;
import slimeknights.tconstruct.library.modifiers.ModifierTrait;
import slimeknights.tconstruct.library.utils.TinkerUtil;

import java.util.List;

public class ModifierSoulStealer extends ModifierTrait {

    public ModifierSoulStealer() {
        super("soul_stealer", 0x7A10A7, 5, 0);

        addAspects(ModifierAspect.weaponOnly);
    }

    @Override
    public void afterHit(ItemStack tool, EntityLivingBase player, EntityLivingBase target, float damageDealt, boolean wasCritical, boolean wasHit) {
        if (!(player instanceof EntityPlayer))
            return;

        if (!SoulShards.config.allowFakePlayers() && player instanceof FakePlayer)
            return;

        if (wasHit && target.getHealth() <= 0.01) {
            ResourceLocation entityId = EntityList.getKey(target);
            ItemStack shardStack = EventHandler.getFirstShard((EntityPlayer) player, entityId);
            if (!shardStack.isEmpty()) {
                ModifierNBT data = new ModifierNBT(TinkerUtil.getModifierTag(tool, getModifierIdentifier()));
                Binding binding = ((ItemSoulShard) shardStack.getItem()).getBinding(shardStack);
                if (binding == null)
                    binding = new Binding(entityId, ((EntityPlayer) player).getGameProfile().getId(), 0);

                ((ItemSoulShard) shardStack.getItem()).updateBinding(shardStack, binding.addKills(data.level));
            }
        }
    }

    @Override
    public boolean canApplyCustom(ItemStack stack) {
        return super.canApplyCustom(stack);
    }

    @Override
    public List<String> getExtraInfo(ItemStack tool, NBTTagCompound modifierTag) {
        String loc = String.format(LOC_Extra, getModifierIdentifier());
        float amount = new ModifierNBT(modifierTag).level;
        return ImmutableList.of(Util.translateFormatted(loc, Util.df.format(amount)));
    }
}
