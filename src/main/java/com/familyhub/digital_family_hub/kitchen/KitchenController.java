package com.familyhub.digital_family_hub.kitchen;

import com.familyhub.digital_family_hub.shared.api.ApiResponse;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/kitchen/recipes")
public class KitchenController {

    private final KitchenService kitchenService;

    public KitchenController(KitchenService kitchenService) {
        this.kitchenService = kitchenService;
    }

    @GetMapping
    public ApiResponse<List<RecipeDTO.Response>> listRecipes(@RequestParam(required = false) String search) {
        return ApiResponse.ok(kitchenService.listRecipes(search));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<RecipeDTO.Response> createRecipe(@Valid @RequestBody RecipeDTO.Request request, Principal principal) {
        return ApiResponse.created(kitchenService.createRecipe(request, principal));
    }

    @PutMapping("/{recipeId}")
    public ApiResponse<RecipeDTO.Response> updateRecipe(
        @PathVariable UUID recipeId,
        @Valid @RequestBody RecipeDTO.Request request,
        Principal principal
    ) {
        return ApiResponse.ok(kitchenService.updateRecipe(recipeId, request, principal));
    }

    @DeleteMapping("/{recipeId}")
    public ResponseEntity<Void> deleteRecipe(@PathVariable UUID recipeId, Principal principal) {
        kitchenService.deleteRecipe(recipeId, principal);
        return ResponseEntity.noContent().build();
    }
}
