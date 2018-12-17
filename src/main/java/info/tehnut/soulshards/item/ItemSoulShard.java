package info.tehnut.soulshards.item;

import info.tehnut.soulshards.SoulShards;
import info.tehnut.soulshards.api.IShardTier;
import info.tehnut.soulshards.api.ISoulShard;
import info.tehnut.soulshards.block.TileEntitySoulCage;
import info.tehnut.soulshards.core.RegistrarSoulShards;
import info.tehnut.soulshards.core.data.Binding;
import info.tehnut.soulshards.core.data.Tier;
import net.minecraft.block.BlockState;
import net.minecraft.block.MobSpawnerBlock;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.client.item.TooltipOptions;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sortme.MobSpawnerLogic;
import net.minecraft.text.Style;
import net.minecraft.text.TextComponent;
import net.minecraft.text.TextFormat;
import net.minecraft.text.TranslatableTextComponent;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DefaultedList;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.List;

public class ItemSoulShard extends Item implements ISoulShard {

    private static final MethodHandle GET_SPAWNER_ENTITY;

    static {
        try {
            Method _getEntityName = MobSpawnerLogic.class.getDeclaredMethod("method_8281");
            _getEntityName.setAccessible(true);
            GET_SPAWNER_ENTITY = MethodHandles.lookup().in(MobSpawnerLogic.class).unreflect(_getEntityName);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public ItemSoulShard() {
        super(new Settings().stackSize(1).itemGroup(ItemGroup.MISC));

        addProperty(new Identifier(SoulShards.MODID, "bound"), (stack, worldIn, entityIn) -> getBinding(stack) != null ? 1.0F : 0.0F);
        addProperty(new Identifier(SoulShards.MODID, "tier"), (stack, world, entity) -> {
            Binding binding = getBinding(stack);
            if (binding == null)
                return 0F;

            return Float.valueOf("0." + Tier.INDEXED.indexOf(binding.getTier()));
        });
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        BlockState state = context.getWorld().getBlockState(context.getPos());
        Binding binding = getBinding(context.getItemStack());
        if (binding == null)
            return ActionResult.PASS;

        if (state.getBlock() instanceof MobSpawnerBlock) {
            if (!SoulShards.CONFIG.getBalance().allowSpawnerAbsorption()) {
                if (context.getPlayer() != null)
                    context.getPlayer().addChatMessage(new TranslatableTextComponent("chat.soulshards.absorb_disabled"), true);
                return ActionResult.PASS;
            }

            if (binding.getKills() > Tier.maxKills)
                return ActionResult.PASS;

            MobSpawnerBlockEntity spawner = (MobSpawnerBlockEntity) context.getWorld().getBlockEntity(context.getPos());
            if (spawner == null)
                return ActionResult.PASS;

            try {
                Identifier entityId = (Identifier) GET_SPAWNER_ENTITY.bindTo(spawner.getLogic()).invoke();
                if (!SoulShards.CONFIG.getEntityList().isEnabled(entityId))
                    return ActionResult.PASS;

                if (binding.getBoundEntity() == null || !binding.getBoundEntity().equals(entityId))
                    return ActionResult.FAILURE;

                updateBinding(context.getItemStack(), binding.addKills(SoulShards.CONFIG.getBalance().getAbsorptionBonus()));
                context.getWorld().breakBlock(context.getPos(), false);
                return ActionResult.SUCCESS;
            } catch (Throwable e) {
                e.printStackTrace();
            }
        } else if (state.getBlock() == RegistrarSoulShards.SOUL_CAGE) {
            if (binding.getBoundEntity() == null)
                return ActionResult.FAILURE;

            TileEntitySoulCage cage = (TileEntitySoulCage) context.getWorld().getBlockEntity(context.getPos());
            if (cage == null)
                return ActionResult.PASS;

            ItemStack cageStack = cage.getInventory().getInvStack(0);
            if (cageStack.isEmpty() && cage.getInventory().isValidInvStack(0, context.getItemStack())) {
                cage.getInventory().setInvStack(0, context.getItemStack().copy());
                context.getItemStack().subtractAmount(1);
                cage.markDirty();
                return ActionResult.SUCCESS;
            }
        }

        return super.useOnBlock(context);
    }

    @Override
    public void buildTooltip(ItemStack stack, World world, List<TextComponent> tooltip, TooltipOptions options) {
        Binding binding = getBinding(stack);
        if (binding == null)
            return;

        Style greyColor = new Style().setColor(TextFormat.GRAY);
        if (binding.getBoundEntity() != null) {
            EntityType entityEntry = Registry.ENTITY_TYPE.get(binding.getBoundEntity());
            if (entityEntry != null)
                tooltip.add(new TranslatableTextComponent("tooltip.soulshards.bound", entityEntry.getTextComponent()).setStyle(greyColor));
            else
                tooltip.add(new TranslatableTextComponent("tooltip.soulshards.bound", binding.getBoundEntity().toString()).setStyle(new Style().setColor(TextFormat.RED)));
        }

        tooltip.add(new TranslatableTextComponent("tooltip.soulshards.tier", binding.getTier().getIndex()).setStyle(greyColor));
        tooltip.add(new TranslatableTextComponent("tooltip.soulshards.kills", binding.getKills()).setStyle(greyColor));
        if (options.isAdvanced() && binding.getOwner() != null)
            tooltip.add(new TranslatableTextComponent("tooltip.soulshards.owner", binding.getOwner().toString()).setStyle(greyColor));
    }

    @Override
    public void addStacksForDisplay(ItemGroup group, DefaultedList<ItemStack> items) {
        if (!isInItemGroup(group))
            return;

        items.add(new ItemStack(this));
        for (IShardTier tier : Tier.INDEXED) {
            ItemStack stack = new ItemStack(this);
            Binding binding = new Binding(null, tier.getKillRequirement());
            updateBinding(stack, binding);
            items.add(stack);
        }
    }

    @Override
    public String getTranslationKey(ItemStack stack) {
        Binding binding = getBinding(stack);
        return super.getTranslationKey(stack) + (binding == null || binding.getBoundEntity() == null ? "_unbound" : "");
    }

    @Override
    public boolean hasEnchantmentGlow(ItemStack stack) {
        Binding binding = getBinding(stack);
        return binding != null && binding.getKills() >= Tier.maxKills;
    }

    @Override
    public Binding getBinding(ItemStack stack) {
        return Binding.fromNBT(stack);
    }

    public void updateBinding(ItemStack stack, Binding binding) {
        CompoundTag tag = stack.getTag();
        if (tag == null)
            stack.setTag(tag = new CompoundTag());

        tag.put("binding", binding.serializeNBT());
    }
}
