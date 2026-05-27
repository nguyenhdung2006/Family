"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { createTimelinePost, deleteTimelinePost, listTimelinePosts, updateTimelinePost } from "@/features/timeline/api";
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

export function useUpdateTimelinePost(postId: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (input: Parameters<typeof updateTimelinePost>[1]) => updateTimelinePost(postId, input),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["timeline", "posts"] })
  });
}

export function useDeleteTimelinePost() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: deleteTimelinePost,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["timeline", "posts"] })
  });
}
