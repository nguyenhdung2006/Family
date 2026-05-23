import { apiFetch } from "@/lib/api/client";
import type { Recipe } from "@/features/kitchen/types";

export function listRecipes() {
  return apiFetch<Recipe[]>("/api/kitchen/recipes");
}
