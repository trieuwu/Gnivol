package com.gnivol.game.system.inventory.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.gnivol.game.system.inventory.data.RecipeData;

import java.util.Arrays;
import java.util.HashMap;

public class CraftingManager {
    private HashMap<String, String> recipes;

    public CraftingManager() {
        recipes = new HashMap<>();
        loadRecipesFromJson();
    }

    public String getMergeResult(String itemA, String itemB) {
        if (itemA == null || itemB == null) {
            return null;
        }
        String recipeKey = makeRecipeKey(itemA, itemB);
        return recipes.get(recipeKey);
    }

    private String makeRecipeKey(String id1, String id2) {
        String[] arr = {id1, id2};
        Arrays.sort(arr);
        return arr[0] + "_" + arr[1];
    }

    private void loadRecipesFromJson() {
        Json json = new Json();
        Array<RecipeData> recipeList = json.fromJson(Array.class, RecipeData.class, Gdx.files.internal("data/recipes.json"));

        for (RecipeData recipeData : recipeList) {
            String key = makeRecipeKey(recipeData.itemA, recipeData.itemB);
            recipes.put(key, recipeData.result);
        }

    }

}
