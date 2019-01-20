package muramasa.itech.client.model.overrides;

import com.google.common.collect.ImmutableList;
import muramasa.itech.api.machines.MachineList;
import muramasa.itech.api.machines.objects.Tier;
import muramasa.itech.common.utils.Ref;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.HashMap;

public class ItemOverrideMachine extends ItemOverrideList {

    private static HashMap<String, IBakedModel> bakedModelsItem;

    public ItemOverrideMachine(HashMap<String, IBakedModel> bakedModels) {
        super(ImmutableList.of());
        bakedModelsItem = bakedModels;
    }

    @Override
    public IBakedModel handleItemState(IBakedModel originalModel, ItemStack stack, @Nullable World world, @Nullable EntityLivingBase entity) {
        if (stack.hasTagCompound()) {
            NBTTagCompound tag = (NBTTagCompound) stack.getTagCompound().getTag(Ref.TAG_MACHINE_STACK_DATA);
            return bakedModelsItem.get(tag.getString(Ref.KEY_MACHINE_STACK_TYPE) + tag.getString(Ref.KEY_MACHINE_STACK_TIER));
        }
        return bakedModelsItem.get(MachineList.ALLOYSMELTER.getName() + Tier.LV.getName());
    }
}
