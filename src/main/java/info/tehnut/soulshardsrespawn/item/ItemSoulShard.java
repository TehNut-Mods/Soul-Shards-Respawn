package info.tehnut.soulshardsrespawn.item;

import info.tehnut.soulshardsrespawn.SoulShards;
import info.tehnut.soulshardsrespawn.api.IShardTier;
import info.tehnut.soulshardsrespawn.api.ISoulShard;
import info.tehnut.soulshardsrespawn.block.TileEntitySoulCage;
import info.tehnut.soulshardsrespawn.core.RegistrarSoulShards;
import info.tehnut.soulshardsrespawn.core.data.Binding;
import info.tehnut.soulshardsrespawn.core.data.Tier;
import net.minecraft.block.BlockMobSpawner;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.MobSpawnerBaseLogic;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.List;

public class ItemSoulShard extends Item implements ISoulShard {

    private static final Method GET_ENTITY_ID_METHOD;

    static {
        GET_ENTITY_ID_METHOD = ReflectionHelper.findMethod(MobSpawnerBaseLogic.class, "getEntityId", "func_190895_g");
    }

    public ItemSoulShard() {
        setUnlocalizedName(SoulShards.MODID + ".soul_shard");
        setCreativeTab(SoulShards.TAB_SS);
        setHasSubtypes(true);

        addPropertyOverride(new ResourceLocation(SoulShards.MODID, "bound"), (stack, worldIn, entityIn) -> getBinding(stack) != null ? 1.0F : 0.0F);
        addPropertyOverride(new ResourceLocation(SoulShards.MODID, "tier"), (stack, world, entity) -> {
            Binding binding = getBinding(stack);
            if (binding == null)
                return 0F;

            return Float.valueOf("0." + Tier.INDEXED.indexOf(binding.getTier()));
        });
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        IBlockState state = world.getBlockState(pos);
        ItemStack stack = player.getHeldItem(hand);
        Binding binding = getBinding(stack);
        if (binding == null)
            return EnumActionResult.PASS;

        if (state.getBlock() instanceof BlockMobSpawner) {
            if (!SoulShards.CONFIG.allowSpawnerAbsorption()) {
                player.sendStatusMessage(new TextComponentTranslation("chat.soulshardsrespawn.absorb_disabled"), true);
                return EnumActionResult.PASS;
            }

            if (binding.getKills() >= Tier.maxKills)
                return EnumActionResult.PASS;

            TileEntityMobSpawner mobSpawner = (TileEntityMobSpawner) world.getTileEntity(pos);
            if (mobSpawner == null)
                return EnumActionResult.PASS;

            try {
                ResourceLocation entityId = (ResourceLocation) GET_ENTITY_ID_METHOD.invoke(mobSpawner.getSpawnerBaseLogic());
                if (!SoulShards.CONFIG.isEntityEnabled(entityId))
                    return EnumActionResult.PASS;

                if (entityId == null || binding.getBoundEntity() == null || !binding.getBoundEntity().equals(entityId))
                    return EnumActionResult.FAIL;

                updateBinding(stack, binding.addKills(SoulShards.CONFIG.getAbsorptionBonus()));
                world.destroyBlock(pos, false);
                return EnumActionResult.SUCCESS;
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (state.getBlock() == RegistrarSoulShards.SOUL_CAGE) {
            if (binding.getBoundEntity() == null)
                return EnumActionResult.FAIL;

            TileEntitySoulCage cage = (TileEntitySoulCage) world.getTileEntity(pos);
            if (cage == null)
                return EnumActionResult.PASS;

            IItemHandler itemHandler = cage.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
            if (itemHandler != null && itemHandler.getStackInSlot(0).isEmpty()) {
                ItemHandlerHelper.insertItem(itemHandler, stack.copy(), false);
                cage.markDirty();
                player.setHeldItem(hand, ItemStack.EMPTY);
                return EnumActionResult.SUCCESS;
            }
        }

        return super.onItemUse(player, world, pos, hand, facing, hitX, hitY, hitZ);
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (!isInCreativeTab(tab))
            return;

        items.add(new ItemStack(this));

        for (IShardTier tier : Tier.INDEXED) {
            ItemStack stack = new ItemStack(this);
            Binding binding = new Binding(null, tier.getKillRequirement());
            updateBinding(stack, binding);
            items.add(stack);
        }

        if (SoulShards.CONFIG.explodeCreativeTab()) {
            Binding binding = new Binding(null, Tier.maxKills);
            SoulShards.CONFIG.getEntityMap().entrySet()
                    .stream()
                    .filter(e -> e.getValue() || SoulShards.CONFIG.ignoreBlacklistForTab())
                    .forEach(e -> {
                        binding.setBoundEntity(e.getKey());
                        ItemStack stack = new ItemStack(this);
                        updateBinding(stack, binding);
                        items.add(stack);
                    });
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        Binding binding = getBinding(stack);
        if (binding == null)
            return;

        if (binding.getBoundEntity() != null) {
            EntityEntry entityEntry = ForgeRegistries.ENTITIES.getValue(binding.getBoundEntity());
            if (entityEntry != null)
                tooltip.add(I18n.format("tooltip.soulshardsrespawn.bound", entityEntry.getName()));
        }

        tooltip.add(I18n.format("tooltip.soulshardsrespawn.tier", binding.getTier().getIndex()));
        tooltip.add(I18n.format("tooltip.soulshardsrespawn.kills", binding.getKills()));
        if (flag.isAdvanced() && binding.getOwner() != null)
            tooltip.add(I18n.format("tooltip.soulshardsrespawn.owner", binding.getOwner().toString()));
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        Binding binding = getBinding(stack);
        return super.getUnlocalizedName(stack) + (binding == null || binding.getBoundEntity() == null ? "_unbound" : "");
    }

    @Override
    public boolean hasEffect(ItemStack stack) {
        Binding binding = getBinding(stack);
        return binding != null && binding.getKills() >= Tier.maxKills;
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        Binding binding = getBinding(stack);
        return SoulShards.CONFIG.displayDurabilityBar() && binding != null && binding.getKills() < Tier.maxKills;
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        Binding binding = getBinding(stack);
        if (binding == null)
            return 1.0D;

        return 1.0D - ((double) binding.getKills() / (double) Tier.maxKills);
    }

    @Override
    public int getRGBDurabilityForDisplay(ItemStack stack) {
        return super.getRGBDurabilityForDisplay(stack);
    }

    @Override
    public int getItemStackLimit(ItemStack stack) {
        return getBinding(stack) == null ? 64 : 1;
    }

    @Nullable
    @Override
    public Binding getBinding(ItemStack stack) {
        return Binding.fromNBT(stack);
    }

    public void updateBinding(ItemStack stack, Binding binding) {
        if (!stack.hasTagCompound())
            stack.setTagCompound(new NBTTagCompound());

        stack.getTagCompound().setTag("binding", binding.serializeNBT());
    }
}
