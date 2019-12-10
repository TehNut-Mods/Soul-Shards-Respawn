package info.tehnut.soulshards.block;

import info.tehnut.soulshards.SoulShards;
import info.tehnut.soulshards.api.CageSpawnEvent;
import info.tehnut.soulshards.core.RegistrarSoulShards;
import info.tehnut.soulshards.core.data.Binding;
import info.tehnut.soulshards.item.ItemSoulShard;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.inventory.BasicInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Tickable;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.LightType;
import net.minecraft.world.dimension.DimensionType;

public class TileEntitySoulCage extends BlockEntity implements Tickable {

    Inventory inventory;

    public TileEntitySoulCage() {
        super(RegistrarSoulShards.SOUL_CAGE_TE);

        this.inventory = new BasicInventory(1) {
            @Override
            public boolean isValidInvStack(int slot, ItemStack stack) {
                if (!(stack.getItem() instanceof ItemSoulShard))
                    return false;

                Binding binding = ((ItemSoulShard) stack.getItem()).getBinding(stack);
                return binding != null && binding.getBoundEntity() != null && SoulShards.CONFIG.getEntityList().isEnabled(binding.getBoundEntity());
            }
        };
    }

    @Override
    public void tick() {
        if (getWorld() == null || getWorld().isClient)
            return;

        TypedActionResult<Binding> result = canSpawn();
        if (result.getResult() != ActionResult.SUCCESS) {
            setState(false);
            world.updateNeighbors(pos, getCachedState().getBlock());
            return;
        }

        setState(true);
        world.updateNeighbors(pos, getCachedState().getBlock());

        if (world.getTime() % result.getValue().getTier().getCooldown() == 0)
            spawnEntities();
    }

    @Override
    public void fromTag(CompoundTag tag) {
        super.fromTag(tag);

        if (tag.contains("shard"))
            inventory.setInvStack(0, ItemStack.fromTag(tag.getCompound("shard")));
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        ItemStack shardStack = inventory.getInvStack(0);
        if (!shardStack.isEmpty())
            tag.put("shard", shardStack.toTag(new CompoundTag()));

        return super.toTag(tag);
    }

    private void spawnEntities() {
        Binding binding = getBinding();
        if (binding == null || binding.getBoundEntity() == null)
            return;

        EntityType entityType = Registry.ENTITY_TYPE.get(binding.getBoundEntity());
        spawnLoop:
        for (int i = 0; i < binding.getTier().getSpawnAmount(); i++) {
            for (int attempts = 0; attempts < 5; attempts++) {

                double x = getPos().getX() + (world.random.nextDouble() - world.random.nextDouble()) * 4.0D;
                double y = getPos().getY() + world.random.nextInt(3) - 1;
                double z = getPos().getZ() + (world.random.nextDouble() - world.random.nextDouble()) * 4.0D;
                BlockPos spawnAt = new BlockPos(x, y, z);

                LivingEntity entityLiving = (LivingEntity) entityType.create(getWorld());
                if (entityLiving == null)
                    continue;

                if (binding.getTier().checkLight() && !canSpawnInLight(entityLiving, spawnAt))
                    continue;

                entityLiving.setPositionAndAngles(spawnAt, MathHelper.wrapDegrees(world.random.nextFloat() * 360F), 0F);
                entityLiving.getDataTracker().set(SoulShards.cageBornTag, true);

                if (entityLiving.isAlive() && !hasReachedSpawnCap(entityLiving) && !isColliding(entityLiving)) {
                    if (!SoulShards.CONFIG.getBalance().allowBossSpawns() && !entityLiving.canUsePortals()) // canUsePortals -> isNonBoss
                        continue;

                    ActionResult result = CageSpawnEvent.CAGE_SPAWN.invoker().onCageSpawn(binding, inventory.getInvStack(0), entityLiving);
                    if (result == ActionResult.FAIL)
                        continue spawnLoop;

                    world.spawnEntity(entityLiving);
                    if (entityLiving instanceof MobEntity)
                        ((MobEntity) entityLiving).initialize(world, world.getLocalDifficulty(pos), SpawnType.SPAWNER, null, null);
                    break;
                }
            }
        }
    }

    private TypedActionResult<Binding> canSpawn() {
        // TODO mojang pls
//        if (!world.getServer().getWorld(DimensionType.OVERWORLD).getGameRules().getBoolean(SoulShards.allowCageSpawns))
//            return new TypedActionResult<>(ActionResult.FAIL, null);

        BlockState state = world.getBlockState(pos);
        if (state.getBlock() != RegistrarSoulShards.SOUL_CAGE)
            return new TypedActionResult<>(ActionResult.FAIL, null);

        ItemStack shardStack = inventory.getInvStack(0);
        if (shardStack.isEmpty() || !(shardStack.getItem() instanceof ItemSoulShard))
            return new TypedActionResult<>(ActionResult.FAIL, null);

        Binding binding = ((ItemSoulShard) shardStack.getItem()).getBinding(shardStack);
        if (binding == null || binding.getBoundEntity() == null)
            return new TypedActionResult<>(ActionResult.FAIL, binding);

        if (binding.getTier().getSpawnAmount() == 0)
            return new TypedActionResult<>(ActionResult.FAIL, binding);

        if (SoulShards.CONFIG.getBalance().requireOwnerOnline() && !ownerOnline())
            return new TypedActionResult<>(ActionResult.FAIL, binding);

        if (!SoulShards.CONFIG.getEntityList().isEnabled(binding.getBoundEntity()))
            return new TypedActionResult<>(ActionResult.FAIL, binding);

        if (!SoulShards.CONFIG.getBalance().requireRedstoneSignal()) {
            if (state.get(BlockSoulCage.POWERED) && binding.getTier().checkRedstone())
                return new TypedActionResult<>(ActionResult.FAIL, binding);
        } else if (!state.get(BlockSoulCage.POWERED))
            return new TypedActionResult<>(ActionResult.FAIL, binding);

        if (binding.getTier().checkPlayer() && world.getClosestPlayer(getPos().getX(), getPos().getY(), getPos().getZ(), 16, false) == null)
            return new TypedActionResult<>(ActionResult.FAIL, binding);

        return new TypedActionResult<>(ActionResult.SUCCESS, binding);
    }

    public Binding getBinding() {
        ItemStack stack = inventory.getInvStack(0);
        if (stack.isEmpty() || !(stack.getItem() instanceof ItemSoulShard))
            return null;

        return ((ItemSoulShard) stack.getItem()).getBinding(stack);
    }

    private boolean canSpawnInLight(LivingEntity entityLiving, BlockPos pos) {
        return !(entityLiving instanceof Monster) || world.getLightLevel(LightType.BLOCK, pos) <= 8;
    }

    private boolean hasReachedSpawnCap(LivingEntity living) {
        Box box = new Box(getPos().getX() - 16, getPos().getY() - 16, getPos().getZ() - 16, getPos().getX() + 16, getPos().getY() + 16, getPos().getZ() + 16);

        int mobCount = world.getEntities(living.getClass(), box, e -> e != null && e.getDataTracker().get(SoulShards.cageBornTag)).size();
        return mobCount >= SoulShards.CONFIG.getBalance().getSpawnCap();
    }

    private boolean isColliding(LivingEntity entity) {
        return world.isAreaNotEmpty(entity.getBoundingBox()) && world.getEntities(LivingEntity.class, entity.getBoundingBox(), e -> true).isEmpty();
    }

    public void setState(boolean active) {
        BlockState state = getCachedState();
        if (!(state.getBlock() instanceof BlockSoulCage))
            return;

        world.setBlockState(getPos(), state.with(BlockSoulCage.ACTIVE, active));
    }

    public boolean ownerOnline() {
        Binding binding = getBinding();
        //noinspection ConstantConditions
        return binding != null && binding.getOwner() != null && world.getServer().getPlayerManager().getPlayer(binding.getOwner()) == null;
    }

    public Inventory getInventory() {
        return inventory;
    }
}
