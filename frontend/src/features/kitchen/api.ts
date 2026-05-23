import { apiFetch } from "@/lib/api/client";
import type { CreateRecipeInput, Recipe, UpdateRecipeInput } from "@/features/kitchen/types";

export function listRecipes() {
  return apiFetch<Recipe[]>("/api/kitchen/recipes");
}

export function createRecipe(input: CreateRecipeInput) {
  return apiFetch<Recipe>("/api/kitchen/recipes", {
    method: "POST",
    body: input
  });
}

export function updateRecipe(recipeId: string, input: UpdateRecipeInput) {
  return apiFetch<Recipe>(`/api/kitchen/recipes/${recipeId}`, {
    method: "PUT",
    body: input
  });
}
