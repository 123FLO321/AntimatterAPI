package muramasa.antimatter.datagen.providers;

import muramasa.antimatter.AntimatterAPI;
import muramasa.antimatter.datagen.IAntimatterProvider;
import muramasa.antimatter.fluid.AntimatterFluid;
import muramasa.antimatter.fluid.AntimatterMaterialFluid;
import muramasa.antimatter.material.Material;
import net.minecraft.core.Registry;
import net.minecraft.world.level.material.Fluid;

import static muramasa.antimatter.util.TagUtils.getForgelikeFluidTag;

public class AntimatterFluidTagProvider extends AntimatterTagProvider<Fluid> implements IAntimatterProvider {

    private final boolean replace;

    public AntimatterFluidTagProvider(String providerDomain, String providerName, boolean replace) {
        super(Registry.FLUID, providerDomain, providerName, "fluids");
        this.replace = replace;
    }

    protected void processTags(String domain) {
        AntimatterAPI.all(AntimatterFluid.class, domain).forEach(f -> {
            tag(getForgelikeFluidTag(f.getId()))
                    .add(f.getFluid(), f.getFlowingFluid())
                    .replace(replace);
            if (f instanceof AntimatterMaterialFluid) {
                Material m = ((AntimatterMaterialFluid) f).getMaterial();
                tag(getForgelikeFluidTag(m.getId()))
                        .add(f.getFluid(), f.getFlowingFluid())
                        .replace(replace);
            }
        });
    }
}
