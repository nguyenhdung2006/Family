"use client";

import { FormEvent, useEffect, useState } from "react";
import { Heart, ImagePlus, MapPin, MessageCircle, Pencil, Trash2, UsersRound, X } from "lucide-react";
import { Avatar } from "@/components/ui/avatar";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Input, Textarea } from "@/components/ui/input";
import { useCurrentUser } from "@/features/auth/hooks";
import { uploadMedia } from "@/features/albums/api";
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
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [previewUrl, setPreviewUrl] = useState<string | null>(null);
  const [uploadProgress, setUploadProgress] = useState<number | null>(null);
  const [uploadError, setUploadError] = useState<string | null>(null);
  const canManagePost = user?.role === "ADMIN" || (user?.role === "MEMBER" && Boolean(user.id) && post.authorId === user.id);
  const isMutating = updatePost.isPending || deletePost.isPending || uploadProgress !== null;
  const displayMedia = post.media.find((media) => media.mediaType === "IMAGE");
  const editPreviewUrl = previewUrl ?? form.mediaUrl;

  useEffect(() => {
    return () => {
      if (previewUrl?.startsWith("blob:")) {
        URL.revokeObjectURL(previewUrl);
      }
    };
  }, [previewUrl]);

  function startEdit() {
    setForm(postForm(post));
    setSelectedFile(null);
    setPreviewUrl(null);
    setUploadError(null);
    setIsEditing(true);
  }

  function cancelEdit() {
    setForm(postForm(post));
    setSelectedFile(null);
    setPreviewUrl(null);
    setUploadError(null);
    setIsEditing(false);
  }

  function handleFileChange(file?: File) {
    setUploadError(null);
    setSelectedFile(file ?? null);
    setPreviewUrl(file ? URL.createObjectURL(file) : null);
    if (!file) {
      setForm((current) => ({ ...current, mediaUrl: null, mediaStoragePublicId: null, mediaType: null }));
    }
  }

  async function submitEdit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setUploadError(null);
    let media = {
      url: form.mediaUrl,
      storagePublicId: form.mediaStoragePublicId,
      mediaType: form.mediaType
    };
    if (selectedFile) {
      setUploadProgress(0);
      try {
        const uploadedMedia = await uploadMedia(selectedFile, setUploadProgress);
        if (uploadedMedia.mediaType !== "IMAGE") {
          setUploadError("Timeline memories support image attachments only.");
          return;
        }
        media = {
          url: uploadedMedia.url,
          storagePublicId: uploadedMedia.storagePublicId,
          mediaType: uploadedMedia.mediaType
        };
      } catch (error) {
        setUploadError(error instanceof Error ? error.message : "Image upload failed.");
        return;
      } finally {
        setUploadProgress(null);
      }
    }
    await updatePost.mutateAsync({
      text: form.text.trim(),
      occurredAt: new Date(form.occurredAt).toISOString(),
      locationName: form.locationName.trim() || undefined,
      eventType: form.eventType,
      taggedMemberIds: post.taggedMemberIds,
      mediaUrl: media.url,
      mediaStoragePublicId: media.storagePublicId,
      mediaType: media.mediaType
    });
    cancelEdit();
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
                <Button type="button" variant="ghost" size="icon" aria-label="Cancel memory edit" onClick={cancelEdit}>
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
            <div className="grid gap-3 rounded-lg border border-dashed border-border-warm bg-surface-soft/50 p-3">
              <div className="flex flex-wrap items-center justify-between gap-3">
                <div>
                  <p className="font-black text-ink">Memory photo</p>
                  <p className="text-sm font-semibold text-muted">Replace or remove the optional archive image.</p>
                </div>
                <label className="inline-flex min-h-11 cursor-pointer items-center justify-center gap-2 rounded-lg border border-border-warm bg-white px-4 text-base font-bold text-ink transition hover:bg-surface-soft">
                  <ImagePlus className="h-5 w-5" />
                  Choose image
                  <input type="file" accept="image/*" className="sr-only" onChange={(event) => handleFileChange(event.target.files?.[0])} />
                </label>
              </div>
              {editPreviewUrl ? (
                <div className="relative overflow-hidden rounded-lg border border-border-warm bg-white">
                  {/* eslint-disable-next-line @next/next/no-img-element */}
                  <img src={editPreviewUrl} alt="Memory attachment preview" className="max-h-72 w-full object-cover" />
                  <Button type="button" variant="secondary" size="icon" className="absolute right-2 top-2 bg-white" aria-label="Remove memory image" onClick={() => handleFileChange()}>
                    <X className="h-4 w-4" />
                  </Button>
                </div>
              ) : null}
              {uploadProgress !== null ? <p className="font-bold text-muted">Uploading image... {uploadProgress}%</p> : null}
            </div>
            {uploadError || updatePost.error ? <p className="font-bold text-[#C15A4A]">{uploadError ?? updatePost.error?.message}</p> : null}
            <div className="flex justify-end gap-2">
              <Button type="button" variant="ghost" onClick={cancelEdit}>Cancel</Button>
              <Button type="submit" disabled={isMutating}>
                <Pencil className="h-5 w-5" /> {updatePost.isPending ? "Saving..." : "Save memory"}
              </Button>
            </div>
          </form>
        ) : (
          <>
            <p className="mt-4 whitespace-pre-wrap text-lg font-semibold leading-8 text-ink">{post.text}</p>
            {displayMedia ? (
              <div className="mt-4 overflow-hidden rounded-lg border border-border-warm bg-surface-soft">
                {/* eslint-disable-next-line @next/next/no-img-element */}
                <img src={displayMedia.url} alt="Family memory attachment" className="max-h-[28rem] w-full object-cover" />
              </div>
            ) : null}
          </>
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
  const image = post.media.find((media) => media.mediaType === "IMAGE");
  return {
    text: post.text,
    occurredAt: toDateTimeLocalValue(post.occurredAt),
    locationName: post.locationName ?? "",
    eventType: post.eventType,
    mediaUrl: image?.url ?? null,
    mediaStoragePublicId: image?.storagePublicId ?? null,
    mediaType: image?.mediaType ?? null
  };
}

function toDateTimeLocalValue(value: string) {
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return "";
  }
  return date.toISOString().slice(0, 16);
}
