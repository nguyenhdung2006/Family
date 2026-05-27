"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { createRecipe, deleteRecipe, listRecipes, updateRecipe } from "@/features/kitchen/api";
import { queryKeys } from "@/lib/api/queryKeys";

export function useRecipes() {
  return useQuery({
    queryKey: queryKeys.recipes,
    queryFn: listRecipes
  });
}

export function useCreateRecipe() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: createRecipe,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: queryKeys.recipes })
  });
}

export function useUpdateRecipe(recipeId: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (input: Parameters<typeof updateRecipe>[1]) => updateRecipe(recipeId, input),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: queryKeys.recipes })
  });
}

export function useDeleteRecipe() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: deleteRecipe,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: queryKeys.recipes })
  });
}
