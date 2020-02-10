package info.tehnut.soulshardsrespawn.block;

import info.tehnut.soulshardsrespawn.core.data.Binding;
import info.tehnut.soulshardsrespawn.core.data.Tier;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.Property;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Random;

public class BlockSoulCage extends Block {

    public static final Property<Boolean> POWERED = BooleanProperty.create("powered");
    public static final Property<Boolean> ACTIVE = BooleanProperty.create("active");

    public BlockSoulCage() {
        super(Properties.create(Material.IRON).harvestLevel(1).harvestTool(ToolType.PICKAXE).hardnessAndResistance(3.0F));

        setDefaultState(getStateContainer().getBaseState().with(POWERED, false));
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        if (!player.isSteppingCarefully())
            return ActionResultType.PASS;

        TileEntitySoulCage cage = (TileEntitySoulCage) world.getTileEntity(pos);
        if (cage == null)
            return ActionResultType.PASS;

        ItemStack stack = cage.getInventory().extractItem(0, 1, false);
        if (stack.isEmpty())
            return ActionResultType.PASS;

        ItemHandlerHelper.giveItemToPlayer(player, stack);
        return ActionResultType.SUCCESS;
    }


    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState state2, boolean someBool) {
        handleRedstoneChange(world, state, pos);
    }

    @Override
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block neighbor, BlockPos neighborPos, boolean someBool) {
        handleRedstoneChange(world, state, pos);
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (state.get(POWERED) && !world.isBlockPowered(pos))
            world.setBlockState(pos, state.with(POWERED, false));
    }

    @Override
    public void onReplaced(BlockState state, World world, BlockPos pos, BlockState blockState2, boolean someBool) {
        if (this.hasTileEntity(state) && state.getBlock() != blockState2.getBlock()) {
            TileEntitySoulCage cage = (TileEntitySoulCage) world.getTileEntity(pos);
            if (cage != null) {
                ItemStack stack = cage.getInventory().getStackInSlot(0);
                InventoryHelper.dropItems(world, pos, NonNullList.from(ItemStack.EMPTY, stack));
            }
        }

        super.onReplaced(state, world, pos, blockState2, someBool);
    }

    @Override
    public boolean canConnectRedstone(BlockState state, IBlockReader world, BlockPos pos, @Nullable Direction side) {
        return true;
    }

    @Override
    public boolean hasComparatorInputOverride(BlockState state) {
        return true;
    }

    @Override
    public int getComparatorInputOverride(BlockState blockState, World world, BlockPos pos) {
        TileEntitySoulCage cage = (TileEntitySoulCage) world.getTileEntity(pos);
        if (cage == null)
            return 0;

        Binding binding = cage.getBinding();
        if (binding == null)
            return 0;

        return (int) (((double) binding.getTier().getIndex() / ((double) Tier.INDEXED.size() - 1)) * 15D);
    }

    @Override
    public boolean isViewBlocking(BlockState state, IBlockReader reader, BlockPos pos) {
        return false;
    }

    @Override
    public boolean isNormalCube(BlockState state, IBlockReader reader, BlockPos pos) {
        return false;
    }

    @Override
    public boolean causesSuffocation(BlockState state, IBlockReader reader, BlockPos pos) {
        return false;
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(POWERED, ACTIVE);
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new TileEntitySoulCage();
    }

    private void handleRedstoneChange(World world, BlockState state, BlockPos pos) {
        boolean powered = world.isBlockPowered(pos);
        if (state.get(POWERED) && !powered)
            world.setBlockState(pos, state.with(POWERED, false), 2);
        else if (!state.get(POWERED) && powered)
            world.setBlockState(pos, state.with(POWERED, true), 2);
    }
}
