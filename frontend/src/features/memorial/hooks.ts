"use client";

import { useQuery } from "@tanstack/react-query";
import { listMemorialMembers, listTributes } from "@/features/memorial/api";
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
