package muramasa.gregtech.integration.fr;

import muramasa.gregtech.Ref;
import muramasa.gregtech.api.data.Machines;
import muramasa.gregtech.api.data.ItemType;
import muramasa.gregtech.api.registration.GregTechRegistrar;
import muramasa.gregtech.api.materials.Material;
import muramasa.gregtech.api.recipe.RecipeBuilder;
import muramasa.gregtech.api.util.Utils;
import net.minecraft.item.ItemStack;

import java.util.Arrays;

import static muramasa.gregtech.api.data.Materials.*;
import static muramasa.gregtech.api.materials.ItemFlag.LIQUID;
import static muramasa.gregtech.api.materials.ItemFlag.ORE;
import static muramasa.gregtech.api.materials.MaterialSet.DIAMOND;
import static muramasa.gregtech.api.materials.RecipeFlag.*;

public class ForestryRegistrar extends GregTechRegistrar {

    public static boolean EASY_COMB_RECIPES = false;

    private static RecipeBuilder RB = new RecipeBuilder();

    //TODO
    public ItemStack FR_WAX = null;

    public static Material Apatite;

    @Override
    public String getId() {
        return Ref.MOD_FR;
    }

    @Override
    public void onMaterialRegistration() {
         Apatite = new Material("Apatite", 0xc8c8ff, DIAMOND).asGemBasic(false, ORE).add(Calcium, 5, Phosphate, 3, Chlorine, 1);
    }

    @Override
    public void onMaterialInit() {
        ELEC.add(Apatite);
        NOSMELT.add(Apatite);
        NOSMASH.add(Apatite);
        CRYSTALLIZE.add(Apatite);
        Apatite.setOreMulti(4).setSmeltingMulti(4).setByProductMulti(2);
        Apatite.addByProduct(Phosphorus);
        Phosphorus.addByProduct(Apatite);

        Chrome.add(ORE);
        Osmium.add(ORE);
        Uranium235.add(ORE);
        Plutonium.add(ORE);
        Naquadria.add(ORE);
    }

    @Override
    public void onCraftingRecipeRegistration() {

    }

    @Override
    public void onMachineRecipeRegistration() {
        ItemStack comb;

        //Organic Line
        comb = ItemType.CombLignite.get(1);
        addSpecialCent(comb, new int[]{90}, Lignite.getGem(1));
        addProcessMain(comb, Lignite);
        comb = ItemType.CombCoal.get(1);
        addSpecialCent(comb, new int[]{40}, Coal.getGem(1));
        addProcessMain(comb, Coal);
        comb = ItemType.CombResin.get(1);
        addSpecialCent(comb, new int[]{70}, ItemType.StickyResin.get(1));
        comb = ItemType.CombOil.get(1);
        addSpecialCent(comb, new int[]{70}, ItemType.DropOil.get(2));
        addProcessMain(comb, Oilsands);

        //Gem Line
        //TODO duplicate recipes
        comb = ItemType.CombStone.get(1);
        addSpecialCent(comb, new int[]{70, 20, 20}, Stone.getDust(1), Salt.getDust(1), RockSalt.getDust(1));
        addProcessMain(comb, Soapstone);
        addProcess(comb, Talc);
        addProcess(comb, Apatite);
        addProcess(comb, Phosphate);
        addProcess(comb, Phosphorus);
        comb = ItemType.CombCertus.get(1);
        addProcessMain(comb, CertusQuartz);
        addProcess(comb, Quartzite);
        addProcess(comb, Barite);
        //TODO duplicate recipes
        comb = ItemType.CombRedstone.get(1);
        addProcessMain(comb, Redstone);
        addProcess(comb, Cinnabar);
        comb = ItemType.CombLapis.get(1);
        addProcessMain(comb, Lapis);
        addProcess(comb, Sodalite);
        addProcess(comb, Lazurite);
        addProcess(comb, Calcite);
        comb = ItemType.CombRuby.get(1);
        addProcessMain(comb, Ruby);
        addProcess(comb, Redstone);
        comb = ItemType.CombSapphire.get(1);
        addProcessMain(comb, Sapphire);
        addProcess(comb, GreenSapphire);
        addProcess(comb, Almandine);
        addProcess(comb, Pyrope);
        comb = ItemType.CombDiamond.get(1);
        addProcessMain(comb, Diamond);
        addProcess(comb, Graphite);
        comb = ItemType.CombOlivine.get(1);
        addProcessMain(comb, Olivine);
        addProcess(comb, Bentonite);
        addProcess(comb, Magnesite);
        addProcess(comb, Glauconite);
        comb = ItemType.CombEmerald.get(1);
        addProcessMain(comb, Emerald);
        addProcess(comb, Beryllium);
        addProcess(comb, Thorium);

        //Metals Line
        comb = ItemType.CombSlag.get(1);
        addSpecialCent(comb, new int[]{50, 20, 20}, Stone.getDust(1), GraniteBlack.getDust(1), GraniteRed.getDust(1));
        addProcessMain(comb, Salt);
        addProcess(comb, RockSalt);
        addProcess(comb, Lepidolite);
        addProcess(comb, Spodumene);
        addProcess(comb, Monazite);
        comb = ItemType.CombCopper.get(1);
        addSpecialCent(comb, new int[]{70}, Copper.getDustT(1));
        addProcessMain(comb, Copper);
        addProcess(comb, Tetrahedrite);
        addProcess(comb, Chalcopyrite);
        addProcess(comb, Malachite);
        addProcess(comb, Pyrite);
        addProcess(comb, Stibnite);
        comb = ItemType.CombTin.get(1);
        addSpecialCent(comb, new int[]{60}, Tin.getDustT(1));
        addProcessMain(comb, Tin);
        addProcess(comb, Cassiterite);
        //TODO has ironcomb recipes?
        comb = ItemType.CombLead.get(1);
        addSpecialCent(comb, new int[]{45}, Lead.getDustT(1));
        addProcessMain(comb, Lead);
        addProcess(comb, Galena);
        comb = ItemType.CombIron.get(1);
//        addProcess(comb, Iron);
        addProcessMain(comb, Magnetite);
        addProcess(comb, BrownLimonite);
        addProcess(comb, YellowLimonite);
        addProcess(comb, VanadiumMagnetite);
        addProcess(comb, BandedIron);
        addProcess(comb, Pyrite);
        //TODO GC Compat if (ProcessingModSupport.aEnableGCMarsMats) addProcess(comb, MeteoricIron);
        comb = ItemType.CombSteel.get(1);
//        addProcess(comb, Iron, Steel);
        addProcess(comb, Magnetite/*, Steel*/);
        addProcess(comb, BrownLimonite/*, Steel*/);
        addProcess(comb, YellowLimonite/*, Steel*/);
//        addProcess(comb, VanadiumMagnetite, VanadiumSteel);
        addProcess(comb, BandedIron/*, Steel*/);
        addProcess(comb, Pyrite/*, Steel*/);
        //TODO GC Compat if (ProcessingModSupport.aEnableGCMarsMats) addProcess(comb, MeteoricIron, MeteoricSteel);
        addProcessMain(comb, Molybdenite);
        addProcess(comb, Molybdenum);
        comb = ItemType.CombNickel.get(1);
        addProcessMain(comb, Nickel);
        addProcess(comb, Garnierite);
        addProcess(comb, Pentlandite);
        addProcess(comb, Cobaltite);
        addProcess(comb, Wulfenite);
        addProcess(comb, Powellite);
        comb = ItemType.CombZinc.get(1);
        addProcessMain(comb, Zinc);
        addProcess(comb, Sphalerite);
        addProcess(comb, Sulfur);
        comb = ItemType.CombSilver.get(1);
        addSpecialCent(comb, new int[]{30}, Silver.getDustT(1));
        addProcessMain(comb, Silver);
        addProcess(comb, Galena);
        comb = ItemType.CombGold.get(1);
        addProcessMain(comb, Gold);
        addProcess(comb, Magnetite, Gold);

        //Rare Metals Line
        comb = ItemType.CombAluminium.get(1);
        addProcessMain(comb, 60, Aluminium);
        addProcess(comb, Bauxite);
        comb = ItemType.CombManganese.get(1);
        addProcessMain(comb, 30, Manganese);
        addProcess(comb, Grossular);
        addProcess(comb, Spessartine);
        addProcess(comb, Pyrolusite);
        addProcess(comb, Tantalite);
        comb = ItemType.CombTitanium.get(1);
//        addProcessMain(comb, Titanium);
        addProcessMain(comb, Ilmenite);
        addProcess(comb, Bauxite);
        comb = ItemType.CombChrome.get(1);
        addProcessMain(comb, 50, Chrome);
        addProcess(comb, Ruby);
        //TODO ? addProcess(comb, Chromite, 50);
        addProcess(comb, Redstone);
        addProcess(comb, Neodymium);
        addProcess(comb, Bastnasite);
        comb = ItemType.CombTungsten.get(1);
        addProcessMain(comb, Tungstate);
        addProcess(comb, Scheelite);
        addProcess(comb, Lithium);
        comb = ItemType.CombPlatinum.get(1);
        addProcessMain(comb, 40, Platinum);
        addProcess(comb, 40, Sheldonite);
        addProcess(comb, 40, Palladium);
        comb = ItemType.CombIridium.get(1);
        addProcessMain(comb, 20, Iridium);
        addProcess(comb, 20, Osmium);

        //Radioactive Line
        comb = ItemType.CombUranium.get(1);
        addProcessMain(comb, 50, Uranium);
        addProcess(comb, 50, Pitchblende);
        addProcess(comb, 50, Uraninite);
        addProcess(comb, 50, Uranium235);
        comb = ItemType.CombPlutonium.get(1);
        addProcessMain(comb, 10, Plutonium);
        addProcess(comb, 5, Uranium235, Plutonium);
        comb = ItemType.CombNaquadah.get(1);
        addProcessMain(comb, 10, Naquadah);
        addProcess(comb, 10, NaquadahEnriched);
        addProcess(comb, 10, Naquadria);

        /*
        GT_Values.RA.addAssemblerRecipe(GT_OreDictUnificator.get(OrePrefixes.dust, Materials.Redstone, 2L), GT_OreDictUnificator.get(OrePrefixes.ingot, Materials.Copper, 5L), Materials.Glass.getMolten(72L), GT_ModHandler.getModItem(aTextForestry, "thermionicTubes", 4L, 0), 64, 32);
        GT_Values.RA.addAssemblerRecipe(GT_OreDictUnificator.get(OrePrefixes.dust, Materials.Redstone, 2L), GT_OreDictUnificator.get(OrePrefixes.ingot, Materials.AnnealedCopper, 5L), Materials.Glass.getMolten(72L), GT_ModHandler.getModItem(aTextForestry, "thermionicTubes", 4L, 0), 64, 32);
        GT_Values.RA.addAssemblerRecipe(GT_OreDictUnificator.get(OrePrefixes.dust, Materials.Redstone, 2L), GT_OreDictUnificator.get(OrePrefixes.ingot, Materials.Tin, 5L), Materials.Glass.getMolten(72L), GT_ModHandler.getModItem(aTextForestry, "thermionicTubes", 4L, 1), 64, 32);
        GT_Values.RA.addAssemblerRecipe(GT_OreDictUnificator.get(OrePrefixes.dust, Materials.Redstone, 2L), GT_OreDictUnificator.get(OrePrefixes.ingot, Materials.Bronze, 5L), Materials.Glass.getMolten(72L), GT_ModHandler.getModItem(aTextForestry, "thermionicTubes", 4L, 2), 64, 32);
        GT_Values.RA.addAssemblerRecipe(GT_OreDictUnificator.get(OrePrefixes.dust, Materials.Redstone, 2L), GT_OreDictUnificator.get(OrePrefixes.ingot, Materials.Iron, 5L), Materials.Glass.getMolten(72L), GT_ModHandler.getModItem(aTextForestry, "thermionicTubes", 4L, 3), 64, 32);
        GT_Values.RA.addAssemblerRecipe(GT_OreDictUnificator.get(OrePrefixes.dust, Materials.Redstone, 2L), GT_OreDictUnificator.get(OrePrefixes.ingot, Materials.WroughtIron, 5L), Materials.Glass.getMolten(72L), GT_ModHandler.getModItem(aTextForestry, "thermionicTubes", 4L, 3), 64, 32);
        GT_Values.RA.addAssemblerRecipe(GT_OreDictUnificator.get(OrePrefixes.dust, Materials.Redstone, 2L), GT_OreDictUnificator.get(OrePrefixes.ingot, Materials.Gold, 5L), Materials.Glass.getMolten(72L), GT_ModHandler.getModItem(aTextForestry, "thermionicTubes", 4L, 4), 64, 32);
        GT_Values.RA.addAssemblerRecipe(GT_OreDictUnificator.get(OrePrefixes.dust, Materials.Redstone, 2L), GT_OreDictUnificator.get(OrePrefixes.gem, Materials.Diamond, 5L), Materials.Glass.getMolten(72L), GT_ModHandler.getModItem(aTextForestry, "thermionicTubes", 4L, 5), 64, 32);
        GT_Values.RA.addAssemblerRecipe(GT_OreDictUnificator.get(OrePrefixes.dust, Materials.Redstone, 2L), GT_OreDictUnificator.get(OrePrefixes.dust, Materials.Blaze, 5L), Materials.Glass.getMolten(72L), GT_ModHandler.getModItem(aTextForestry, "thermionicTubes", 4L, 7), 64, 32);
        GT_Values.RA.addAssemblerRecipe(GT_OreDictUnificator.get(OrePrefixes.dust, Materials.Redstone, 2L), GT_OreDictUnificator.get(OrePrefixes.ingot, Materials.Rubber, 5L), Materials.Glass.getMolten(72L), GT_ModHandler.getModItem(aTextForestry, "thermionicTubes", 4L, 8), 64, 32);
        GT_Values.RA.addAssemblerRecipe(GT_OreDictUnificator.get(OrePrefixes.dust, Materials.Redstone, 2L), GT_OreDictUnificator.get(OrePrefixes.gem, Materials.Emerald, 5L), Materials.Glass.getMolten(72L), GT_ModHandler.getModItem(aTextForestry, "thermionicTubes", 4L, 9), 64, 32);
        GT_Values.RA.addAssemblerRecipe(GT_OreDictUnificator.get(OrePrefixes.dust, Materials.Redstone, 2L), GT_OreDictUnificator.get(OrePrefixes.gem, Materials.Apatite, 5L), Materials.Glass.getMolten(72L), GT_ModHandler.getModItem(aTextForestry, "thermionicTubes", 4L, 10), 64, 32);
        GT_Values.RA.addAssemblerRecipe(GT_OreDictUnificator.get(OrePrefixes.dust, Materials.Redstone, 2L), GT_OreDictUnificator.get(OrePrefixes.gem, Materials.Lapis, 5L), Materials.Glass.getMolten(72L), GT_ModHandler.getModItem(aTextForestry, "thermionicTubes", 4L, 11), 64, 32);
        */
    }

    public void addProcessMain(ItemStack stack, Material... materials) {
        addProcessMain(stack, 100, materials);
    }

    public void addProcessMain(ItemStack stack, int chance, Material... materials) {
        if (!EASY_COMB_RECIPES) {
            if (materials.length == 0) return;
//            FluidStack output =  ? materials[0].getByProducts().get(0).getLiquid(144) : new FluidStack[0];
            RB.get(Machines.CHEMICAL_REACTOR).ii(Utils.ca(9, stack), materials[0].getCrushed(1)).fi(Water.getLiquid(1000)).io(materials.length == 2 ? materials[1].getCrushedP(4) : materials[0].getCrushedP(4));
            if (!materials[0].getByProducts().isEmpty() && materials[0].getByProducts().get(0).has(LIQUID)) {
                RB.fo(materials[0].getByProducts().get(0).getLiquid(144)).add(96, 24);
            }
            RB.add(96, 24);
            RB.get(Machines.AUTOCLAVE).ii(Utils.ca(16, stack)).fi(UUMatter.getLiquid((int)Math.max(1, ((materials[0].getMass()+9)/10)))).io(materials[0].getCrushedP(1)).add(materials[0].getMass() * 128, 384);
        } else {
            RB.get(Machines.CENTRIFUGE).ii(stack).io(materials[0].getDustT(1), FR_WAX).chances(chance, 30).add(128, 5);
            //TODO RecipeManagers.centrifugeManager.addRecipe(40, stack, ImmutableMap.of(materials[0].getDustT(1), /* TODO chance will be wrong */chance * 0.01f, FR_WAX, 0.3f));
        }
    }

    public void addSpecialCent(ItemStack stack, int[] chances, ItemStack... outputs) {
        int[] chancesCopy = Arrays.copyOf(chances, chances.length + 1);
        chancesCopy[chances.length] = 30;
        ItemStack[] outputsCopy = Arrays.copyOf(outputs, outputs.length + 1);
        outputsCopy[outputs.length] = FR_WAX;
        RB.get(Machines.CENTRIFUGE).ii(stack).io(outputs).chances(chances).add(128, 5);
        //TODO RecipeManagers.centrifugeManager.addRecipe(40, stack, ImmutableMap.of(aOutput, chance * 0.01f, ItemList.FR_Wax.get(1, new Object[0]), 0.3f,aOutput2,chance2 * 0.01f,aOutput3,chance3*0.01f));
    }
    
    public void addProcess(ItemStack stack, Material... materials) {
        addProcess(stack, 100, materials);
    }

    public void addProcess(ItemStack stack, int chance, Material... materials) {
        return;
//        if (!EASY_COMB_RECIPES) {
//            if (materials.length == 0) return;
////            FluidStack output =  ? materials[0].getByProducts().get(0).getLiquid(144) : new FluidStack[0];
//            RB.get(Machines.CHEMICAL_REACTOR).ii(Utils.ca(9, stack), materials[0].getCrushed(1)).fi(Water.getLiquid(1000)).io(materials.length == 2 ? materials[1].getCrushedP(4) : materials[0].getCrushedP(4));
//            if (!materials[0].getByProducts().isEmpty() && materials[0].getByProducts().get(0).has(LIQUID)) {
////                RB.fo(materials[0].getByProducts().get(0).getLiquid(144)).add(96, 24);
//            }
//            RB.add(96, 24);
////            RB.get(Machines.AUTOCLAVE).ii(Utils.ca(16, stack)).fi(UUMatter.getLiquid(Math.max(1, ((materials[0].getMass()+9)/10)))).io(materials[0].getCrushedP(1)).add(materials[0].getMass() * 128, 384);
//        } else {
//            RB.get(Machines.CENTRIFUGE).ii(stack).io(materials[0].getDustT(1), FR_WAX).chances(chance, 30).add(128, 5);
//            //TODO RecipeManagers.centrifugeManager.addRecipe(40, stack, ImmutableMap.of(materials[0].getDustT(1), /* TODO chance will be wrong */chance * 0.01f, FR_WAX, 0.3f));
//        }
    }
}
