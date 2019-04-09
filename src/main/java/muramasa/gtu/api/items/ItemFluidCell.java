package muramasa.gtu.api.items;

import muramasa.gtu.Ref;
import muramasa.gtu.api.data.Materials;
import muramasa.gtu.api.materials.ItemFlag;
import muramasa.gtu.api.materials.Material;
import muramasa.gtu.api.registration.GregTechRegistry;
import muramasa.gtu.api.registration.IHasModelOverride;
import muramasa.gtu.api.util.Utils;
import muramasa.gtu.client.render.GTModelLoader;
import muramasa.gtu.client.render.models.ModelFluidCell;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStackSimple;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class ItemFluidCell extends Item implements IHasModelOverride {

    private static int CAPACITY = 1000;

    public ItemFluidCell() {
        setUnlocalizedName("fluid_cell");
        setRegistryName("fluid_cell");
        setMaxStackSize(1);
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable NBTTagCompound nbt) {
        return new FluidHandlerItemStackSimple(stack, CAPACITY);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        FluidStack fluid = getContents(stack);
        if (fluid != null) {
            tooltip.add(fluid.getLocalizedName() + " - " + fluid.amount);
        }
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (world.isRemote) return EnumActionResult.PASS;

        TileEntity tile = Utils.getTile(world, pos);
        if (tile != null && tile.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null)) {
            System.out.println("has cap");

            IFluidHandler fluidHandler = tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null);
            IFluidHandlerItem cellHandler = player.getHeldItem(hand).getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);

            if (cellHandler.getTankProperties()[0].getContents() != null) {
                int countFilled = fluidHandler.fill(getContents(player.getHeldItem(hand)), true);
                System.out.println("fill tile: " + countFilled);
                if (countFilled == 1000) cellHandler.drain(1000, true);
            } else {
                System.out.println("drain tile");
                cellHandler.fill(fluidHandler.drain(CAPACITY, true), true);
            }
        } else {
            Material mat = Materials.getAll().toArray(new Material[0])[Ref.RNG.nextInt(Materials.getCount())];
            if (mat != null && mat.has(ItemFlag.LIQUID)) {
                player.setHeldItem(hand, getCellWithFluid(mat.getLiquid()));
            }
        }
        return EnumActionResult.SUCCESS;
    }

    @Nullable
    public static FluidStack getContents(ItemStack stack) {
        IFluidHandlerItem handler = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
        return handler.getTankProperties()[0].getContents();
    }

    public static ItemStack getCellWithFluid(Fluid fluid) {
        ItemStack stack = new ItemStack(GregTechRegistry.getItem("fluid_cell"));
        IFluidHandlerItem handler = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
        handler.fill(new FluidStack(fluid, CAPACITY), true);
        return stack;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void initModel() {
        ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(getRegistryName(), "inventory"));
        GTModelLoader.register(getRegistryName().getResourcePath(), new ModelFluidCell());
    }
}
