package muramasa.antimatter.recipe;

import muramasa.antimatter.recipe.ingredient.RecipeIngredient;
import muramasa.antimatter.recipe.map.RecipeBuilder;
import muramasa.antimatter.recipe.map.RecipeMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.List;
import java.util.function.BiFunction;

public class RecipeProxies {

    private static final BiFunction<Recipe<?>, RecipeBuilder, muramasa.antimatter.recipe.Recipe> getDefault(int power, int duration) {
        return (t, b) -> {
            List<Ingredient> ingredients = t.getIngredients();
            Ingredient input = ingredients.get(0);
            ItemStack[] stacks = input.getItems();
            RecipeIngredient ing = stacks.length == 1 ? RecipeIngredient.of(stacks[0]) : RecipeIngredient.of(1, input.getItems());
            return b.ii(ing)
                    .io(t.getResultItem()).build(duration, power, 0, 1);
        };
    }

    /*
    private static Function<IRecipe, Recipe> CRAFTING = t -> {
        if (t instanceof ShapedRecipe) return null;
        List<Ingredient> ingredients = t.getIngredients();
        if (ingredients.size() > 6 || ingredients.size() < 2) return null;
        List<ItemStack> list = new ObjectArrayList<>();
        List<LazyValue<AntimatterIngredient>> ings = new ObjectArrayList<>();
        for (Ingredient i : ingredients) {
            ItemStack[] stacks = i.getMatchingStacks();
            if (stacks.length == 0) return null;
            if (stacks.length == 1) {
                list.add(stacks[0]);
            } else {
                LazyValue<AntimatterIngredient> ing = AntimatterIngredient.of(1, stacks);
                ings.add(ing);
            }
        }
        ItemStack[] stacks = RecipeMap.uniqueItems(list.toArray(new ItemStack[0]));
        for (ItemStack stack : stacks) {
            ings.add(AntimatterIngredient.of(stack));
        }
        return new RecipeBuilder().ii(ings)
                .io(t.getRecipeOutput()).build(60, 8,0, 1);
    };
*/
    public static BiFunction<Integer, Integer, RecipeMap.Proxy> FURNACE_PROXY = (power, duration) -> new RecipeMap.Proxy(RecipeType.SMELTING, getDefault(power, duration));
    public static BiFunction<Integer, Integer, RecipeMap.Proxy> BLASTING_PROXY = (power, duration) -> new RecipeMap.Proxy(RecipeType.BLASTING, getDefault(power, duration));
    public static BiFunction<Integer, Integer, RecipeMap.Proxy> SMOKING_PROXY = (power, duration) -> new RecipeMap.Proxy(RecipeType.SMOKING, getDefault(power, duration));
    //public static RecipeMap.Proxy CRAFTING_PROXY = new RecipeMap.Proxy(IRecipeType.CRAFTING, CRAFTING);
}