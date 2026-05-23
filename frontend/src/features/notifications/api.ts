import { apiFetch } from "@/lib/api/client";
import type { CreateNotificationInput, InAppNotification } from "@/features/notifications/types";

export function listNotifications() {
  return apiFetch<InAppNotification[]>("/api/notifications");
}

export function createNotification(input: CreateNotificationInput) {
  return apiFetch<InAppNotification>("/api/notifications", {
    method: "POST",
    body: input
  });
}

export function markNotificationRead(id: string) {
  return apiFetch<InAppNotification>(`/api/notifications/${id}/read`, {
    method: "PATCH"
  });
}
