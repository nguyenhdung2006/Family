package com.familyhub.digital_family_hub.kitchen;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class KitchenService {

    private final RecipeRepository recipes;

    public KitchenService(RecipeRepository recipes) {
        this.recipes = recipes;
    }

    @Transactional(readOnly = true)
    public List<RecipeDTO.Response> listRecipes() {
        return recipes.findAll().stream().map(RecipeDTO.Response::from).toList();
    }

    @Transactional
    public RecipeDTO.Response createRecipe(RecipeDTO.Request request) {
        Recipe recipe = new Recipe();
        recipe.setTitle(request.title());
        recipe.setDescription(request.description());
        recipe.setIngredients(request.ingredients());
        recipe.setInstructions(request.instructions());
        recipe.setVideoUrl(request.videoUrl());
        recipe.setNotesFromElders(request.notesFromElders());
        return RecipeDTO.Response.from(recipes.save(recipe));
    }
}
