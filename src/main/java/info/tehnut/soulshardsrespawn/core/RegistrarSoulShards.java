package info.tehnut.soulshardsrespawn.core;

import info.tehnut.soulshardsrespawn.SoulShards;
import info.tehnut.soulshardsrespawn.block.BlockSoulCage;
import info.tehnut.soulshardsrespawn.block.TileEntitySoulCage;
import info.tehnut.soulshardsrespawn.core.data.Tier;
import info.tehnut.soulshardsrespawn.core.util.EnchantmentSoulStealer;
import info.tehnut.soulshardsrespawn.item.ItemEnum;
import info.tehnut.soulshardsrespawn.item.ItemSoulShard;
import info.tehnut.soulshardsrespawn.item.ItemVileSword;
import info.tehnut.soulshardsrespawn.item.Materials;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;

@Mod.EventBusSubscriber(modid = SoulShards.MODID)
@GameRegistry.ObjectHolder(SoulShards.MODID)
public class RegistrarSoulShards {

    public static final Block SOUL_CAGE = Blocks.AIR;

    public static final Item VILE_SWORD = Items.AIR;
    public static final Item SOUL_SHARD = Items.AIR;
    public static final Item MATERIALS = Items.AIR;

    public static final Enchantment SOUL_STEALER = Enchantments.INFINITY;

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().registerAll(
                new BlockSoulCage().setRegistryName("soul_cage")
        );

        GameRegistry.registerTileEntity(TileEntitySoulCage.class, SoulShards.MODID + ":soul_cage");
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        Tier.readTiers();

        event.getRegistry().registerAll(
                new ItemBlock(SOUL_CAGE).setRegistryName(SOUL_CAGE.getRegistryName()),
                new ItemVileSword().setRegistryName("vile_sword"),
                new ItemSoulShard().setRegistryName("soul_shard"),
                new ItemEnum<>(Materials.class).setRegistryName("materials")
        );
    }

    @SubscribeEvent
    public static void registerEnchantments(RegistryEvent.Register<Enchantment> event) {
        event.getRegistry().registerAll(
                new EnchantmentSoulStealer().setRegistryName("soul_stealer")
        );
    }

    @SubscribeEvent
    public static void registerRecipes(RegistryEvent.Register<IRecipe> event) {
        OreDictionary.registerOre("dustCorrupted", Materials.CORRUPTED_ESSENCE.getStack());
        OreDictionary.registerOre("ingotCorrupted", Materials.CORRUPTED_INGOT.getStack());
        OreDictionary.registerOre("dustVile", Materials.VILE_DUST.getStack());

        GameRegistry.addSmelting(new ItemStack(Blocks.SOUL_SAND), Materials.VILE_DUST.getStack(), 0.75F);
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void registerModels(ModelRegistryEvent event) {
        ModelLoader.setCustomModelResourceLocation(SOUL_SHARD, 0, new ModelResourceLocation(SOUL_SHARD.getRegistryName(), "inventory"));

        for (Materials material : Materials.values())
            ModelLoader.setCustomModelResourceLocation(MATERIALS, material.ordinal(), new ModelResourceLocation(SoulShards.MODID + ":" + material.getInternalName(), "inventory"));

        ModelLoader.setCustomModelResourceLocation(VILE_SWORD, 0, new ModelResourceLocation(VILE_SWORD.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(SOUL_CAGE), 0, new ModelResourceLocation(SOUL_CAGE.getRegistryName(), "active=true,powered=false"));
    }
}
