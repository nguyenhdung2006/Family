"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { queryKeys } from "@/lib/api/queryKeys";
import {
  createFamilyMember,
  createRelationship,
  listFamilyMembers,
  listRelationships,
  updateFamilyMember,
  updateRelationship,
  type FamilyMemberFilters
} from "@/features/family/api";

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

export function useCreateFamilyMember() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: createFamilyMember,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["family"] })
  });
}

export function useUpdateFamilyMember(memberId: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (input: Parameters<typeof updateFamilyMember>[1]) => updateFamilyMember(memberId, input),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["family"] })
  });
}

export function useCreateFamilyRelationship() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: createRelationship,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["family"] })
  });
}

export function useUpdateFamilyRelationship(relationshipId: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (input: Parameters<typeof updateRelationship>[1]) => updateRelationship(relationshipId, input),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["family"] })
  });
}
