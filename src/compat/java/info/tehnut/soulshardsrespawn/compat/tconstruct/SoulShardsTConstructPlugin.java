package info.tehnut.soulshardsrespawn.compat.tconstruct;

import info.tehnut.soulshardsrespawn.SoulShards;
import info.tehnut.soulshardsrespawn.core.util.compat.CompatibilityPlugin;
import info.tehnut.soulshardsrespawn.core.util.compat.ICompatibilityPlugin;
import info.tehnut.soulshardsrespawn.item.Materials;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import slimeknights.tconstruct.common.ModelRegisterUtil;
import slimeknights.tconstruct.library.TinkerRegistry;
import slimeknights.tconstruct.library.Util;
import slimeknights.tconstruct.library.client.MaterialRenderInfo;
import slimeknights.tconstruct.library.client.texture.MetalTextureTexture;
import slimeknights.tconstruct.library.materials.*;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.traits.ITrait;
import slimeknights.tconstruct.library.utils.HarvestLevels;

@CompatibilityPlugin("tconstruct")
public class SoulShardsTConstructPlugin implements ICompatibilityPlugin {

    public static final Modifier MOD_SOUL_STEALER = new ModifierSoulStealer();
    public static final ITrait TRAIT_VILE = new TraitVile();
    public static final Material MATERIAL_CORRUPTED = new Material("corrupted", 0x7A10A7)
            .setCastable(true)
            .addTrait(TRAIT_VILE, MaterialTypes.HEAD);

    @Override
    public void preInit() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void registerItems(RegistryEvent.Register<Item> event) {
        TinkerRegistry.addMaterial(MATERIAL_CORRUPTED);
        TinkerRegistry.addMaterialStats(
                MATERIAL_CORRUPTED,
                new HeadMaterialStats(204, 7.00F, 4.00F, HarvestLevels.IRON),
                new HandleMaterialStats(0.85F, 60),
                new ExtraMaterialStats(50)
        );
    }

    @SubscribeEvent
    public void registerRecipes(RegistryEvent.Register<IRecipe> event) {
        MOD_SOUL_STEALER.addItem(Materials.CORRUPTED_INGOT.getStack(), 16, 1);

        MATERIAL_CORRUPTED.addItem(Materials.CORRUPTED_INGOT.getStack(), 1, 1);
        MATERIAL_CORRUPTED.setRepresentativeItem(Materials.CORRUPTED_INGOT.getStack());
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void registerModels(ModelRegistryEvent event) {
        ModelRegisterUtil.registerModifierModel(MOD_SOUL_STEALER, new ResourceLocation(SoulShards.MODID, "models/item/modifiers/soul_stealer"));

        MATERIAL_CORRUPTED.setRenderInfo(new MaterialRenderInfo.AbstractMaterialRenderInfo() {
            @Override
            public TextureAtlasSprite getTexture(ResourceLocation baseTexture, String location) {
                return new MetalTextureTexture(new ResourceLocation(Util.resource("items/materials/ardite_rust")), baseTexture, location, 0x372458, 0.6f, 1.0f, 0.1f);
            }
        });
    }
}
