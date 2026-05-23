"use client";

import { Bell, Check } from "lucide-react";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { useMarkNotificationRead, useNotifications } from "@/features/notifications/hooks";
import { formatFamilyDate } from "@/lib/utils/date";

export function NotificationsView() {
  const { data: notifications = [] } = useNotifications();
  const markRead = useMarkNotificationRead();

  return (
    <div className="grid gap-4">
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
            {!notification.readAt ? (
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
