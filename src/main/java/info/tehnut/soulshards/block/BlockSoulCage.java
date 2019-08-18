package info.tehnut.soulshards.block;

import info.tehnut.soulshards.core.data.Binding;
import info.tehnut.soulshards.core.data.Tier;
import net.minecraft.block.*;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateFactory;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Property;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

import java.util.Random;

public class BlockSoulCage extends Block implements BlockEntityProvider {

    public static final Property<Boolean> ACTIVE = BooleanProperty.of("active");
    public static final Property<Boolean> POWERED = BooleanProperty.of("powered");

    public BlockSoulCage() {
        super(Settings.copy(Blocks.SPAWNER));

        setDefaultState(getStateFactory().getDefaultState().with(ACTIVE, false).with(POWERED, false));
    }

    @Override
    public boolean activate(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult result) {
        if (!player.isSneaking())
            return false;

        TileEntitySoulCage cage = (TileEntitySoulCage) world.getBlockEntity(pos);
        if (cage == null)
            return false;

        ItemStack stack = cage.inventory.getInvStack(0);
        if (stack.isEmpty())
            return false;

        if (!player.inventory.insertStack(stack)) {
            BlockPos playerPos = new BlockPos(player);
            ItemEntity entity = new ItemEntity(world, playerPos.getX(), playerPos.getY(), playerPos.getZ(), stack);
            world.spawnEntity(entity);
        }
        return true;
    }


    @Override
    public void onBlockRemoved(BlockState blockState, World world, BlockPos blockPos, BlockState blockState2, boolean boolean_1) {
        if (this.hasBlockEntity() && blockState.getBlock() != blockState2.getBlock()) {
            TileEntitySoulCage cage = (TileEntitySoulCage) world.getBlockEntity(blockPos);
            if (cage != null)
                ItemScatterer.spawn(world.getWorld(), blockPos, cage.inventory);
        }

        super.onBlockRemoved(blockState, world, blockPos, blockState2, boolean_1);
    }

    @Override
    public boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    @Override
    public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        TileEntitySoulCage cage = (TileEntitySoulCage) world.getBlockEntity(pos);
        if (cage == null)
            return 0;

        Binding binding = cage.getBinding();
        if (binding == null)
            return 0;

        return (int) (((double) binding.getTier().getIndex() / ((double) Tier.INDEXED.size() - 1)) * 15D);
    }

    @Override
    public boolean isTranslucent(BlockState state, BlockView view, BlockPos pos) {
        return true;
    }

    @Override
    public boolean canSuffocate(BlockState state, BlockView view, BlockPos pos) {
        return false;
    }

    @Override
    public boolean isSimpleFullBlock(BlockState state, BlockView view, BlockPos pos) {
        return false;
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public void onBlockAdded(BlockState state1, World world, BlockPos pos, BlockState state2, boolean someBool) {
        handleRedstoneChange(world, state1, pos);
    }

    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos neighborPos, boolean someBool) {
        handleRedstoneChange(world, state, pos);
    }

    @Override
    public void onRandomTick(BlockState state, World world, BlockPos pos, Random rand) {
        if (state.get(POWERED) && !world.isReceivingRedstonePower(pos))
            world.setBlockState(pos, state.with(POWERED, false));
    }

    @Override
    protected void appendProperties(StateFactory.Builder<Block, BlockState> factory) {
        factory.add(ACTIVE, POWERED);
    }

    @Override
    public boolean hasBlockEntity() {
        return true;
    }

    @Override
    public BlockEntity createBlockEntity(BlockView view) {
        return new TileEntitySoulCage();
    }

    private void handleRedstoneChange(World world, BlockState state, BlockPos pos) {
        boolean powered = world.isReceivingRedstonePower(pos);
        if (state.get(POWERED) && !powered)
            world.setBlockState(pos, state.with(POWERED, false), 2);
        else if (!state.get(POWERED) && powered)
            world.setBlockState(pos, state.with(POWERED, true), 2);
    }
}
