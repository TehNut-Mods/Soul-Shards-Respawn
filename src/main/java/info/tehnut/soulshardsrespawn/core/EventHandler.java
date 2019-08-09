package info.tehnut.soulshardsrespawn.core;

import info.tehnut.soulshardsrespawn.SoulShards;
import info.tehnut.soulshardsrespawn.api.BindingEvent;
import info.tehnut.soulshardsrespawn.api.ISoulWeapon;
import info.tehnut.soulshardsrespawn.core.data.Binding;
import info.tehnut.soulshardsrespawn.core.data.MultiblockPattern;
import info.tehnut.soulshardsrespawn.core.data.Tier;
import info.tehnut.soulshardsrespawn.item.ItemSoulShard;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import java.util.Set;

@Mod.EventBusSubscriber(modid = SoulShards.MODID)
public class EventHandler {

    @SubscribeEvent
    public static void onEntityKill(LivingDeathEvent event) {
        if (event.getEntityLiving() instanceof EntityPlayer)
            return;

        if (!SoulShards.CONFIG.allowFakePlayers() && event.getSource().getTrueSource() instanceof FakePlayer)
            return;

        if (!SoulShards.CONFIG.isEntityEnabled(event.getEntityLiving().getClass()))
            return;

        if (!SoulShards.CONFIG.allowBossSpawns() && !event.getEntityLiving().isNonBoss())
            return;

        if (!SoulShards.CONFIG.countCageBornForShard() && event.getEntityLiving().getEntityData().getBoolean("cageBorn"))
            return;

        if (event.getSource().getTrueSource() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) event.getSource().getTrueSource();
            if (player instanceof FakePlayer && !SoulShards.CONFIG.allowFakePlayers())
                return;

            BindingEvent.GetEntityName getEntityName = new BindingEvent.GetEntityName(event.getEntityLiving());
            MinecraftForge.EVENT_BUS.post(getEntityName);
            ResourceLocation entityId = getEntityName.getEntityId() == null ?  EntityList.getKey(event.getEntityLiving()) : getEntityName.getEntityId();

            ItemStack shardItem = getFirstShard(player, entityId);
            if (shardItem.isEmpty())
                return;
            ItemSoulShard soulShard = (ItemSoulShard) shardItem.getItem();

            boolean newItem = false;
            Binding binding = soulShard.getBinding(shardItem);
            if (binding == null) {
                BindingEvent.NewBinding newBinding = new BindingEvent.NewBinding(event.getEntityLiving(), new Binding(null, 0));
                if (MinecraftForge.EVENT_BUS.post(newBinding))
                    return;

                if (shardItem.getCount() > 1) { // Peel off one blank shard from a stack of them
                    shardItem = shardItem.splitStack(1);
                    newItem = true;
                }

                binding = (Binding) newBinding.getBinding();
            }

            ItemStack mainHand = player.getHeldItem(EnumHand.MAIN_HAND);

            // Base of 1 plus enchantment bonus
            int soulsGained = 1 + EnchantmentHelper.getEnchantmentLevel(RegistrarSoulShards.SOUL_STEALER, mainHand);
            if (mainHand.getItem() instanceof ISoulWeapon)
                soulsGained += ((ISoulWeapon) mainHand.getItem()).getSoulBonus(mainHand, player, event.getEntityLiving());

            BindingEvent.GainSouls gainSouls = new BindingEvent.GainSouls(event.getEntityLiving(), binding, soulsGained);
            MinecraftForge.EVENT_BUS.post(gainSouls);

            if (binding.getBoundEntity() == null)
                binding.setBoundEntity(entityId);

            if (binding.getOwner() == null)
                binding.setOwner(player.getGameProfile().getId());

            soulShard.updateBinding(shardItem, binding.addKills(gainSouls.getAmount()));
            if (newItem) // Give the player the peeled off stack
                ItemHandlerHelper.giveItemToPlayer(player, shardItem);
        }
    }

    @SubscribeEvent
    public static void onBlockInteract(PlayerInteractEvent.RightClickBlock event) {
        MultiblockPattern pattern = SoulShards.CONFIG.getMultiblockPattern();
        ItemStack held = event.getEntityPlayer().getHeldItem(event.getHand());
        if (!ItemStack.areItemsEqual(held, pattern.getCatalyst()))
            return;

        IBlockState state = event.getWorld().getBlockState(event.getPos());
        if (!pattern.isOriginBlock(state))
            return;

        ActionResult<Set<BlockPos>> matched = pattern.match(event.getWorld(), event.getPos());
        if (matched.getType() != EnumActionResult.SUCCESS)
            return;

        for (BlockPos pos : matched.getResult())
            event.getWorld().destroyBlock(pos, false);

        held.shrink(1);
        ItemHandlerHelper.giveItemToPlayer(event.getEntityPlayer(), new ItemStack(RegistrarSoulShards.SOUL_SHARD));
    }

    @SubscribeEvent
    public static void onAnvil(AnvilUpdateEvent event) {
        if (!SoulShards.CONFIG.allowShardCombination())
            return;

        if (event.getLeft().getItem() instanceof ItemSoulShard && event.getRight().getItem() instanceof ItemSoulShard) {
            Binding left = ((ItemSoulShard) event.getLeft().getItem()).getBinding(event.getLeft());
            Binding right = ((ItemSoulShard) event.getRight().getItem()).getBinding(event.getRight());

            if (left == null || right == null)
                return;

            if (left.getBoundEntity() != null && left.getBoundEntity().equals(right.getBoundEntity())) {
                ItemStack output = new ItemStack(RegistrarSoulShards.SOUL_SHARD);
                ((ItemSoulShard) output.getItem()).updateBinding(output, left.addKills(right.getKills()));
                event.setOutput(output);
                event.setCost(left.getTier().getIndex() * 6);
            }
        }
    }

    @SubscribeEvent
    public static void dropExperience(LivingExperienceDropEvent event) {
        if (!SoulShards.CONFIG.dropExperience() && event.getEntityLiving().getEntityData().getBoolean("cageBorn"))
            event.setCanceled(true);
    }

    @Nonnull
    public static ItemStack getFirstShard(EntityPlayer player, ResourceLocation entityId) {
        // Checks the offhand first
        ItemStack shardItem = player.getHeldItem(EnumHand.OFF_HAND);
        // If offhand isn't a shard, loop through the hotbar
        if (shardItem.isEmpty() || !(shardItem.getItem() instanceof ItemSoulShard)) {
            for (int i = 0; i < 9; i++) {
                shardItem = player.inventory.getStackInSlot(i);
                if (!shardItem.isEmpty() && shardItem.getItem() instanceof ItemSoulShard) {
                    Binding binding = ((ItemSoulShard) shardItem.getItem()).getBinding(shardItem);

                    // If there's no binding or no bound entity, this is a valid shard
                    if (binding == null || binding.getBoundEntity() == null)
                        return shardItem;

                    // If there is a bound entity and we're less than the max kills, this is a valid shard
                    if (binding.getBoundEntity().equals(entityId) && binding.getKills() < Tier.maxKills)
                        return shardItem;
                }
            }
        } else { // If offhand is a shard, check it it
            Binding binding = ((ItemSoulShard) shardItem.getItem()).getBinding(shardItem);

            // If there's no binding or no bound entity, this is a valid shard
            if (binding == null || binding.getBoundEntity() == null)
                return shardItem;

            // If there is a bound entity and we're less than the max kills, this is a valid shard
            if (binding.getBoundEntity().equals(entityId) && binding.getKills() < Tier.maxKills)
                return shardItem;
        }

        return ItemStack.EMPTY; // No shard found
    }
}
