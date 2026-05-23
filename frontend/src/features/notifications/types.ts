export type NotificationType = "BIRTHDAY" | "DEATH_ANNIVERSARY" | "FAMILY_EVENT" | "NEW_MEMORY" | "MESSAGE";

export type InAppNotification = {
  id: string;
  type: NotificationType;
  title: string;
  body: string;
  scheduledFor: string | null;
  readAt: string | null;
};

export type CreateNotificationInput = {
  type: NotificationType;
  title: string;
  body: string;
  scheduledFor?: string | null;
};
