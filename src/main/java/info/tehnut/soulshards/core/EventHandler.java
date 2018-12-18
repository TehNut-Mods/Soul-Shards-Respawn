package info.tehnut.soulshards.core;

import info.tehnut.soulshards.SoulShards;
import info.tehnut.soulshards.api.BindingEvent;
import info.tehnut.soulshards.api.IBinding;
import info.tehnut.soulshards.api.ISoulWeapon;
import info.tehnut.soulshards.core.data.Binding;
import info.tehnut.soulshards.core.data.MultiblockPattern;
import info.tehnut.soulshards.core.data.Tier;
import info.tehnut.soulshards.item.ItemSoulShard;
import net.fabricmc.fabric.events.PlayerInteractionEvent;
import net.fabricmc.fabric.util.HandlerArray;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sortme.ItemScatterer;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;

import java.util.Set;

public class EventHandler {

    public static void init() {
        PlayerInteractionEvent.INTERACT_BLOCK.register((player, world, hand, pos, facing, hitX, hitY, hitZ) -> {
            MultiblockPattern pattern = ConfigSoulShards.getMultiblock();

            ItemStack held = player.getStackInHand(hand);
            if (!ItemStack.areEqualIgnoreTags(pattern.getCatalyst(), held))
                return ActionResult.PASS;

            BlockState worldState = world.getBlockState(pos);
            if (!pattern.isOriginBlock(worldState))
                return ActionResult.PASS;

            TypedActionResult<Set<BlockPos>> match = pattern.match(world, pos);
            if (match.getResult() == ActionResult.FAILURE)
                return match.getResult();

            match.getValue().forEach(matchedPos -> world.breakBlock(matchedPos, false));
            held.subtractAmount(1);
            ItemStack shardStack = new ItemStack(RegistrarSoulShards.SOUL_SHARD);
            if (!player.inventory.insertStack(shardStack))
                ItemScatterer.spawn(world, player.x, player.y, player.z, shardStack);
            return ActionResult.SUCCESS;
        });
    }

    public static void onEntityDeath(LivingEntity killed, DamageSource source) {
        // Using canUsePortals because it appears to be MCP's isNonBoss().
        // Only returns false for Wither and Ender Dragon
        if (!SoulShards.CONFIG.getBalance().allowBossSpawns() && !killed.canUsePortals())
            return;

        if (!SoulShards.CONFIG.getBalance().countCageBornForShard() && killed.getDataTracker().get(SoulShards.cageBornTag))
            return;

        if (source.getAttacker() instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) source.getAttacker();
            Identifier entityId = getEntityId(killed);

            if (!SoulShards.CONFIG.getEntityList().isEnabled(entityId))
                return;

            ItemStack shardStack = getFirstShard(player, entityId);
            if (shardStack.isEmpty())
                return;

            ItemSoulShard shard = (ItemSoulShard) shardStack.getItem();
            Binding binding = shard.getBinding(shardStack);
            if (binding == null)
                binding = getNewBinding(killed);

            if (binding == null)
                return;

            ItemStack mainHand = player.getStackInHand(Hand.MAIN);
            int soulsGained = 1 + EnchantmentHelper.getLevel(RegistrarSoulShards.SOUL_STEALER, mainHand);
            if (mainHand.getItem() instanceof ISoulWeapon)
                soulsGained += ((ISoulWeapon) mainHand.getItem()).getSoulBonus(mainHand, player, killed);

            Object[] subscribers = ((HandlerArray<BindingEvent.GainSouls>) BindingEvent.GAIN_SOULS).getBackingArray();
            for (Object subscriber : subscribers)
                soulsGained = ((BindingEvent.GainSouls) subscriber).getGainedSouls(killed, binding, soulsGained);

            if (binding.getBoundEntity() == null)
                binding.setBoundEntity(entityId);

            if (binding.getOwner() == null)
                binding.setOwner(player.getGameProfile().getId());

            shard.updateBinding(shardStack, binding.addKills(soulsGained));
        }
    }

    private static ItemStack getFirstShard(PlayerEntity player, Identifier entityId) {
        // Checks the offhand first
        ItemStack shardItem = player.getStackInHand(Hand.OFF);
        // If offhand isn't a shard, loop through the hotbar
        if (shardItem.isEmpty() || !(shardItem.getItem() instanceof ItemSoulShard)) {
            for (int i = 0; i < 9; i++) {
                shardItem = player.inventory.getInvStack(i);
                if (!shardItem.isEmpty() && shardItem.getItem() instanceof ItemSoulShard) {
                    if (checkBinding(entityId, shardItem)) return shardItem;
                }
            }
        } else { // If offhand is a shard, check it it
            if (checkBinding(entityId, shardItem))
                return shardItem;
        }

        return ItemStack.EMPTY; // No shard found
    }

    private static boolean checkBinding(Identifier entityId, ItemStack shardItem) {
        Binding binding = ((ItemSoulShard) shardItem.getItem()).getBinding(shardItem);

        // If there's no binding or no bound entity, this is a valid shard
        if (binding == null || binding.getBoundEntity() == null)
            return true;

        // If there is a bound entity and we're less than the max kills, this is a valid shard
        return binding.getBoundEntity().equals(entityId) && binding.getKills() < Tier.maxKills;

    }

    private static Identifier getEntityId(LivingEntity entity) {
        Identifier id = Registry.ENTITY_TYPE.getId(entity.getType());
        Object[] subscribers = ((HandlerArray<BindingEvent.GetEntityName>) BindingEvent.GET_ENTITY_NAME).getBackingArray();
        for (Object subscriber : subscribers) {
            Identifier newId = ((BindingEvent.GetEntityName) subscriber).getEntityName(entity, id);
            if (newId != null)
                id = newId;
        }

        return id;
    }

    private static Binding getNewBinding(LivingEntity entity) {
        Binding binding = new Binding(null, 0);
        Object[] subscribers = ((HandlerArray<BindingEvent.NewBinding>) BindingEvent.NEW_BINDING).getBackingArray();
        for (Object subscriber : subscribers) {
            TypedActionResult<IBinding> result = ((BindingEvent.NewBinding) subscriber).onNewBinding(entity, binding);
            if (result.getResult() == ActionResult.FAILURE)
                return null;

            if (result.getValue() != null)
                binding = (Binding) result.getValue();
        }

        return binding;
    }
}
