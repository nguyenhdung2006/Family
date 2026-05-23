import { apiFetch } from "@/lib/api/client";
import type { InAppNotification } from "@/features/notifications/types";

export function listNotifications() {
  return apiFetch<InAppNotification[]>("/api/notifications");
}

export function markNotificationRead(id: string) {
  return apiFetch<InAppNotification>(`/api/notifications/${id}/read`, {
    method: "PATCH"
  });
}
