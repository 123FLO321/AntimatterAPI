package muramasa.antimatter.util.fabric;

import net.fabricmc.loader.api.FabricLoader;

public class AntimatterPreLaunchUtilImpl {
    public static boolean isModLoaded(String modid){
        return FabricLoader.getInstance().isModLoaded(modid);
    }
}
