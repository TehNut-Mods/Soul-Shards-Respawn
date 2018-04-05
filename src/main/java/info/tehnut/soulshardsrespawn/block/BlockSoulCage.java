package info.tehnut.soulshardsrespawn.block;

import info.tehnut.soulshardsrespawn.SoulShards;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Random;

public class BlockSoulCage extends Block {

    public static final IProperty<Boolean> POWERED = PropertyBool.create("powered");
    public static final IProperty<Boolean> ACTIVE = PropertyBool.create("active");

    public BlockSoulCage() {
        super(Material.IRON, MapColor.PURPLE);

        setUnlocalizedName(SoulShards.MODID + ".soul_cage");
        setCreativeTab(SoulShards.TAB_SS);
        setDefaultState(blockState.getBaseState().withProperty(POWERED, false));
        setHarvestLevel("pickaxe", 1);
        setHardness(3.0F);
        setResistance(3.0F);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!player.isSneaking())
            return false;

        TileEntitySoulCage cage = (TileEntitySoulCage) world.getTileEntity(pos);
        if (cage == null)
            return false;

        ItemStack stack = cage.inventory.extractItem(0, 1, false);
        if (stack.isEmpty())
            return false;

        ItemHandlerHelper.giveItemToPlayer(player, stack);
        return true;
    }

    @Override
    public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
        if (state.getValue(POWERED) && !world.isBlockPowered(pos))
            world.setBlockState(pos, state.withProperty(POWERED, false), 2);
        else if (!state.getValue(POWERED) && world.isBlockPowered(pos))
            world.setBlockState(pos, state.withProperty(POWERED, true), 2);
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos) {
        if (state.getValue(POWERED) && !world.isBlockPowered(pos))
            world.setBlockState(pos, state.withProperty(POWERED, false));
        else if (!state.getValue(POWERED) && world.isBlockPowered(pos))
            world.setBlockState(pos, state.withProperty(POWERED, true));
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
        if (state.getValue(POWERED) && !world.isBlockPowered(pos))
            world.setBlockState(pos, state.withProperty(POWERED, false));
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        TileEntitySoulCage cage = (TileEntitySoulCage) world.getTileEntity(pos);
        if (cage != null)
            InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), cage.inventory.getStackInSlot(0));

        super.breakBlock(world, pos, state);
    }

    @Override
    public int damageDropped(IBlockState state) {
        return 0;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean causesSuffocation(IBlockState state) {
        return false;
    }

    @Override
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        boolean powered = (meta & 1) == 1;
        boolean active = meta >> 2 == 1;
        return getDefaultState().withProperty(POWERED, powered).withProperty(ACTIVE, active);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int powered = state.getValue(POWERED) ? 1 : 0;
        int active = (state.getValue(ACTIVE) ? 1 : 0) << 2;
        return powered | active;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer.Builder(this).add(POWERED, ACTIVE).build();
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileEntitySoulCage();
    }
}
