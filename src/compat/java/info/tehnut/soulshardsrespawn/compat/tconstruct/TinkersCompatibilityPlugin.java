package info.tehnut.soulshardsrespawn.compat.tconstruct;

import info.tehnut.soulshardsrespawn.SoulShards;
import info.tehnut.soulshardsrespawn.core.util.compat.CompatibilityPlugin;
import info.tehnut.soulshardsrespawn.core.util.compat.ICompatibilityPlugin;
import info.tehnut.soulshardsrespawn.item.Materials;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import slimeknights.tconstruct.common.ModelRegisterUtil;
import slimeknights.tconstruct.library.TinkerRegistry;
import slimeknights.tconstruct.library.Util;
import slimeknights.tconstruct.library.client.MaterialRenderInfo;
import slimeknights.tconstruct.library.client.texture.MetalTextureTexture;
import slimeknights.tconstruct.library.fluid.FluidColored;
import slimeknights.tconstruct.library.materials.*;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.traits.ITrait;
import slimeknights.tconstruct.smeltery.TinkerSmeltery;

import static slimeknights.tconstruct.library.utils.HarvestLevels.DIAMOND;

@CompatibilityPlugin("tconstruct")
public class TinkersCompatibilityPlugin implements ICompatibilityPlugin {

    public static final Modifier MOD_SOUL_STEALER = new ModifierSoulStealer();
    public static final ITrait TRAIT_VILE = new TraitVile();
    public static final Fluid FLUID_CORRUPTED = new FluidColored(SoulShards.MODID + ".corrupted", 0x372458);
    public static final Material MATERIAL_CORRUPTED = new Material("corrupted", 0x7A10A7)
            .addTrait(TRAIT_VILE, MaterialTypes.HEAD);

    @Override
    public void preInit() {
        MinecraftForge.EVENT_BUS.register(this);
        FluidRegistry.registerFluid(FLUID_CORRUPTED);
        MATERIAL_CORRUPTED.setFluid(FLUID_CORRUPTED);
        MATERIAL_CORRUPTED.setCastable(true);
        MATERIAL_CORRUPTED.setCraftable(false);

        TinkerRegistry.addMaterial(MATERIAL_CORRUPTED);
        TinkerSmeltery.registerToolpartMeltingCasting(MATERIAL_CORRUPTED);
        TinkerRegistry.addMaterialStats(
                MATERIAL_CORRUPTED,
                new HeadMaterialStats(204, 6.00f, 4.00f, DIAMOND),
                new HandleMaterialStats(0.85f, 60),
                new ExtraMaterialStats(50)
        );
    }

    @SubscribeEvent
    public void registerRecipes(RegistryEvent.Register<IRecipe> event) {
        TinkerRegistry.registerMelting(Materials.CORRUPTED_INGOT.getStack(), FLUID_CORRUPTED, Material.VALUE_Ingot);
        MOD_SOUL_STEALER.addItem(Materials.CORRUPTED_INGOT.getStack(), 16, 1);

        MATERIAL_CORRUPTED.addItem("ingotCorrupted", 1, Material.VALUE_Ingot);
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
