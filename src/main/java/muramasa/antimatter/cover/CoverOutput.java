package muramasa.antimatter.cover;

import muramasa.antimatter.AntimatterAPI;
import muramasa.antimatter.Ref;
import muramasa.antimatter.machine.event.IMachineEvent;
import muramasa.antimatter.machine.event.MachineEvent;
import muramasa.antimatter.tile.TileEntityMachine;
import muramasa.antimatter.util.Utils;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;

public class CoverOutput extends Cover {

    public CoverOutput() {
        super();
        AntimatterAPI.register(Cover.class, getId(), this);
    }

    @Override
    public String getId() {
        return "output";
    }

    @Override
    public void onPlace(CoverInstance<?> cover, Direction side) {
        super.onPlace(cover, side);
        cover.getTag().putBoolean(Ref.KEY_COVER_OUTPUT, false);
    }

    @Override
    public void onMachineEvent(CoverInstance<?> cover, TileEntity tile, IMachineEvent event, Object... data) {
        //TODO: Refactor?
        if (tile instanceof TileEntityMachine) {
            TileEntityMachine machine = (TileEntityMachine) tile;
            if (event == MachineEvent.ITEMS_OUTPUTTED && cover.getTag().getBoolean(Ref.KEY_COVER_OUTPUT)) {
                Direction outputDir = machine.getOutputFacing();
                TileEntity adjTile = Utils.getTile(tile.getWorld(), tile.getPos().offset(outputDir));
                if (adjTile == null) return;
                adjTile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, outputDir.getOpposite()).ifPresent(adjHandler -> {
                    machine.itemHandler.ifPresent(h -> Utils.transferItems(h.getOutputWrapper(), adjHandler));
                });
            } else if (event == MachineEvent.FLUIDS_OUTPUTTED) {
                Direction outputDir = machine.getOutputFacing();
                TileEntity adjTile = Utils.getTile(tile.getWorld(), tile.getPos().offset(outputDir));
                if (adjTile == null) return;
                adjTile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, outputDir.getOpposite()).ifPresent(adjHandler -> {
                    machine.fluidHandler.ifPresent(h -> Utils.transferFluids(h.getOutputWrapper(), adjHandler));
                });
            }
        }
    }
}
