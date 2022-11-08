package muramasa.antimatter.cover;

import muramasa.antimatter.Antimatter;
import muramasa.antimatter.capability.ICoverHandler;
import muramasa.antimatter.machine.Tier;
import net.minecraft.core.Direction;
import net.minecraftforge.fluids.FluidStack;
import tesseract.FluidPlatformUtils;

import javax.annotation.Nullable;

public class CoverDebug extends BaseCover {

    public CoverDebug(ICoverHandler<?> source, @Nullable Tier tier, Direction side, CoverFactory factory) {
        super(source, tier, side, factory);
    }

    @Override
    public void onTransfer(Object object, boolean inputSide, boolean simulate) {
        if (this.handler.getTile().getLevel().isClientSide) return;
        if (!simulate) {
            String fmt = "";
            if (object instanceof FluidStack) {
                fmt = String.format("Fluid: %s, amount: %d", FluidPlatformUtils.getFluidId(((FluidStack) object).getFluid()), ((FluidStack) object).getAmount());
            } else {
                fmt = object.toString();
            }
            Antimatter.LOGGER.info(String.format("Transfer type: %s, data: %s, position: %s, side: %s", object.getClass().getSimpleName(), fmt, this.handler.getTile().getBlockPos().toString(), side));
        }
    }

    public boolean ticks() {
        return true;
    }

}
