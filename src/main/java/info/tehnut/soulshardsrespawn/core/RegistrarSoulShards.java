package info.tehnut.soulshardsrespawn.core;

import info.tehnut.soulshardsrespawn.SoulShards;
import info.tehnut.soulshardsrespawn.block.BlockSoulCage;
import info.tehnut.soulshardsrespawn.block.TileEntitySoulCage;
import info.tehnut.soulshardsrespawn.core.data.Tier;
import info.tehnut.soulshardsrespawn.core.util.EnchantmentSoulStealer;
import info.tehnut.soulshardsrespawn.item.ItemSoulShard;
import info.tehnut.soulshardsrespawn.item.ItemVileSword;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.*;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ObjectHolder;

@Mod.EventBusSubscriber(modid = SoulShards.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
@ObjectHolder(SoulShards.MODID)
public class RegistrarSoulShards {

    public static final Block SOUL_CAGE = Blocks.AIR;

    public static final TileEntityType<?> SOUL_CAGE_TE = TileEntityType.BED;

    public static final Item SOUL_SHARD = Items.AIR;
    public static final Item VILE_SWORD = Items.AIR;
    public static final Item CORRUPTED_ESSENCE = Items.AIR;
    public static final Item CORRUPTED_INGOT = Items.AIR;
    public static final Item VILE_DUST = Items.AIR;

    public static final Enchantment SOUL_STEALER = Enchantments.INFINITY;

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().registerAll(
                new BlockSoulCage().setRegistryName("soul_cage")
        );
    }

    @SubscribeEvent
    public static void registerTileEntities(RegistryEvent.Register<TileEntityType<?>> event) {
        event.getRegistry().register(
                TileEntityType.Builder.create(TileEntitySoulCage::new, SOUL_CAGE).build(null).setRegistryName(SoulShards.MODID, "soul_cage")
        );
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        Tier.readTiers();

        event.getRegistry().registerAll(
                new BlockItem(SOUL_CAGE, new Item.Properties().group(SoulShards.TAB_SS)).setRegistryName(SOUL_CAGE.getRegistryName()),
                new ItemVileSword().setRegistryName("vile_sword"),
                new ItemSoulShard().setRegistryName("soul_shard"),
                new Item(new Item.Properties().group(SoulShards.TAB_SS)).setRegistryName("corrupted_essence"),
                new Item(new Item.Properties().group(SoulShards.TAB_SS)).setRegistryName("corrupted_ingot"),
                new Item(new Item.Properties().group(SoulShards.TAB_SS)).setRegistryName("vile_dust")
        );
    }

    @SubscribeEvent
    public static void registerEnchantments(RegistryEvent.Register<Enchantment> event) {
        event.getRegistry().registerAll(
                new EnchantmentSoulStealer().setRegistryName("soul_stealer")
        );
    }
}
