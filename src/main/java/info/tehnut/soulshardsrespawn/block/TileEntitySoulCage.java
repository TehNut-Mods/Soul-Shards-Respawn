package info.tehnut.soulshardsrespawn.block;

import info.tehnut.soulshardsrespawn.SoulShards;
import info.tehnut.soulshardsrespawn.api.CageSpawnEvent;
import info.tehnut.soulshardsrespawn.api.IShardTier;
import info.tehnut.soulshardsrespawn.core.RegistrarSoulShards;
import info.tehnut.soulshardsrespawn.core.data.Binding;
import info.tehnut.soulshardsrespawn.item.ItemSoulShard;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.monster.IMob;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TileEntitySoulCage extends TileEntity implements ITickableTileEntity {

    private ItemStackHandler inventory;
    private int activeTime;
    private boolean active = false;

    public TileEntitySoulCage() {
        super(RegistrarSoulShards.SOUL_CAGE_TE);

        this.inventory = new SoulCageInventory();
    }

    @Override
    public void tick() {
        if (world.isRemote)
            return;

        ActionResult<Binding> result = canSpawn();
        if (result.getType() != ActionResultType.SUCCESS) {
            if (active) {
                setState(false);
                world.notifyNeighborsOfStateChange(pos, getBlockState().getBlock());
            }
            return;
        }

        if (!active) {
            setState(true);
            world.notifyNeighborsOfStateChange(pos, getBlockState().getBlock());
        }
        activeTime++;
        BlockState state = getWorld().getBlockState(getPos());
        getWorld().notifyBlockUpdate(getPos(), state, state, 3);

        if (activeTime % result.getResult().getTier().getCooldown() == 0)
            spawnEntities();
    }

    private void spawnEntities() {
        Binding binding = getBinding();
        if (binding == null || binding.getBoundEntity() == null)
            return;

        EntityType<?> entityEntry = ForgeRegistries.ENTITIES.getValue(binding.getBoundEntity());
        if (entityEntry == null)
            return;

        IShardTier tier = binding.getTier();
        for (int i = 0; i < tier.getSpawnAmount(); i++) {
            for (int attempts = 0; attempts < 5; attempts++) {
                double x = getPos().getX() + (getWorld().rand.nextDouble() - getWorld().rand.nextDouble()) * 4.0D + 0.5D;
                double y = getPos().getY() + getWorld().rand.nextInt(3);
                double z = getPos().getZ() + (getWorld().rand.nextDouble() - getWorld().rand.nextDouble()) * 4.0D + 0.5D;
                BlockPos spawnAt = new BlockPos(x, y, z);

                if (spawnAt.equals(getPos()))
                    spawnAt = new BlockPos(x, y + 1, z);

                LivingEntity entityLiving = (LivingEntity) entityEntry.create(getWorld());
                if (entityLiving == null)
                    continue;

                if (binding.getTier().checkLight() && !canSpawnInLight(entityLiving, spawnAt))
                    continue;

                entityLiving.moveToBlockPosAndAngles(spawnAt, getWorld().rand.nextFloat() * 360F, 0F);
                entityLiving.getPersistentData().putBoolean("cageBorn", true);
                entityLiving.forceSpawn = true;

                if (entityLiving.isAlive() && !hasReachedSpawnCap(entityLiving) && getWorld().func_226668_i_(entityLiving)) { // func_226668_i_ -> checkNoEntityCollision
                    if (!SoulShards.CONFIG.getBalance().allowBossSpawns() && !entityLiving.isNonBoss())
                        continue;

                    CageSpawnEvent event = new CageSpawnEvent(binding, inventory.getStackInSlot(0), entityLiving);
                    if (MinecraftForge.EVENT_BUS.post(event))
                        continue;

                    getWorld().addEntity(entityLiving);
                    if (entityLiving instanceof MobEntity)
                        ((MobEntity) entityLiving).onInitialSpawn(getWorld(), getWorld().getDifficultyForLocation(spawnAt), SpawnReason.SPAWNER, null, null);
                    break;
                }
            }
        }
    }

    private ActionResult<Binding> canSpawn() {
        BlockState state = getBlockState();
        if (state.getBlock() != RegistrarSoulShards.SOUL_CAGE)
            return new ActionResult<>(ActionResultType.FAIL, null);

        ItemStack shardStack = inventory.getStackInSlot(0);
        if (shardStack.isEmpty() || !(shardStack.getItem() instanceof ItemSoulShard))
            return new ActionResult<>(ActionResultType.FAIL, null);

        Binding binding = getBinding();
        if (binding == null || binding.getBoundEntity() == null)
            return new ActionResult<>(ActionResultType.FAIL, binding);

        IShardTier tier = binding.getTier();

        if (tier.getSpawnAmount() == 0)
            return new ActionResult<>(ActionResultType.FAIL, binding);

        if (SoulShards.CONFIG.getBalance().requireOwnerOnline() && !ownerOnline())
            return new ActionResult<>(ActionResultType.FAIL, binding);

        if (!SoulShards.CONFIG.getEntityList().isEnabled(binding.getBoundEntity()))
            return new ActionResult<>(ActionResultType.FAIL, binding);

        if (!SoulShards.CONFIG.getBalance().requireRedstoneSignal()) {
            if (state.get(BlockSoulCage.POWERED) && tier.checkRedstone())
                return new ActionResult<>(ActionResultType.FAIL, binding);
        } else if (!state.get(BlockSoulCage.POWERED))
            return new ActionResult<>(ActionResultType.FAIL, binding);

        if (tier.checkPlayer() && getWorld().getClosestPlayer(getPos().getX(), getPos().getY(), getPos().getZ(), 16, false) == null)
            return new ActionResult<>(ActionResultType.FAIL, binding);

        return new ActionResult<>(ActionResultType.SUCCESS, binding);
    }

    private boolean canSpawnInLight(LivingEntity entityLiving, BlockPos pos) {
        return !(entityLiving instanceof IMob) || world.getLightFor(LightType.BLOCK, pos) <= 8;
    }

    private boolean hasReachedSpawnCap(LivingEntity living) {
        AxisAlignedBB box = new AxisAlignedBB(getPos().getX() - 16, getPos().getY() - 16, getPos().getZ() - 16, getPos().getX() + 16, getPos().getY() + 16, getPos().getZ() + 16);

        int mobCount = getWorld().getEntitiesWithinAABB(living.getClass(), box, e -> e != null && e.getPersistentData().getBoolean("cageBorn")).size();
        return mobCount >= SoulShards.CONFIG.getBalance().getSpawnCap();
    }

    public void setState(boolean active) {
        BlockState state = getBlockState();
        if (!(state.getBlock() instanceof BlockSoulCage))
            return;
        
        getWorld().setBlockState(getPos(), state.with(BlockSoulCage.ACTIVE, active));
        this.active = active;
    }

    @Override
    public void read(CompoundNBT tag) {
        super.read(tag);

        this.inventory.deserializeNBT(tag.getCompound("inventory"));
        this.activeTime = tag.getInt("activeTime");
        this.active = tag.getBoolean("active");
    }

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        tag.put("inventory", inventory.serializeNBT());
        tag.putInt("activeTime", activeTime);
        tag.putBoolean("active", active);

        return super.write(tag);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap != CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
            return LazyOptional.empty();

        return LazyOptional.of(() -> inventory).cast();
    }

    public ItemStackHandler getInventory() {
        return inventory;
    }

    @Nullable
    public Binding getBinding() {
        ItemStack stack = inventory.getStackInSlot(0);
        if (stack.isEmpty() || !(stack.getItem() instanceof ItemSoulShard))
            return null;

        return ((ItemSoulShard) stack.getItem()).getBinding(stack);
    }

    public boolean ownerOnline() {
        Binding binding = getBinding();
        //noinspection ConstantConditions
        return binding != null && binding.getOwner() != null && world.getServer().getPlayerList().getPlayerByUUID(binding.getOwner()) == null;
    }

    public static class SoulCageInventory extends ItemStackHandler {

        public SoulCageInventory() {
            super(1);
        }

        @Nonnull
        @Override
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
            if (!(stack.getItem() instanceof ItemSoulShard))
                return stack;

            Binding binding = ((ItemSoulShard) stack.getItem()).getBinding(stack);
            if (binding == null || binding.getBoundEntity() == null || !SoulShards.CONFIG.getEntityList().isEnabled(binding.getBoundEntity()))
                return stack;

            return super.insertItem(slot, stack, simulate);
        }
    }
}
