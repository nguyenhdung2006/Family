"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { createTimelinePost, listTimelinePosts } from "@/features/timeline/api";
import type { TimelineFilters } from "@/features/timeline/types";
import { queryKeys } from "@/lib/api/queryKeys";

export function useTimelinePosts(filters: TimelineFilters = { page: 0, size: 20 }) {
  return useQuery({
    queryKey: queryKeys.timelinePosts(filters),
    queryFn: () => listTimelinePosts(filters)
  });
}

export function useCreateTimelinePost() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: createTimelinePost,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["timeline", "posts"] })
  });
}
