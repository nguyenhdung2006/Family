"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { createRecipe, listRecipes } from "@/features/kitchen/api";
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
