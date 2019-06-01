package info.tehnut.soulshardsrespawn.block;

import info.tehnut.soulshardsrespawn.SoulShards;
import info.tehnut.soulshardsrespawn.api.CageSpawnEvent;
import info.tehnut.soulshardsrespawn.core.RegistrarSoulShards;
import info.tehnut.soulshardsrespawn.core.data.Binding;
import info.tehnut.soulshardsrespawn.item.ItemSoulShard;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.monster.IMob;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TileEntitySoulCage extends TileEntity implements ITickable {

    ItemStackHandler inventory;
    private int activeTime;

    public TileEntitySoulCage() {
        this.inventory = new SoulCageInventory();
    }

    @Override
    public void update() {
        if (getWorld().isRemote)
            return;

        ActionResult<Binding> result = canSpawn();
        if (result.getType() != EnumActionResult.SUCCESS) {
            setState(false);
            world.notifyNeighborsOfStateChange(pos, getBlockType(), false);
            return;
        }

        setState(true);
        world.notifyNeighborsOfStateChange(pos, getBlockType(), false);
        activeTime++;
        IBlockState state = getWorld().getBlockState(getPos());
        getWorld().notifyBlockUpdate(getPos(), state, state, 3);

        if (activeTime % result.getResult().getTier().getCooldown() == 0)
            spawnEntities();
    }

    private void spawnEntities() {
        Binding binding = getBinding();
        if (binding == null || binding.getBoundEntity() == null)
            return;

        EntityEntry entityEntry = ForgeRegistries.ENTITIES.getValue(binding.getBoundEntity());
        for (int i = 0; i < binding.getTier().getSpawnAmount(); i++) {
            for (int attempts = 0; attempts < 5; attempts++) {

                double x = getPos().getX() + (getWorld().rand.nextDouble() - getWorld().rand.nextDouble()) * 4.0D;
                double y = getPos().getY() + getWorld().rand.nextInt(3) - 1;
                double z = getPos().getZ() + (getWorld().rand.nextDouble() - getWorld().rand.nextDouble()) * 4.0D;
                BlockPos spawnAt = new BlockPos(x, y, z);

                EntityLiving entityLiving = (EntityLiving) entityEntry.newInstance(getWorld());
                if (entityLiving == null)
                    continue;

                if (binding.getTier().checkLight() && !canSpawnInLight(entityLiving, spawnAt))
                    continue;

                entityLiving.setLocationAndAngles(spawnAt.getX(), spawnAt.getY(), spawnAt.getZ(), MathHelper.wrapDegrees(getWorld().rand.nextFloat() * 360F), 0F);
                entityLiving.getEntityData().setBoolean("cageBorn", true);
                entityLiving.forceSpawn = true;
                entityLiving.enablePersistence();

                if (entityLiving.isNotColliding() && !entityLiving.isDead && !hasReachedSpawnCap(entityLiving)) {
                    if (!SoulShards.CONFIG.allowBossSpawns() && !entityLiving.isNonBoss())
                        continue;

                    CageSpawnEvent event = new CageSpawnEvent(binding, inventory.getStackInSlot(0), entityLiving);
                    if (MinecraftForge.EVENT_BUS.post(event))
                        continue;

                    getWorld().spawnEntity(entityLiving);
                    entityLiving.onInitialSpawn(getWorld().getDifficultyForLocation(spawnAt), null);
                    break;
                }
            }
        }
    }

    private ActionResult<Binding> canSpawn() {
        if (!FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(0).getGameRules().getBoolean("allowCageSpawns"))
            return ActionResult.newResult(EnumActionResult.FAIL, null);

        IBlockState state = world.getBlockState(pos);
        if (state.getBlock() != RegistrarSoulShards.SOUL_CAGE)
            return ActionResult.newResult(EnumActionResult.FAIL, null);

        ItemStack shardStack = inventory.getStackInSlot(0);
        if (shardStack.isEmpty() || !(shardStack.getItem() instanceof ItemSoulShard))
            return ActionResult.newResult(EnumActionResult.FAIL, null);

        Binding binding = ((ItemSoulShard) shardStack.getItem()).getBinding(shardStack);
        if (binding == null || binding.getBoundEntity() == null)
            return ActionResult.newResult(EnumActionResult.FAIL, binding);

        if (binding.getTier().getSpawnAmount() == 0)
            return ActionResult.newResult(EnumActionResult.FAIL, binding);

        if (SoulShards.CONFIG.requireOwnerOnline() && !ownerOnline())
            return ActionResult.newResult(EnumActionResult.FAIL, binding);

        if (!SoulShards.CONFIG.isEntityEnabled(binding.getBoundEntity()))
            return ActionResult.newResult(EnumActionResult.FAIL, binding);

        if (!SoulShards.CONFIG.requireRedstoneSignal()) {
            if (state.getValue(BlockSoulCage.POWERED) && binding.getTier().checkRedstone())
                return ActionResult.newResult(EnumActionResult.FAIL, binding);
        } else if (!state.getValue(BlockSoulCage.POWERED))
            return ActionResult.newResult(EnumActionResult.FAIL, binding);

        if (binding.getTier().checkPlayer() && getWorld().getClosestPlayer(getPos().getX(), getPos().getY(), getPos().getZ(), 16, false) == null)
            return ActionResult.newResult(EnumActionResult.FAIL, binding);

        return ActionResult.newResult(EnumActionResult.SUCCESS, binding);
    }

    private boolean canSpawnInLight(EntityLiving entityLiving, BlockPos pos) {
        return !(entityLiving instanceof IMob) || world.getLightFromNeighbors(pos) <= 8;
    }

    private boolean hasReachedSpawnCap(EntityLiving living) {
        AxisAlignedBB box = new AxisAlignedBB(getPos().getX() - 16, getPos().getY() - 16, getPos().getZ() - 16, getPos().getX() + 16, getPos().getY() + 16, getPos().getZ() + 16);

        int mobCount = getWorld().getEntitiesWithinAABB(living.getClass(), box, e -> e != null && e.getEntityData().getBoolean("cageBorn")).size();
        return mobCount >= SoulShards.CONFIG.getSpawnCap();
    }

    public void setState(boolean active) {
        IBlockState state = getWorld().getBlockState(getPos());
        getWorld().setBlockState(getPos(), state.withProperty(BlockSoulCage.ACTIVE, active));
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        this.inventory.deserializeNBT(compound.getCompoundTag("inventory"));
        this.activeTime = compound.getInteger("activeTime");

        super.readFromNBT(compound);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setTag("inventory", inventory.serializeNBT());
        compound.setInteger("activeTime", activeTime);

        return super.writeToNBT(compound);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY ? CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(inventory) : super.getCapability(capability, facing);
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
        return oldState.getBlock() != newState.getBlock();
    }

    @Override
    public final SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(getPos(), -999, writeToNBT(new NBTTagCompound()));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public final void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        super.onDataPacket(net, pkt);
        readFromNBT(pkt.getNbtCompound());
    }

    @Override
    public final NBTTagCompound getUpdateTag() {
        return writeToNBT(new NBTTagCompound());
    }

    @Override
    public final void handleUpdateTag(NBTTagCompound tag) {
        readFromNBT(tag);
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
        return binding != null && binding.getOwner() != null && FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUUID(binding.getOwner()) == null;
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
            if (binding == null || binding.getBoundEntity() == null || !SoulShards.CONFIG.isEntityEnabled(binding.getBoundEntity()))
                return stack;

            return super.insertItem(slot, stack, simulate);
        }
    }
}
