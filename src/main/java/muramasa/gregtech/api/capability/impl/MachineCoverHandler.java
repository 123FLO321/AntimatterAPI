package muramasa.gregtech.api.capability.impl;

import muramasa.gregtech.api.GregTechAPI;
import muramasa.gregtech.api.cover.Cover;
import muramasa.gregtech.api.tileentities.TileEntityMachine;
import net.minecraft.util.EnumFacing;

public class MachineCoverHandler extends CoverHandler {

    public MachineCoverHandler(TileEntityMachine tile) {
        //TODO add valid covers to Machine class
        super(tile, GregTechAPI.CoverPlate, GregTechAPI.CoverItem, GregTechAPI.CoverFluid, GregTechAPI.CoverEnergy);
        covers = new Cover[] {
            GregTechAPI.CoverNone, GregTechAPI.CoverNone, GregTechAPI.CoverNone, GregTechAPI.CoverItem, GregTechAPI.CoverNone, GregTechAPI.CoverNone
        };
    }

    @Override
    public boolean isValid(EnumFacing side, Cover cover) {
        return /*side != getTileFacing() &&*/ super.isValid(side, cover);
    }

    @Override
    public EnumFacing getTileFacing() {
        return ((TileEntityMachine) getTile()).getEnumFacing();
    }
}
