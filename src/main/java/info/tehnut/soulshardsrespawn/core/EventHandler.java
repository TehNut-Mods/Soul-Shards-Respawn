package info.tehnut.soulshardsrespawn.core;

import info.tehnut.soulshardsrespawn.SoulShards;
import info.tehnut.soulshardsrespawn.api.BindingEvent;
import info.tehnut.soulshardsrespawn.api.ISoulWeapon;
import info.tehnut.soulshardsrespawn.core.data.Binding;
import info.tehnut.soulshardsrespawn.core.data.MultiblockPattern;
import info.tehnut.soulshardsrespawn.core.data.Tier;
import info.tehnut.soulshardsrespawn.item.ItemSoulShard;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import java.util.Set;

@Mod.EventBusSubscriber(modid = SoulShards.MODID)
public class EventHandler {

    @SubscribeEvent
    public static void onEntityKill(LivingDeathEvent event) {
        if (event.getEntityLiving() instanceof PlayerEntity)
            return;

        if (!SoulShards.CONFIG.getBalance().allowFakePlayers() && event.getSource().getTrueSource() instanceof FakePlayer)
            return;

        if (!SoulShards.CONFIG.getEntityList().isEnabled(event.getEntityLiving().getType().getRegistryName()))
            return;

        if (!SoulShards.CONFIG.getBalance().allowBossSpawns() && !event.getEntityLiving().isNonBoss())
            return;

        if (!SoulShards.CONFIG.getBalance().countCageBornForShard() && event.getEntityLiving().getPersistentData().getBoolean("cageBorn"))
            return;

        if (event.getSource().getTrueSource() instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) event.getSource().getTrueSource();

            BindingEvent.GetEntityName getEntityName = new BindingEvent.GetEntityName(event.getEntityLiving());
            MinecraftForge.EVENT_BUS.post(getEntityName);
            ResourceLocation entityId = getEntityName.getEntityId() == null ?  ForgeRegistries.ENTITIES.getKey(event.getEntityLiving().getType()) : getEntityName.getEntityId();

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
                    shardItem = shardItem.split(1);
                    newItem = true;
                }

                binding = (Binding) newBinding.getBinding();
            }

            ItemStack mainHand = player.getHeldItem(Hand.MAIN_HAND);

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
        MultiblockPattern pattern = ConfigSoulShards.getMultiblock();
        ItemStack held = event.getPlayer().getHeldItem(event.getHand());
        if (!ItemStack.areItemsEqual(held, pattern.getCatalyst()))
            return;

        BlockState state = event.getWorld().getBlockState(event.getPos());
        if (!pattern.isOriginBlock(state))
            return;

        ActionResult<Set<BlockPos>> matched = pattern.match(event.getWorld(), event.getPos());
        if (matched.getType() != ActionResultType.SUCCESS)
            return;

        for (BlockPos pos : matched.getResult())
            event.getWorld().destroyBlock(pos, false);

        held.shrink(1);
        ItemHandlerHelper.giveItemToPlayer(event.getPlayer(), new ItemStack(RegistrarSoulShards.SOUL_SHARD));
    }

    @SubscribeEvent
    public static void onAnvil(AnvilUpdateEvent event) {
        if (!SoulShards.CONFIG.getBalance().allowShardCombination())
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
        if (!SoulShards.CONFIG.getBalance().shouldDropExperience() && event.getEntityLiving().getPersistentData().getBoolean("cageBorn"))
            event.setCanceled(true);
    }

    @Nonnull
    public static ItemStack getFirstShard(PlayerEntity player, ResourceLocation entityId) {
        // Checks the offhand first
        ItemStack shardItem = player.getHeldItem(Hand.OFF_HAND);
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
