package com.familyhub.digital_family_hub.kitchen;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/kitchen/recipes")
public class KitchenController {

    private final RecipeRepository recipes;

    public KitchenController(RecipeRepository recipes) {
        this.recipes = recipes;
    }

    @GetMapping
    public List<RecipeResponse> listRecipes() {
        return recipes.findAll().stream().map(RecipeResponse::from).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RecipeResponse createRecipe(@Valid @RequestBody CreateRecipeRequest request) {
        Recipe recipe = new Recipe();
        recipe.setTitle(request.title());
        recipe.setDescription(request.description());
        recipe.setIngredients(request.ingredients());
        recipe.setInstructions(request.instructions());
        recipe.setVideoUrl(request.videoUrl());
        recipe.setNotesFromElders(request.notesFromElders());
        return RecipeResponse.from(recipes.save(recipe));
    }

    public record CreateRecipeRequest(
        @NotBlank String title,
        String description,
        @NotBlank String ingredients,
        @NotBlank String instructions,
        String videoUrl,
        String notesFromElders
    ) {
    }

    public record RecipeResponse(
        UUID id,
        String title,
        String description,
        String ingredients,
        String instructions,
        String videoUrl,
        String notesFromElders
    ) {
        static RecipeResponse from(Recipe recipe) {
            return new RecipeResponse(
                recipe.getId(),
                recipe.getTitle(),
                recipe.getDescription(),
                recipe.getIngredients(),
                recipe.getInstructions(),
                recipe.getVideoUrl(),
                recipe.getNotesFromElders()
            );
        }
    }
}
