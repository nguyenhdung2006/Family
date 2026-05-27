import { apiFetch } from "@/lib/api/client";
import type { CreateNotificationInput, InAppNotification, UpdateNotificationInput } from "@/features/notifications/types";

export function listNotifications() {
  return apiFetch<InAppNotification[]>("/api/notifications");
}

export function createNotification(input: CreateNotificationInput) {
  return apiFetch<InAppNotification>("/api/notifications", {
    method: "POST",
    body: input
  });
}

export function updateNotification(id: string, input: UpdateNotificationInput) {
  return apiFetch<InAppNotification>(`/api/notifications/${id}`, {
    method: "PUT",
    body: input
  });
}

export function deleteNotification(id: string) {
  return apiFetch<void>(`/api/notifications/${id}`, {
    method: "DELETE"
  });
}

export function markNotificationRead(id: string) {
  return apiFetch<InAppNotification>(`/api/notifications/${id}/read`, {
    method: "PATCH"
  });
}
