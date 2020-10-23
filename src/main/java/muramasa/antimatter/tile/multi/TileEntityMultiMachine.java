package muramasa.antimatter.tile.multi;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import muramasa.antimatter.Ref;
import muramasa.antimatter.capability.AntimatterCaps;
import muramasa.antimatter.capability.EnergyHandler;
import muramasa.antimatter.capability.IComponentHandler;
import muramasa.antimatter.capability.machine.*;
import muramasa.antimatter.gui.event.IGuiEvent;
import muramasa.antimatter.machine.MachineFlag;
import muramasa.antimatter.machine.MachineState;
import muramasa.antimatter.machine.types.Machine;
import muramasa.antimatter.registration.IAntimatterObject;
import muramasa.antimatter.structure.IComponent;
import muramasa.antimatter.structure.Structure;
import muramasa.antimatter.structure.StructureCache;
import muramasa.antimatter.structure.StructureResult;
import muramasa.antimatter.texture.Texture;
import muramasa.antimatter.tile.TileEntityMachine;
import muramasa.antimatter.util.LazyHolder;
import muramasa.antimatter.util.Utils;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static muramasa.antimatter.machine.MachineFlag.*;

public class TileEntityMultiMachine extends TileEntityMachine implements IComponent {

    protected int efficiency, efficiencyIncrease; //TODO move to BasicMachine
    protected long EUt;

    protected final LazyOptional<ControllerComponentHandler> componentHandler = LazyOptional.of(() -> new ControllerComponentHandler(this));

    protected Optional<StructureResult> result = Optional.empty();

    public TileEntityMultiMachine(Machine<?> type) {
        super(type);
        this.itemHandler = type.has(ITEM) ? LazyHolder.of(() -> new MultiMachineItemHandler(this)) : LazyHolder.empty();
        this.energyHandler = type.has(ENERGY) ? LazyHolder.of(() -> new MultiMachineEnergyHandler(this)) : LazyHolder.empty();
    }

    @Override
    public void onRemove() {
        super.onRemove();
        invalidateStructure();
    }

    @Override
    public void onFirstTick() {
        super.onFirstTick();
        if (!isStructureValid()) {
            if (checkStructure()) {
                recipeHandler.ifPresent(MachineRecipeHandler::scheduleCheck);
            }
        }
    }

    public boolean checkStructure() {
        if (!isServerSide()) return false;
        Structure structure = getMachineType().getStructure(getMachineTier());
        if (structure == null) return false;
        StructureResult result = structure.evaluate(this);
        if (result.evaluate()) {
            this.result = Optional.of(result);
            if (onStructureFormed()) {
                StructureCache.add(world, pos, result.positions);
                this.result.ifPresent(r -> r.components.forEach((k, v) -> v.forEach(c -> c.onStructureFormed(this))));
                //Handlers.
                this.itemHandler.ifPresent(handle -> {
                    ((MultiMachineItemHandler)handle).onStructureBuild();
                });
                this.energyHandler.ifPresent(handle -> {
                    ((MultiMachineEnergyHandler)handle).onStructureBuild();
                });
                setMachineState(MachineState.IDLE);
                System.out.println("[Structure Debug] Valid Structure");
                setTileTextures();
                return true;
            }
        }
        this.result = Optional.empty();
        System.out.println("[Structure Debug] Invalid Structure" + result.getError());
        return false;
    }

    protected void setTileTextures() {
        this.result.ifPresent(t -> t.components.forEach((k, v) -> v.forEach(tile -> {
            TileEntity optionalTile = tile.getTile();
            if (optionalTile instanceof TileEntityMachine) {
                ((TileEntityMachine)optionalTile).setMultiTexture(this.getMachineType().getBaseTexture(tier));
            }
        })));
    }

    @Override
    public boolean setFacing(Direction side) {
        boolean ok = super.setFacing(side);
        if (ok) {
            checkStructure();
        }
        return ok;
    }

    public void invalidateStructure() {
        if (removed) return;
        result.ifPresent(r -> r.components.forEach((k, v) -> v.forEach(c -> {
            c.onStructureInvalidated(this);
            if (c.getTile() instanceof TileEntityMachine) {
                TileEntityMachine m = (TileEntityMachine) c.getTile();
                m.resetMultiTexture();
            }
        })));
        this.itemHandler.ifPresent(handle -> ((MultiMachineItemHandler)handle).invalidate());
        this.energyHandler.ifPresent(handle -> ((MultiMachineEnergyHandler)handle).invalidate());
        result = Optional.empty();
        resetMachine();
        System.out.println("INVALIDATED STRUCTURE");
        onStructureInvalidated();
    }

    /** Returns a list of Components **/
    public List<IComponentHandler> getComponents(IAntimatterObject object) {
        return getComponents(object.getId());
    }

    public List<IComponentHandler> getComponents(String id) {
        if (result.isPresent()) {
            List<IComponentHandler> list = result.get().components.get(id);
            return list != null ? list : Collections.emptyList();
        }
        return Collections.emptyList();
    }

    public List<BlockState> getStates(String id) {
        if (result.isPresent()) {
            List<BlockState> list = result.get().states.get(id);
            return list != null ? list : Collections.emptyList();
        }
        return Collections.emptyList();
    }

    public boolean isStructureValid() {
        return StructureCache.has(world, pos);
    }

    /** Events **/
    public boolean onStructureFormed() {
        return true;
    }

    public void onStructureInvalidated() {
        //NOOP
    }

    @Override
    public void onGuiEvent(IGuiEvent event, int... data) {
        /*if (event == GuiEvent.MULTI_ACTIVATE) {
            checkStructure();
            recipeHandler.ifPresent(MachineRecipeHandler::checkRecipe);
        }*/
    }

    /** Returns list of items across all input hatches. Merges equal filters empty **/
    public ItemStack[] getStoredItems() {
        if (!has(MachineFlag.ITEM)) return new ItemStack[0];
        List<ItemStack> all = new ObjectArrayList<>();
        for (IComponentHandler hatch : getComponents("hatch_item_input")) {
            hatch.getItemHandler().ifPresent(h -> Utils.mergeItems(all, h.getInputList()));
        }
        System.out.println(all.toString());
        return all.toArray(new ItemStack[0]);
    }

    /** Returns list of fluids across all input hatches. Merges equal filters empty **/
    public FluidStack[] getStoredFluids() {
        if (!has(MachineFlag.FLUID)) return new FluidStack[0];
        List<FluidStack> all = new ObjectArrayList<>();
        for (IComponentHandler hatch : getComponents("hatch_fluid_input")) {
            hatch.getFluidHandler().ifPresent(h -> Utils.mergeFluids(all, Arrays.asList(h.getInputs())));
        }
        System.out.println(all.toString());
        return all.toArray(new FluidStack[0]);
    }

    /** Returns the total energy stored across all energy hatches **/
    public long getStoredEnergy() {
        long total = 0;
        for (IComponentHandler hatch : getComponents("hatch_energy")) {
            if (hatch.getEnergyHandler().isPresent()) total += hatch.getEnergyHandler().map(MachineEnergyHandler::getEnergyStored).orElse(0);
        }
        return total;
    }

    /** Consumes inputs from all input hatches. Assumes Utils.doItemsMatchAndSizeValid has been used **/
    public void consumeItems(ItemStack[] items) {
        if (items == null) return;
        for (IComponentHandler hatch : getComponents("hatch_item_input")) {
            if (hatch.getItemHandler().isPresent()) {
                ItemStack[] finalItems = items;
                items = hatch.getItemHandler().map(ih -> ih.consumeAndReturnInputs(finalItems.clone())).orElse(new ItemStack[0]);
                if (items.length == 0) break;
            }
        }
        if (items.length > 0) System.out.println("DID NOT CONSUME ALL: " + Arrays.toString(items));
    }

    /** Consumes inputs from all input hatches. Assumes Utils.doFluidsMatchAndSizeValid has been used **/
    public void consumeFluids(FluidStack[] fluids) {
        if (fluids == null) return;
        for (IComponentHandler hatch : getComponents("hatch_fluid_input")) {
            if (hatch.getFluidHandler().isPresent()) {
                FluidStack[] finalFluids = fluids;
                fluids = hatch.getFluidHandler().map(fh -> fh.consumeAndReturnInputs(finalFluids.clone())).orElse(new FluidStack[0]);
                if (fluids.length == 0) break;
            }
        }
        if (fluids.length > 0) System.out.println("DID NOT CONSUME ALL: " + Arrays.toString(fluids));
    }

    /** Consumes energy from all energy hatches. Assumes enough energy is present in hatches **/
    public void consumeEnergy(long energy) {
        if (energy <= 0) return;
        for (IComponentHandler hatch : getComponents("hatch_energy")) {
            if (hatch.getEnergyHandler().isPresent()) {
                long finalEnergy = energy;
                energy -= hatch.getEnergyHandler().map(eh -> eh.extract(finalEnergy, false)).orElse(0L);
                if (energy == 0) break;
            }
        }
    }

    /** Export items to hatches regardless of space. Assumes canOutputsFit has been used **/
    public void outputItems(ItemStack[] items) {
        if (items == null) return;
        for (IComponentHandler hatch : getComponents("hatch_item_output")) {
            if (hatch.getItemHandler().isPresent()) {
                ItemStack[] finalItems = items;
                items = hatch.getItemHandler().map(ih -> ih.exportAndReturnOutputs(finalItems.clone())).orElse(new ItemStack[0]); //WHY CLONE?!!?
                if (items.length == 0) break;
            }
        }
        if (items.length > 0) System.out.println("HATCH OVERFLOW: " + Arrays.toString(items));
    }

    /** Export fluids to hatches regardless of space. Assumes canOutputsFit has been used **/
    public void outputFluids(FluidStack[] fluids) {
        if (fluids == null) return;
        for (IComponentHandler hatch : getComponents("hatch_fluid_output")) {
            if (hatch.getFluidHandler().isPresent()) {
                FluidStack[] finalFluids = fluids;
                fluids = hatch.getFluidHandler().map(fh -> fh.exportAndReturnOutputs(finalFluids.clone())).orElse(new FluidStack[0]);
                if (fluids.length == 0) break;
            }
        }
        if (fluids.length > 0) System.out.println("HATCH OVERFLOW: " + Arrays.toString(fluids));
    }

    /** Tests if items can fit across all output hatches **/
    public boolean canItemsFit(ItemStack[] items) {
        if (items == null) return true;
        int matchCount = 0;
        for (IComponentHandler hatch : getComponents("hatch_item_output")) {
            if (hatch.getItemHandler().isPresent()) {
                matchCount += hatch.getItemHandler().map(ih -> ih.getSpaceForOutputs(items)).orElse(0);
            }
        }
        return matchCount >= items.length;
    }

    /** Tests if fluids can fit across all output hatches **/
    public boolean canFluidsFit(FluidStack[] fluids) {
        if (fluids == null) return true;
        int matchCount = 0;
        for (IComponentHandler hatch : getComponents("hatch_fluid_output")) {
            if (hatch.getFluidHandler().isPresent()) {
                matchCount += hatch.getFluidHandler().map(fh -> fh.getSpaceForOutputs(fluids)).orElse(0);
            }
        }
        return matchCount >= fluids.length;
    }

    @Override
    public int getMaxInputVoltage() {
        List<IComponentHandler> hatches = getComponents("hatch_energy");
        return hatches.size() >= 1 ? hatches.get(0).getEnergyHandler().map(EnergyHandler::getInputVoltage).orElse(Ref.V[0]) : Ref.V[0];
    }

    @Override
    public LazyOptional<ControllerComponentHandler> getComponentHandler() {
        return componentHandler;
    }

    @Override
    public MachineState getDefaultMachineState() {
        return MachineState.INVALID_STRUCTURE;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, Direction side) {
        if (cap == AntimatterCaps.COMPONENT_HANDLER_CAPABILITY && componentHandler.isPresent()) {
            return componentHandler.cast();
        }
        return super.getCapability(cap, side);
    }
}
