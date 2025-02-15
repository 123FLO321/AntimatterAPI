package muramasa.antimatter.mixin;

import muramasa.antimatter.datagen.AntimatterDynamics;
import net.minecraft.commands.Commands;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.ReloadableServerResources;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(ReloadableServerResources.class)
public class SimpleReloadInstanceMixin {


    //Since tag event doesn't include recipe manager.
    @Inject(method = "updateRegistryTags(Lnet/minecraft/core/RegistryAccess;)V", at = @At(value = "TAIL"))
    public void onUpdateTags(RegistryAccess p_206869, CallbackInfo info) {
        ReloadableServerResources rs = (ReloadableServerResources) (Object) this;
        AntimatterDynamics.onRecipeCompile(true, rs.getRecipeManager());
    }
}
