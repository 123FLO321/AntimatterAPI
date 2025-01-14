package muramasa.antimatter.client.baked;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.IModelData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class BakedMachineSide extends GroupedBakedModel{
    public BakedMachineSide(TextureAtlasSprite p, Map<String, BakedModel> models) {
        super(p, models);
    }

    @Override
    public List<BakedQuad> getBlockQuads(BlockState state, @Nullable Direction side, @Nonnull Random rand, @Nonnull IModelData data) {
        return this.models.get("").getQuads(state, side, rand, data);
    }

    @Override
    public List<BakedQuad> getItemQuads(@Nullable Direction side, @Nonnull Random rand, @Nonnull IModelData data) {
        return this.models.get("").getQuads(null, side, rand, data);
    }

    @Override
    public boolean usesBlockLight() {
        return false;
    }

    
}
