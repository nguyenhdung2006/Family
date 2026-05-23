package com.familyhub.digital_family_hub.kitchen;

import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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
        applyRecipeRequest(recipe, request);
        return RecipeDTO.Response.from(recipes.save(recipe));
    }

    @Transactional
    public RecipeDTO.Response updateRecipe(UUID recipeId, RecipeDTO.Request request) {
        Recipe recipe = recipes.findById(recipeId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recipe not found"));
        applyRecipeRequest(recipe, request);
        return RecipeDTO.Response.from(recipes.save(recipe));
    }

    private void applyRecipeRequest(Recipe recipe, RecipeDTO.Request request) {
        recipe.setTitle(request.title());
        recipe.setDescription(request.description());
        recipe.setIngredients(request.ingredients());
        recipe.setInstructions(request.instructions());
        recipe.setVideoUrl(request.videoUrl());
        recipe.setNotesFromElders(request.notesFromElders());
    }
}
