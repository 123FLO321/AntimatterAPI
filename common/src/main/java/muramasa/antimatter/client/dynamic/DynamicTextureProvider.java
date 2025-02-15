package muramasa.antimatter.client.dynamic;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import muramasa.antimatter.Ref;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.IModelData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;
import java.util.WeakHashMap;
import java.util.function.Function;

public class DynamicTextureProvider<T extends IDynamicModelProvider, U> {


    //Data required to assemble the model.
    public class BuilderData {
        public Random rand;
        @Nullable
        public BlockState state;
        public IModelData data;
        public T source;
        public U key;
        public Direction currentDir;
        public String type;

        public BuilderData(String type, Random r, BlockState s, IModelData d, T t, U u, Direction dir) {
            rand = r;
            state = s;
            data = d;
            source = t;
            this.key = u;
            this.currentDir = dir;
            this.type = type;
        }
    }

    //The weak-reference backed hashmap.
    protected Object2ObjectMap<String, WeakHashMap<U, List<BakedQuad>[]>> MODEL_CACHE = new Object2ObjectOpenHashMap<>();

    private final Function<BuilderData, List<BakedQuad>> builder;

    public DynamicTextureProvider(@Nonnull Function<BuilderData, List<BakedQuad>> builder) {
        this.builder = builder;
    }

    public List<BakedQuad>[] getQuads(String type, BlockState state, T t, U key, IModelData data) {
      //  return MODEL_CACHE.compute(t.getId(), (k, v) -> {
     //       if (v == null) v = new WeakHashMap<>();
    //        v.computeIfAbsent(key, (k1) -> bakeQuads(type, state, t, key, data));
    //        return v;
     //  }).get(key);
     return bakeQuads(type, state, t, key, data);
    }

    private List<BakedQuad>[] bakeQuads(String type, BlockState state, T c, U key, IModelData data) {
        List<BakedQuad>[] bakedArray = new List[Ref.DIRS.length];
        for (Direction dir : Ref.DIRS) {
            bakedArray[dir.get3DDataValue()] = builder.apply(new BuilderData(type, Ref.RNG, state, data, c, key, dir));
        }
        return bakedArray;
    }
}
