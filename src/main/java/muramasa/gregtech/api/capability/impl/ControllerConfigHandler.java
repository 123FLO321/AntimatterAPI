package muramasa.gregtech.api.capability.impl;

import muramasa.gregtech.api.tools.ToolType;
import muramasa.gregtech.api.tileentities.multi.TileEntityMultiMachine;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;

public class ControllerConfigHandler extends MachineConfigHandler {

    public ControllerConfigHandler(TileEntityMultiMachine tile) {
        super(tile);
    }

    @Override
    public boolean onInteract(EntityPlayer player, EnumHand hand, EnumFacing side, ToolType type) {
        if (type == ToolType.HAMMER) {
            TileEntityMultiMachine machine = (TileEntityMultiMachine) getTile();
            if (!machine.validStructure) {
                machine.checkStructure();
                return true;
            }
        }
        return super.onInteract(player, hand, side, type);
    }
}
