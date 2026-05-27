"use client";

import { FormEvent, useState } from "react";
import { Heart, MapPin, MessageCircle, Pencil, Trash2, UsersRound, X } from "lucide-react";
import { Avatar } from "@/components/ui/avatar";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Input, Textarea } from "@/components/ui/input";
import { useCurrentUser } from "@/features/auth/hooks";
import { useDeleteTimelinePost, useUpdateTimelinePost } from "@/features/timeline/hooks";
import type { EventType, MemoryPost } from "@/features/timeline/types";
import { formatFamilyDate } from "@/lib/utils/date";

const events: EventType[] = ["FAMILY_GATHERING", "BIRTHDAY", "WEDDING", "TET", "TRAVEL", "MEMORIAL", "EVERYDAY", "OTHER"];

export function MemoryCard({ post }: { post: MemoryPost }) {
  const { data: user } = useCurrentUser();
  const updatePost = useUpdateTimelinePost(post.id);
  const deletePost = useDeleteTimelinePost();
  const [isEditing, setIsEditing] = useState(false);
  const [form, setForm] = useState(() => postForm(post));
  const canManagePost = user?.role === "ADMIN" || (user?.role === "MEMBER" && Boolean(user.id) && post.authorId === user.id);
  const isMutating = updatePost.isPending || deletePost.isPending;

  function startEdit() {
    setForm(postForm(post));
    setIsEditing(true);
  }

  async function submitEdit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    await updatePost.mutateAsync({
      text: form.text.trim(),
      occurredAt: new Date(form.occurredAt).toISOString(),
      locationName: form.locationName.trim() || undefined,
      eventType: form.eventType,
      taggedMemberIds: post.taggedMemberIds
    });
    setIsEditing(false);
  }

  async function handleDelete() {
    if (!window.confirm("Delete this memory?")) {
      return;
    }
    await deletePost.mutateAsync(post.id);
  }

  return (
    <Card>
      <CardContent>
        <div className="flex gap-3">
          <Avatar name="Family memory" className="h-12 w-12" />
          <div className="min-w-0 flex-1">
            <div className="flex flex-wrap items-center gap-2">
              <p className="text-lg font-black text-ink">Family Memory</p>
              <Badge tone="yellow">{post.eventType.replace("_", " ")}</Badge>
            </div>
            <p className="font-bold text-muted">{formatFamilyDate(post.occurredAt)}</p>
          </div>
          {canManagePost ? (
            <div className="flex gap-2">
              {isEditing ? (
                <Button type="button" variant="ghost" size="icon" aria-label="Cancel memory edit" onClick={() => setIsEditing(false)}>
                  <X className="h-4 w-4" />
                </Button>
              ) : (
                <Button type="button" variant="secondary" size="icon" aria-label="Edit memory" onClick={startEdit}>
                  <Pencil className="h-4 w-4" />
                </Button>
              )}
              <Button type="button" variant="danger" size="icon" aria-label="Delete memory" disabled={isMutating} onClick={handleDelete}>
                <Trash2 className="h-4 w-4" />
              </Button>
            </div>
          ) : null}
        </div>

        {isEditing ? (
          <form className="mt-4 grid gap-3" onSubmit={submitEdit}>
            <Textarea
              value={form.text}
              onChange={(event) => setForm((current) => ({ ...current, text: event.target.value }))}
              required
            />
            <div className="grid gap-3 md:grid-cols-[1fr_1fr_12rem]">
              <Input
                type="datetime-local"
                value={form.occurredAt}
                onChange={(event) => setForm((current) => ({ ...current, occurredAt: event.target.value }))}
                required
              />
              <Input
                placeholder="Location"
                value={form.locationName}
                onChange={(event) => setForm((current) => ({ ...current, locationName: event.target.value }))}
              />
              <select
                className="min-h-12 rounded-lg border border-border-warm bg-white px-3 text-base font-bold text-ink"
                value={form.eventType}
                onChange={(event) => setForm((current) => ({ ...current, eventType: event.target.value as EventType }))}
              >
                {events.map((event) => (
                  <option key={event} value={event}>{event.replace("_", " ")}</option>
                ))}
              </select>
            </div>
            {updatePost.error ? <p className="font-bold text-[#C15A4A]">{updatePost.error.message}</p> : null}
            <div className="flex justify-end gap-2">
              <Button type="button" variant="ghost" onClick={() => setIsEditing(false)}>Cancel</Button>
              <Button type="submit" disabled={isMutating}>
                <Pencil className="h-5 w-5" /> {updatePost.isPending ? "Saving..." : "Save memory"}
              </Button>
            </div>
          </form>
        ) : (
          <p className="mt-4 whitespace-pre-wrap text-lg font-semibold leading-8 text-ink">{post.text}</p>
        )}
        {!isEditing && deletePost.error ? <p className="mt-3 font-bold text-[#C15A4A]">{deletePost.error.message}</p> : null}

        <div className="mt-4 flex flex-wrap gap-3 text-sm font-bold text-muted">
          {post.locationName ? <span className="inline-flex items-center gap-1"><MapPin className="h-4 w-4" />{post.locationName}</span> : null}
          {post.taggedMemberIds.length ? <span className="inline-flex items-center gap-1"><UsersRound className="h-4 w-4" />{post.taggedMemberIds.length} tagged</span> : null}
        </div>

        <div className="mt-5 flex gap-2 border-t border-border-warm pt-4">
          <Button variant="ghost" className="flex-1">
            <Heart className="h-5 w-5" /> Love
          </Button>
          <Button variant="ghost" className="flex-1">
            <MessageCircle className="h-5 w-5" /> Comment
          </Button>
        </div>
      </CardContent>
    </Card>
  );
}

function postForm(post: MemoryPost) {
  return {
    text: post.text,
    occurredAt: toDateTimeLocalValue(post.occurredAt),
    locationName: post.locationName ?? "",
    eventType: post.eventType
  };
}

function toDateTimeLocalValue(value: string) {
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return "";
  }
  return date.toISOString().slice(0, 16);
}
