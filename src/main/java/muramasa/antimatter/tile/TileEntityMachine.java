package muramasa.antimatter.tile;

import muramasa.antimatter.AntimatterAPI;
import muramasa.antimatter.AntimatterProperties;
import muramasa.antimatter.Data;
import muramasa.antimatter.Ref;
import muramasa.antimatter.capability.AntimatterCaps;
import muramasa.antimatter.capability.impl.*;
import muramasa.antimatter.cover.Cover;
import muramasa.antimatter.gui.SlotType;
import muramasa.antimatter.machine.BlockMachine;
import muramasa.antimatter.machine.MachineFlag;
import muramasa.antimatter.machine.MachineState;
import muramasa.antimatter.machine.Tier;
import muramasa.antimatter.machine.event.IMachineEvent;
import muramasa.antimatter.machine.types.Machine;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import tesseract.util.Dir;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

import static muramasa.antimatter.machine.MachineFlag.*;

public class TileEntityMachine extends TileEntityTickable implements INamedContainerProvider {

    /** NBT Data **/ //TODO move to caps
    protected CompoundNBT itemData, fluidData;

    /** Machine Data **/
    protected Machine<?> type;
    private MachineState machineState;

    /** Client Data **/
    protected float clientProgress = 0; //TODO look into receiveClientEvent

    /** Capabilities **/
    public Optional<MachineItemHandler> itemHandler = Optional.empty();
    public Optional<MachineFluidHandler> fluidHandler = Optional.empty();
    public Optional<MachineRecipeHandler<?>> recipeHandler = Optional.empty();
    public Optional<MachineEnergyHandler> energyHandler = Optional.empty();
    public Optional<MachineCoverHandler> coverHandler = Optional.empty();
    public Optional<MachineConfigHandler> configHandler = Optional.empty();

    public TileEntityMachine(TileEntityType<?> tileType) {
        super(tileType);
        machineState = getDefaultMachineState();
    }

    public TileEntityMachine(Machine<?> type) {
        this(type.getTileType());
        this.type = type;
    }

    @Override
    public void onInit() {
        if (!itemHandler.isPresent() && has(ITEM) && getMachineType().getGui().hasAnyItem(getMachineTier())) itemHandler = Optional.of(new MachineItemHandler(this, itemData));
        if (!fluidHandler.isPresent() && has(FLUID) && getMachineType().getGui().hasAnyFluid(getMachineTier())) fluidHandler = Optional.of(new MachineFluidHandler(this, fluidData));
        if (!coverHandler.isPresent() && has(COVERABLE)) coverHandler = Optional.of(new MachineCoverHandler(this));
        if (!energyHandler.isPresent() && has(ENERGY)) energyHandler = Optional.of(new MachineEnergyHandler(this));
        if (!configHandler.isPresent() && has(CONFIGURABLE)) configHandler = Optional.of(new MachineConfigHandler(this));
        if (!recipeHandler.isPresent() && has(RECIPE)) recipeHandler = Optional.of(new MachineRecipeHandler<>(this));
    }

    @Override
    public void onRemove() {
        energyHandler.ifPresent(MachineEnergyHandler::onRemove);
        fluidHandler.ifPresent(MachineFluidHandler::onRemove);
        itemHandler.ifPresent(MachineItemHandler::onRemove);
        coverHandler.ifPresent(CoverHandler::onRemove);
    }

    // Should be called on the rotation or cover changes to update connections
    //public void onReset() {
    //    energyHandler.ifPresent(MachineEnergyHandler::onReset);
    //    fluidHandler.ifPresent(MachineFluidHandler::onReset);
    //    itemHandler.ifPresent(MachineItemHandler::onReset);
    //}

    @Override
    public void onServerUpdate() {
        recipeHandler.ifPresent(MachineRecipeHandler::onUpdate);
        fluidHandler.ifPresent(MachineFluidHandler::onUpdate);
        itemHandler.ifPresent(MachineItemHandler::onUpdate);
        coverHandler.ifPresent(CoverHandler::onUpdate);
    }

    public void onMachineEvent(IMachineEvent event, Object... data) {
        recipeHandler.ifPresent(h -> h.onMachineEvent(event, data));
        coverHandler.ifPresent(h -> h.onMachineEvent(event, data));
    }

    /** Getters **/
    public Machine<?> getMachineType() {
        return type != null ? type : ((BlockMachine) getBlockState().getBlock()).getType();
    }

    public Tier getMachineTier() {
        return ((BlockMachine) getBlockState().getBlock()).getTier();
    }

    public boolean has(MachineFlag flag) {
        return getMachineType().has(flag);
    }

    public Direction getFacing() {
        return getBlockState().get(BlockStateProperties.HORIZONTAL_FACING);
    }

    public Direction getOutputFacing() {
        return coverHandler.map(MachineCoverHandler::getOutputFacing).orElse(getFacing().getOpposite());
    }

    public MachineState getMachineState() {
        return machineState;
    }

    public MachineState getDefaultMachineState() {
        return MachineState.IDLE;
    }

    public int getMaxInputVoltage() {
        return energyHandler.map(EnergyHandler::getInputVoltage).orElse(0);
    }

    public Cover getCover(Direction side) {
        return coverHandler.map(h -> h.getCover(side)).orElse(Data.COVER_NONE);
    }

    //TODO
    public void toggleDisabled() {
        setMachineState(machineState == MachineState.DISABLED ? MachineState.IDLE : MachineState.DISABLED);
    }

    /** Helpers **/
    public void resetMachine() {
        setMachineState(getDefaultMachineState());
        recipeHandler.ifPresent(MachineRecipeHandler::resetRecipe);
    }

    public void setMachineState(MachineState newState) {
        if (machineState.getOverlayId() != newState.getOverlayId() && newState.allowRenderUpdate()) {
            markForRenderUpdate();
            System.out.println("RENDER UPDATE");
        }
        machineState = newState;
    }

    public Cover[] getValidCovers() {
        return AntimatterAPI.getRegisteredCovers().toArray(new Cover[0]);
    }

    public float getClientProgress() {
        return clientProgress;
    }

    public TileEntityMachine asMachine() {
        return this;
    }

    @Nonnull
    @Override
    public IModelData getModelData() {
        ModelDataMap.Builder builder = new ModelDataMap.Builder().withInitial(AntimatterProperties.MACHINE_TYPE, getMachineType());
        coverHandler.ifPresent(machineCoverHandler -> builder.withInitial(AntimatterProperties.MACHINE_COVER, machineCoverHandler.getAll()));
        return builder.build();
    }

    @Nonnull
    @Override
    public ITextComponent getDisplayName() {
        return getMachineType().getDisplayName(getMachineTier());
    }

    @Nullable
    @Override
    public Container createMenu(int windowId, @Nonnull PlayerInventory inv, @Nonnull PlayerEntity player) {
        return getMachineType().has(GUI) ? getMachineType().getGui().getMenuHandler().getMenu(this, inv, windowId) : null;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap) {
        if ((cap == AntimatterCaps.ENERGY || cap == CapabilityEnergy.ENERGY) && energyHandler.isPresent()) return LazyOptional.of(() -> energyHandler.get()).cast();
        else if (cap == AntimatterCaps.CONFIGURABLE && configHandler.isPresent()) return LazyOptional.of(() -> configHandler.get()).cast();
        return super.getCapability(cap);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && itemHandler.isPresent()) return LazyOptional.of(() -> itemHandler.get().getHandlerForSide(side)).cast();
        else if (cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && fluidHandler.isPresent()) return LazyOptional.of(() -> fluidHandler.get().getWrapperForSide(side)).cast();
        else if ((cap == AntimatterCaps.ENERGY || cap == CapabilityEnergy.ENERGY) && energyHandler.isPresent()) return LazyOptional.of(() -> energyHandler.get()).cast();
        else if (cap == AntimatterCaps.COVERABLE && coverHandler.map(h -> h.getCover(side).isEmpty()).orElse(false)) return LazyOptional.of(() -> coverHandler.get()).cast();
        else if (cap == AntimatterCaps.CONFIGURABLE && configHandler.isPresent()) return LazyOptional.of(() -> configHandler.get()).cast();
        return super.getCapability(cap, side);
    }

    @Override
    public void read(CompoundNBT tag) {
        super.read(tag);
        if (tag.contains(Ref.KEY_MACHINE_TILE_STATE)) machineState = MachineState.VALUES[tag.getInt(Ref.KEY_MACHINE_TILE_STATE)];//TODO saving state needed? if recipe is saved, serverUpdate should handle it.
        if (tag.contains(Ref.KEY_MACHINE_TILE_ITEMS)) itemData = tag.getCompound(Ref.KEY_MACHINE_TILE_ITEMS);
        if (tag.contains(Ref.KEY_MACHINE_TILE_FLUIDS)) fluidData = tag.getCompound(Ref.KEY_MACHINE_TILE_FLUIDS);
    }

    @Nonnull
    @Override
    public CompoundNBT write(CompoundNBT tag) {
        super.write(tag); //TODO get tile data tag
        if (machineState != null) tag.putInt(Ref.KEY_MACHINE_TILE_STATE, machineState.ordinal());
        itemHandler.ifPresent(h -> tag.put(Ref.KEY_MACHINE_TILE_ITEMS, h.serialize()));
        fluidHandler.ifPresent(h -> tag.put(Ref.KEY_MACHINE_TILE_FLUIDS, h.serialize()));
        return tag;
    }

    //TODO move toString to capabilities
    @Override
    public List<String> getInfo() {
        List<String> info = super.getInfo();
        info.add("Machine: " + getMachineType().getId() + " Tier: " + getMachineTier().getId());
        String slots = "";
        if (getMachineType().has(ITEM)) {
            int inputs = getMachineType().getGui().getSlots(SlotType.IT_IN, getMachineTier()).size();
            int outputs = getMachineType().getGui().getSlots(SlotType.IT_OUT, getMachineTier()).size();
            if (inputs > 0) slots += (" IT_IN: " + inputs + ",");
            if (outputs > 0) slots += (" IT_OUT: " + outputs + ",");
        }
        if (getMachineType().has(FLUID)) {
            int inputs = getMachineType().getGui().getSlots(SlotType.FL_IN, getMachineTier()).size();
            int outputs = getMachineType().getGui().getSlots(SlotType.FL_OUT, getMachineTier()).size();
            if (inputs > 0) slots += (" FL_IN: " + inputs + ",");
            if (outputs > 0) slots += (" FL_OUT: " + outputs + ",");
        }
        if (slots.length() > 0) info.add("Slots:" + slots);
        energyHandler.ifPresent(h -> info.add("Energy: " + h.getEnergyStored() + " / " + h.getMaxEnergyStored()));
        coverHandler.ifPresent(h -> {
            StringBuilder builder = new StringBuilder("Covers: ");
            for (int i = 0; i < 6; i++) {
                builder.append(h.getCover(Ref.DIRECTIONS[i]).getId()).append(" ");
            }
            info.add(builder.toString());
        });
        recipeHandler.ifPresent(h -> info.add("Recipe: " + h.getCurProgress() + " / " + h.getMaxProgress()));
        return info;
    }
}