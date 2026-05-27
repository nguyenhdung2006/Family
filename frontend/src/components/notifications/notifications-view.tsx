"use client";

import { FormEvent, useState } from "react";
import { Bell, Check, Pencil, Plus, Trash2, X } from "lucide-react";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Input, Textarea } from "@/components/ui/input";
import { useCurrentUser } from "@/features/auth/hooks";
import type { CurrentUser } from "@/features/auth/types";
import { useCreateNotification, useDeleteNotification, useMarkNotificationRead, useNotifications, useUpdateNotification } from "@/features/notifications/hooks";
import type { InAppNotification, NotificationType } from "@/features/notifications/types";
import { formatFamilyDate } from "@/lib/utils/date";

export function NotificationsView() {
  const { data: user } = useCurrentUser();
  const { data: notifications = [], isLoading, error, refetch } = useNotifications();
  const markRead = useMarkNotificationRead();
  const createNotification = useCreateNotification();
  const deleteNotification = useDeleteNotification();
  const [editingNotificationId, setEditingNotificationId] = useState<string | null>(null);
  const editingNotification = notifications.find((notification) => notification.id === editingNotificationId);
  const updateNotification = useUpdateNotification(editingNotificationId ?? "");
  const [form, setForm] = useState({
    type: "FAMILY_EVENT" as NotificationType,
    title: "",
    body: "",
    scheduledFor: ""
  });
  const isViewer = user?.role === "VIEWER";
  const isSaving = createNotification.isPending || updateNotification.isPending;

  async function submitNotification(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!form.title.trim() || !form.body.trim()) {
      return;
    }
    const payload = {
      type: form.type,
      title: form.title.trim(),
      body: form.body.trim(),
      scheduledFor: form.scheduledFor ? new Date(form.scheduledFor).toISOString() : null
    };
    if (editingNotificationId) {
      await updateNotification.mutateAsync(payload);
      setEditingNotificationId(null);
    } else {
      await createNotification.mutateAsync(payload);
    }
    setForm({ type: "FAMILY_EVENT", title: "", body: "", scheduledFor: "" });
  }

  function startEditNotification(notification: InAppNotification) {
    if (!canManageNotification(notification, user)) {
      return;
    }
    setEditingNotificationId(notification.id);
    setForm({
      type: notification.type,
      title: notification.title,
      body: notification.body,
      scheduledFor: notification.scheduledFor ? toDateTimeLocalValue(notification.scheduledFor) : ""
    });
  }

  function cancelEditNotification() {
    setEditingNotificationId(null);
    setForm({ type: "FAMILY_EVENT", title: "", body: "", scheduledFor: "" });
  }

  async function handleDeleteNotification(notification: InAppNotification) {
    if (!canManageNotification(notification, user) || !window.confirm(`Delete ${notification.title}?`)) {
      return;
    }
    await deleteNotification.mutateAsync(notification.id);
    if (editingNotificationId === notification.id) {
      cancelEditNotification();
    }
  }

  return (
    <div className="grid gap-4">
      {!isViewer ? (
      <Card>
        <CardContent>
          <form className="grid gap-3" onSubmit={submitNotification}>
            <div className="flex items-center justify-between gap-3">
              <h2 className="text-xl font-black text-ink">{editingNotification ? "Edit notification" : "Create notification"}</h2>
              {editingNotification ? (
                <Button type="button" variant="ghost" size="icon" aria-label="Cancel notification edit" onClick={cancelEditNotification}>
                  <X className="h-5 w-5" />
                </Button>
              ) : (
                <Badge tone="yellow">Alert</Badge>
              )}
            </div>
            <div className="grid gap-3 md:grid-cols-[12rem_1fr_15rem]">
              <select className="min-h-12 rounded-lg border border-border-warm bg-white px-3 text-base font-bold text-ink" value={form.type} onChange={(event) => setForm((current) => ({ ...current, type: event.target.value as NotificationType }))}>
                {["BIRTHDAY", "DEATH_ANNIVERSARY", "FAMILY_EVENT", "NEW_MEMORY", "MESSAGE"].map((type) => <option key={type} value={type}>{type.replace("_", " ")}</option>)}
              </select>
              <Input value={form.title} onChange={(event) => setForm((current) => ({ ...current, title: event.target.value }))} placeholder="Title" required />
              <Input type="datetime-local" value={form.scheduledFor} onChange={(event) => setForm((current) => ({ ...current, scheduledFor: event.target.value }))} aria-label="Schedule time" />
            </div>
            <Textarea value={form.body} onChange={(event) => setForm((current) => ({ ...current, body: event.target.value }))} placeholder="Message" required />
            {createNotification.error || updateNotification.error ? <p className="font-bold text-[#C15A4A]">{createNotification.error?.message ?? updateNotification.error?.message}</p> : null}
            <Button type="submit" disabled={isSaving}>
              {editingNotification ? <Pencil className="h-5 w-5" /> : <Plus className="h-5 w-5" />}
              {isSaving ? "Saving..." : editingNotification ? "Save notification" : "Create notification"}
            </Button>
          </form>
        </CardContent>
      </Card>
      ) : null}

      {isLoading ? <Card><CardContent><p className="text-lg font-bold text-muted">Loading notifications...</p></CardContent></Card> : null}
      {error ? (
        <Card>
          <CardContent className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
            <p className="font-bold text-[#C15A4A]">We could not load notifications. {error.message}</p>
            <Button variant="secondary" onClick={() => void refetch()}>Try again</Button>
          </CardContent>
        </Card>
      ) : null}
      {deleteNotification.error ? <Card><CardContent><p className="font-bold text-[#C15A4A]">{deleteNotification.error.message}</p></CardContent></Card> : null}

      {notifications.map((notification) => (
        <Card key={notification.id} className={notification.readAt ? "opacity-75" : ""}>
          <CardContent className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
            <div className="flex gap-3">
              <span className="grid h-12 w-12 place-items-center rounded-lg bg-warm-yellow/25 text-wood">
                <Bell className="h-6 w-6" />
              </span>
              <div>
                <div className="flex flex-wrap items-center gap-2">
                  <h2 className="text-xl font-black text-ink">{notification.title}</h2>
                  <Badge tone={notification.readAt ? "cream" : "yellow"}>{notification.type.replace("_", " ")}</Badge>
                </div>
                <p className="mt-1 font-semibold leading-7 text-muted">{notification.body}</p>
                {notification.scheduledFor ? <p className="mt-1 text-sm font-bold text-muted">{formatFamilyDate(notification.scheduledFor)}</p> : null}
              </div>
            </div>
            <div className="flex flex-wrap gap-2">
              {canManageNotification(notification, user) ? (
                <>
                  <Button type="button" variant="secondary" size="icon" aria-label={`Edit ${notification.title}`} onClick={() => startEditNotification(notification)}>
                    <Pencil className="h-4 w-4" />
                  </Button>
                  <Button type="button" variant="danger" size="icon" aria-label={`Delete ${notification.title}`} disabled={deleteNotification.isPending} onClick={() => void handleDeleteNotification(notification)}>
                    <Trash2 className="h-4 w-4" />
                  </Button>
                </>
              ) : null}
              {!notification.readAt && !isViewer ? (
                <Button variant="secondary" onClick={() => markRead.mutate(notification.id)}>
                  <Check className="h-5 w-5" /> Mark read
                </Button>
              ) : null}
            </div>
          </CardContent>
        </Card>
      ))}
      {!notifications.length && !isLoading && !error ? (
        <Card>
          <CardContent>
            <p className="text-lg font-black text-ink">No notifications yet.</p>
            <p className="mt-2 font-semibold text-muted">
              {isViewer ? "Family alerts and reminders will appear here." : "Create the first family alert from the form above."}
            </p>
          </CardContent>
        </Card>
      ) : null}
    </div>
  );
}

function canManageNotification(notification: InAppNotification, user?: CurrentUser) {
  return user?.role === "ADMIN" || (user?.role === "MEMBER" && Boolean(user.id) && notification.createdById === user.id);
}

function toDateTimeLocalValue(value: string) {
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return "";
  }
  return date.toISOString().slice(0, 16);
}
