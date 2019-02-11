package muramasa.gregtech.api.machines.types;

import muramasa.gregtech.GregTech;
import muramasa.gregtech.api.machines.MachineFlag;
import muramasa.gregtech.api.machines.Tier;
import muramasa.gregtech.common.blocks.BlockHatch;
import muramasa.gregtech.common.tileentities.base.multi.TileEntityHatch;
import muramasa.gregtech.common.utils.Ref;

import static muramasa.gregtech.api.machines.MachineFlag.*;

public class HatchMachine extends Machine {

    public HatchMachine(String name, MachineFlag... extraFlags) {
        super(name, new BlockHatch(name), TileEntityHatch.class);
        setTiers(Tier.getStandard());
        addFlags(HATCH, ITEM);
        addFlags(extraFlags);
        addGUI(GregTech.INSTANCE, Ref.HATCH_ID);
    }
}
