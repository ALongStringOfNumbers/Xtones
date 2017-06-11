package tehnut.xtones;

import com.google.common.collect.Maps;
import net.minecraft.block.Block;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.OreIngredient;

import javax.annotation.Nonnull;
import java.util.HashMap;

// TODO - Remove when Forge updates
//This is ShapedOreRecipe modified to actually work until forge re-fixes it in an update.
public class FixedShapedOreRecipe implements IRecipe {
    //Added in for future ease of change, but hard coded for now.
    public static final int MAX_CRAFT_GRID_WIDTH = 3;
    public static final int MAX_CRAFT_GRID_HEIGHT = 3;

    @Nonnull
    protected ItemStack output = ItemStack.EMPTY;
    protected NonNullList<Ingredient> input = null;
    protected int width = 0;
    protected int height = 0;
    protected boolean mirrored = true;
    protected ResourceLocation group;

    public FixedShapedOreRecipe(ResourceLocation group, @Nonnull ItemStack result, Object... recipe) {
        this.group = group;
        output = result.copy();

        String shape = "";
        int idx = 0;

        if (recipe[idx] instanceof Boolean) {
            mirrored = (Boolean) recipe[idx];
            if (recipe[idx + 1] instanceof Object[]) {
                recipe = (Object[]) recipe[idx + 1];
            } else {
                idx = 1;
            }
        }

        if (recipe[idx] instanceof String[]) {
            String[] parts = ((String[]) recipe[idx++]);

            for (String s : parts) {
                width = s.length();
                shape += s;
            }

            height = parts.length;
        } else {
            while (recipe[idx] instanceof String) {
                String s = (String) recipe[idx++];
                shape += s;
                width = s.length();
                height++;
            }
        }

        if (width * height != shape.length()) {
            String ret = "Invalid shaped ore recipe: ";
            for (Object tmp : recipe) {
                ret += tmp + ", ";
            }
            ret += output;
            throw new RuntimeException(ret);
        }

        HashMap<Character, Ingredient> itemMap = Maps.newHashMap();

        for (; idx < recipe.length; idx += 2) {
            Character chr = (Character) recipe[idx];
            Object in = recipe[idx + 1];

            if (in instanceof ItemStack) {
                itemMap.put(chr, Ingredient.func_193369_a(((ItemStack) in).copy()));
            } else if (in instanceof Item) {
                itemMap.put(chr, Ingredient.func_193367_a((Item) in));
            } else if (in instanceof Block) {
                itemMap.put(chr,
                        Ingredient.func_193369_a(new ItemStack((Block) in, 1, OreDictionary.WILDCARD_VALUE)));
            } else if (in instanceof String) {
                itemMap.put(chr, new OreIngredient((String) in));
            } else if (in instanceof Ingredient) {
                itemMap.put(chr, (Ingredient) in);
            } else {
                String ret = "Invalid shaped ore recipe: ";
                for (Object tmp : recipe) {
                    ret += tmp + ", ";
                }
                ret += output;
                throw new RuntimeException(ret);
            }
        }

        this.input = NonNullList.withSize(width * height, Ingredient.field_193370_a);
        int x = 0;
        for (char chr : shape.toCharArray()) {
            if (itemMap.get(chr) != null)
                input.set(x, itemMap.get(chr));
            x++;
        }
    }

    /**
     * Returns an Item that is the result of this recipe
     */
    @Override
    @Nonnull
    public ItemStack getCraftingResult(@Nonnull InventoryCrafting var1) {
        return output.copy();
    }

    @Override
    @Nonnull
    public ItemStack getRecipeOutput() {
        return output;
    }

    /**
     * Used to check if a recipe matches current crafting inventory
     */
    @Override
    public boolean matches(InventoryCrafting inv, World world) {
        for (int x = 0; x <= MAX_CRAFT_GRID_WIDTH - width; x++) {
            for (int y = 0; y <= MAX_CRAFT_GRID_HEIGHT - height; ++y) {
                if (checkMatch(inv, x, y, false)) {
                    return true;
                }

                if (mirrored && checkMatch(inv, x, y, true)) {
                    return true;
                }
            }
        }

        return false;
    }

    protected boolean checkMatch(InventoryCrafting inv, int startX, int startY, boolean mirror) {
        for (int x = 0; x < MAX_CRAFT_GRID_WIDTH; x++) {
            for (int y = 0; y < MAX_CRAFT_GRID_HEIGHT; y++) {
                int subX = x - startX;
                int subY = y - startY;
                Ingredient target = null;

                if (subX >= 0 && subY >= 0 && subX < width && subY < height) {
                    if (mirror) {
                        target = input.get(width - subX - 1 + subY * width);
                    } else {
                        target = input.get(subX + subY * width);
                    }
                }

                if (!target.apply(inv.getStackInRowAndColumn(x, y))) {
                    return false;
                }
            }
        }

        return true;
    }

    public FixedShapedOreRecipe setMirrored(boolean mirror) {
        mirrored = mirror;
        return this;
    }

    public NonNullList<Ingredient> func_192400_c() {
        return this.input;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inv) //getRecipeLeftovers
    {
        return ForgeHooks.defaultRecipeGetRemainingItems(inv);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public String func_193358_e() {
        return this.group.toString();
    }

    public boolean func_194133_a(int p_194133_1_, int p_194133_2_) {
        return p_194133_1_ >= this.width && p_194133_2_ >= this.height;
    }
}
