"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { createTribute, listMemorialMembers, listTributes } from "@/features/memorial/api";
import { queryKeys } from "@/lib/api/queryKeys";

export function useMemorialMembers() {
  return useQuery({
    queryKey: queryKeys.memorials,
    queryFn: listMemorialMembers
  });
}

export function useTributes(memberId?: string | null) {
  return useQuery({
    queryKey: memberId ? queryKeys.memorialTributes(memberId) : ["memorials", "none", "tributes"],
    queryFn: () => listTributes(memberId as string),
    enabled: Boolean(memberId)
  });
}

export function useCreateTribute(memberId: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (input: Parameters<typeof createTribute>[1]) => createTribute(memberId, input),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: queryKeys.memorialTributes(memberId) })
  });
}
