package com.familyhub.digital_family_hub.kitchen;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public final class RecipeDTO {

    private RecipeDTO() {
    }

    public record Request(
        @NotBlank @Size(max = 255) String title,
        @Size(max = 3000) String description,
        @NotBlank @Size(max = 10000) String ingredients,
        @NotBlank @Size(max = 10000) String instructions,
        @Size(max = 2000) String videoUrl,
        @Size(max = 5000) String notesFromElders
    ) {
    }

    public record Response(
        UUID id,
        UUID createdById,
        String createdByName,
        String title,
        String description,
        String ingredients,
        String instructions,
        String videoUrl,
        String notesFromElders
    ) {
        public static Response from(Recipe recipe) {
            return new Response(
                recipe.getId(),
                recipe.getCreatedBy() != null ? recipe.getCreatedBy().getId() : null,
                recipe.getCreatedBy() != null ? recipe.getCreatedBy().getName() : null,
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
