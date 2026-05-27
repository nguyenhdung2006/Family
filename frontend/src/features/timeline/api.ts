import { apiFetch } from "@/lib/api/client";
import type { CreateMemoryPost, MemoryPost, TimelineFilters, UpdateMemoryPost } from "@/features/timeline/types";

export function listTimelinePosts(filters: TimelineFilters = { page: 0, size: 20 }) {
  return apiFetch<MemoryPost[]>("/api/timeline/posts", { params: filters });
}

export function createTimelinePost(input: CreateMemoryPost) {
  return apiFetch<MemoryPost>("/api/timeline/posts", {
    method: "POST",
    body: input
  });
}

export function updateTimelinePost(postId: string, input: UpdateMemoryPost) {
  return apiFetch<MemoryPost>(`/api/timeline/posts/${postId}`, {
    method: "PUT",
    body: input
  });
}

export function deleteTimelinePost(postId: string) {
  return apiFetch<void>(`/api/timeline/posts/${postId}`, {
    method: "DELETE"
  });
}
