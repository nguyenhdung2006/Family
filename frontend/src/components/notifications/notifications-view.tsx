"use client";

import { FormEvent, useState } from "react";
import { Bell, Check, Plus } from "lucide-react";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Input, Textarea } from "@/components/ui/input";
import { useCurrentUser } from "@/features/auth/hooks";
import { useCreateNotification, useMarkNotificationRead, useNotifications } from "@/features/notifications/hooks";
import type { NotificationType } from "@/features/notifications/types";
import { formatFamilyDate } from "@/lib/utils/date";

export function NotificationsView() {
  const { data: user } = useCurrentUser();
  const { data: notifications = [], isLoading, error } = useNotifications();
  const markRead = useMarkNotificationRead();
  const createNotification = useCreateNotification();
  const [form, setForm] = useState({
    type: "FAMILY_EVENT" as NotificationType,
    title: "",
    body: "",
    scheduledFor: ""
  });
  const isViewer = user?.role === "VIEWER";

  async function submitNotification(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!form.title.trim() || !form.body.trim()) {
      return;
    }
    await createNotification.mutateAsync({
      type: form.type,
      title: form.title.trim(),
      body: form.body.trim(),
      scheduledFor: form.scheduledFor ? new Date(form.scheduledFor).toISOString() : null
    });
    setForm({ type: "FAMILY_EVENT", title: "", body: "", scheduledFor: "" });
  }

  return (
    <div className="grid gap-4">
      {!isViewer ? (
      <Card>
        <CardContent>
          <form className="grid gap-3" onSubmit={submitNotification}>
            <div className="flex items-center justify-between gap-3">
              <h2 className="text-xl font-black text-ink">Create notification</h2>
              <Badge tone="yellow">Alert</Badge>
            </div>
            <div className="grid gap-3 md:grid-cols-[12rem_1fr_15rem]">
              <select className="min-h-12 rounded-lg border border-border-warm bg-white px-3 text-base font-bold text-ink" value={form.type} onChange={(event) => setForm((current) => ({ ...current, type: event.target.value as NotificationType }))}>
                {["BIRTHDAY", "DEATH_ANNIVERSARY", "FAMILY_EVENT", "NEW_MEMORY", "MESSAGE"].map((type) => <option key={type} value={type}>{type.replace("_", " ")}</option>)}
              </select>
              <Input value={form.title} onChange={(event) => setForm((current) => ({ ...current, title: event.target.value }))} placeholder="Title" required />
              <Input type="datetime-local" value={form.scheduledFor} onChange={(event) => setForm((current) => ({ ...current, scheduledFor: event.target.value }))} aria-label="Schedule time" />
            </div>
            <Textarea value={form.body} onChange={(event) => setForm((current) => ({ ...current, body: event.target.value }))} placeholder="Message" required />
            {createNotification.error ? <p className="font-bold text-[#C15A4A]">{createNotification.error.message}</p> : null}
            <Button type="submit" disabled={createNotification.isPending}>
              <Plus className="h-5 w-5" /> {createNotification.isPending ? "Creating..." : "Create notification"}
            </Button>
          </form>
        </CardContent>
      </Card>
      ) : null}

      {isLoading ? <Card><CardContent><p className="text-lg font-bold text-muted">Loading notifications...</p></CardContent></Card> : null}
      {error ? <Card><CardContent><p className="font-bold text-[#C15A4A]">{error.message}</p></CardContent></Card> : null}

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
            {!notification.readAt && !isViewer ? (
              <Button variant="secondary" onClick={() => markRead.mutate(notification.id)}>
                <Check className="h-5 w-5" /> Mark read
              </Button>
            ) : null}
          </CardContent>
        </Card>
      ))}
      {!notifications.length ? (
        <Card><CardContent><p className="text-lg font-bold text-muted">No notifications yet.</p></CardContent></Card>
      ) : null}
    </div>
  );
}
