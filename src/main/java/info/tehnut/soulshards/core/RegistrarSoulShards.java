package info.tehnut.soulshards.core;

import info.tehnut.soulshards.SoulShards;
import info.tehnut.soulshards.block.BlockSoulCage;
import info.tehnut.soulshards.block.TileEntitySoulCage;
import info.tehnut.soulshards.core.util.EnchantmentSoulStealer;
import info.tehnut.soulshards.item.ItemSoulShard;
import info.tehnut.soulshards.item.ItemVileSword;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class RegistrarSoulShards {

    public static final Block SOUL_CAGE = new BlockSoulCage();

    public static final BlockEntityType<TileEntitySoulCage> SOUL_CAGE_TE = BlockEntityType.Builder.create(TileEntitySoulCage::new, SOUL_CAGE).build(null);

    public static final Item SOUL_SHARD = new ItemSoulShard();
    public static final Item VILE_SWORD = new ItemVileSword();
    public static final Item CORRUPTED_ESSENCE = new Item(new Item.Settings().group(ItemGroup.MISC));
    public static final Item CORRUPTED_INGOT = new Item(new Item.Settings().group(ItemGroup.MISC));
    public static final Item VILE_DUST = new Item(new Item.Settings().group(ItemGroup.MISC));

    public static final Enchantment SOUL_STEALER = new EnchantmentSoulStealer();

    public static void registerBlocks(Registry<Block> registry) {
        Registry.register(registry, new Identifier(SoulShards.MODID, "soul_cage"), SOUL_CAGE);
        Registry.register(Registry.BLOCK_ENTITY, new Identifier(SoulShards.MODID, "soul_cage"), SOUL_CAGE_TE);
    }

    public static void registerItems(Registry<Item> registry) {
        Registry.register(registry, new Identifier(SoulShards.MODID, "soul_cage"), new BlockItem(SOUL_CAGE, new Item.Settings().group(ItemGroup.MISC)));
        Registry.register(registry, new Identifier(SoulShards.MODID, "soul_shard"), SOUL_SHARD);
        Registry.register(registry, new Identifier(SoulShards.MODID, "vile_sword"), VILE_SWORD);
        Registry.register(registry, new Identifier(SoulShards.MODID, "corrupted_essence"), CORRUPTED_ESSENCE);
        Registry.register(registry, new Identifier(SoulShards.MODID, "corrupted_ingot"), CORRUPTED_INGOT);
        Registry.register(registry, new Identifier(SoulShards.MODID, "vile_dust"), VILE_DUST);
    }

    public static void registerEnchantments(Registry<Enchantment> registry) {
        Registry.register(registry, new Identifier(SoulShards.MODID, "soul_stealer"), SOUL_STEALER);
    }
}
