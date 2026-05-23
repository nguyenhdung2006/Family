"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { createNotification, listNotifications, markNotificationRead } from "@/features/notifications/api";
import { queryKeys } from "@/lib/api/queryKeys";

export function useNotifications() {
  return useQuery({
    queryKey: queryKeys.notifications,
    queryFn: listNotifications,
    refetchInterval: 60_000
  });
}

export function useMarkNotificationRead() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: markNotificationRead,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: queryKeys.notifications })
  });
}

export function useCreateNotification() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: createNotification,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: queryKeys.notifications })
  });
}
