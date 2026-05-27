package com.familyhub.digital_family_hub.kitchen;

import com.familyhub.digital_family_hub.users.AppUser;
import com.familyhub.digital_family_hub.users.AppUserRepository;
import java.security.Principal;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class KitchenService {

    private final RecipeRepository recipes;
    private final AppUserRepository users;

    public KitchenService(RecipeRepository recipes, AppUserRepository users) {
        this.recipes = recipes;
        this.users = users;
    }

    @Transactional(readOnly = true)
    public List<RecipeDTO.Response> listRecipes() {
        return recipes.findAll().stream().map(RecipeDTO.Response::from).toList();
    }

    @Transactional
    public RecipeDTO.Response createRecipe(RecipeDTO.Request request, Principal principal) {
        AppUser currentUser = resolveCurrentUser(principal);
        Recipe recipe = new Recipe();
        applyRecipeRequest(recipe, request);
        recipe.setCreatedBy(currentUser);
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

    private AppUser resolveCurrentUser(Principal principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }
        return users.findByEmailIgnoreCase(principal.getName())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user was not found"));
    }
}
