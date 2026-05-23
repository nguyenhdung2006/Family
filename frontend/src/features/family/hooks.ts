"use client";

import { useQuery } from "@tanstack/react-query";
import { queryKeys } from "@/lib/api/queryKeys";
import { listFamilyMembers, listRelationships, type FamilyMemberFilters } from "@/features/family/api";

export function useFamilyMembers(filters: FamilyMemberFilters = { page: 0, size: 200 }) {
  return useQuery({
    queryKey: queryKeys.familyMembers(filters),
    queryFn: () => listFamilyMembers(filters),
    staleTime: 5 * 60_000
  });
}

export function useFamilyRelationships(memberId?: string | null) {
  return useQuery({
    queryKey: memberId ? queryKeys.familyRelationships(memberId) : ["family", "relationships", "none"],
    queryFn: () => listRelationships(memberId as string),
    enabled: Boolean(memberId),
    staleTime: 5 * 60_000
  });
}
