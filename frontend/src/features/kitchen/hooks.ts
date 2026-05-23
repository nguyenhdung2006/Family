"use client";

import { useQuery } from "@tanstack/react-query";
import { listRecipes } from "@/features/kitchen/api";
import { queryKeys } from "@/lib/api/queryKeys";

export function useRecipes() {
  return useQuery({
    queryKey: queryKeys.recipes,
    queryFn: listRecipes
  });
}
