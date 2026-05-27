"use client";

import { FormEvent, useEffect, useMemo, useRef, useState } from "react";
import { AnimatePresence, motion } from "framer-motion";
import { ImagePlus, Pencil, Play, Plus, Trash2, X } from "lucide-react";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { FormError, FormHint } from "@/components/forms/form-status";
import { Input, Textarea } from "@/components/ui/input";
import { useCurrentUser } from "@/features/auth/hooks";
import { useAlbumMedia, useAlbums, useAttachAlbumMedia, useCreateAlbum, useDeleteAlbum, useRemoveAlbumMedia, useUpdateAlbum, useUploadMedia } from "@/features/albums/hooks";
import type { CurrentUser } from "@/features/auth/types";
import type { Album, AlbumCategory } from "@/features/albums/types";

type UploadState = {
  fileName: string;
  previewUrl: string | null;
  progress: number;
  status: "uploading" | "attaching" | "complete" | "failed";
  metadata?: {
    storagePublicId: string;
    contentType: string;
    sizeBytes: number;
  };
};

export function AlbumsGallery() {
  const { data: user } = useCurrentUser();
  const { data: albums = [], isLoading, error } = useAlbums();
  const [selectedAlbum, setSelectedAlbum] = useState<Album | null>(null);
  const [selectedMediaIndex, setSelectedMediaIndex] = useState<number | null>(null);
  const [albumForm, setAlbumForm] = useState({ title: "", description: "", category: "EVERYDAY" as AlbumCategory });
  const [editingAlbumId, setEditingAlbumId] = useState<string | null>(null);
  const [albumValidation, setAlbumValidation] = useState<string | null>(null);
  const [caption, setCaption] = useState("");
  const [uploadState, setUploadState] = useState<UploadState | null>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);
  const previewUrlRef = useRef<string | null>(null);
  const { data: media = [] } = useAlbumMedia(selectedAlbum?.id);
  const selectedMedia = selectedMediaIndex !== null ? media[selectedMediaIndex] : null;

  const featuredAlbum = useMemo(() => selectedAlbum ?? albums[0], [albums, selectedAlbum]);
  const editingAlbum = albums.find((album) => album.id === editingAlbumId);
  const createAlbum = useCreateAlbum();
  const updateAlbum = useUpdateAlbum(editingAlbumId ?? "");
  const deleteAlbum = useDeleteAlbum();
  const uploadMedia = useUploadMedia();
  const attachMedia = useAttachAlbumMedia(featuredAlbum?.id ?? "");
  const removeMedia = useRemoveAlbumMedia(featuredAlbum?.id ?? "");
  const isViewer = user?.role === "VIEWER";
  const canManageFeaturedAlbum = featuredAlbum ? canManageAlbum(featuredAlbum, user) : false;

  useEffect(() => {
    return () => {
      if (previewUrlRef.current) {
        URL.revokeObjectURL(previewUrlRef.current);
      }
    };
  }, []);

  async function submitAlbum(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const payload = albumPayload(albumForm);
    if (!payload) {
      setAlbumValidation("Album title is required.");
      return;
    }
    setAlbumValidation(null);
    const saved = editingAlbumId ? await updateAlbum.mutateAsync(payload) : await createAlbum.mutateAsync(payload);
    setSelectedAlbum(saved);
    setEditingAlbumId(null);
    setAlbumForm({ title: "", description: "", category: "EVERYDAY" });
  }

  function startEditAlbum(album: Album) {
    setEditingAlbumId(album.id);
    setAlbumValidation(null);
    setAlbumForm({
      title: album.title,
      description: album.description ?? "",
      category: album.category
    });
  }

  function cancelEditAlbum() {
    setEditingAlbumId(null);
    setAlbumValidation(null);
    setAlbumForm({ title: "", description: "", category: "EVERYDAY" });
  }

  async function handleDeleteAlbum(album: Album) {
    if (!window.confirm(`Delete ${album.title}?`)) {
      return;
    }
    await deleteAlbum.mutateAsync(album.id);
    if (selectedAlbum?.id === album.id) {
      setSelectedAlbum(null);
    }
    if (editingAlbumId === album.id) {
      cancelEditAlbum();
    }
  }

  async function handleRemoveMedia(mediaId: string) {
    if (!featuredAlbum || !window.confirm("Remove this media from the album?")) {
      return;
    }
    await removeMedia.mutateAsync(mediaId);
    if (selectedMedia?.id === mediaId) {
      setSelectedMediaIndex(null);
    }
  }

  async function handleFileChange(file?: File) {
    if (!file || !featuredAlbum) {
      return;
    }
    const previewUrl = file.type.startsWith("image/") ? URL.createObjectURL(file) : null;
    if (previewUrlRef.current) {
      URL.revokeObjectURL(previewUrlRef.current);
    }
    previewUrlRef.current = previewUrl;
    setUploadState({ fileName: file.name, previewUrl, progress: 0, status: "uploading" });
    try {
      const uploaded = await uploadMedia.mutateAsync({
        file,
        onProgress: (progress) => {
          setUploadState((current) => current ? { ...current, progress, status: "uploading" } : current);
        }
      });
      setUploadState((current) => current ? {
        ...current,
        progress: 100,
        status: "attaching",
        metadata: {
          storagePublicId: uploaded.storagePublicId,
          contentType: uploaded.contentType,
          sizeBytes: uploaded.sizeBytes
        }
      } : current);
      await attachMedia.mutateAsync({
        url: uploaded.url,
        storagePublicId: uploaded.storagePublicId,
        mediaType: uploaded.mediaType,
        caption: caption.trim() || file.name,
        capturedAt: new Date().toISOString()
      });
      setUploadState((current) => current ? { ...current, status: "complete" } : current);
      setCaption("");
    } catch {
      setUploadState((current) => current ? { ...current, status: "failed" } : current);
    } finally {
      if (fileInputRef.current) {
        fileInputRef.current.value = "";
      }
    }
  }

  return (
    <div className="grid gap-5">
      <Card>
        <CardContent className="flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
          <div>
            <Badge tone="yellow">Family archive</Badge>
            <h2 className="mt-3 text-3xl font-black text-ink">Albums and old photos</h2>
            <p className="mt-2 max-w-2xl text-lg font-semibold leading-8 text-muted">
              Keep holidays, weddings, everyday meals, and tiny ordinary moments in one gentle place.
            </p>
          </div>
          {canManageFeaturedAlbum ? (
          <Button disabled={!featuredAlbum || uploadMedia.isPending || attachMedia.isPending} onClick={() => fileInputRef.current?.click()}>
            <ImagePlus className="h-5 w-5" /> {uploadMedia.isPending || attachMedia.isPending ? "Uploading..." : "Upload"}
          </Button>
          ) : null}
          {!isViewer ? <input ref={fileInputRef} type="file" accept="image/*,video/*" className="hidden" onChange={(event) => void handleFileChange(event.target.files?.[0])} /> : null}
        </CardContent>
      </Card>

      {!isViewer ? (
      <Card>
        <CardContent>
          <form className="grid gap-3" onSubmit={submitAlbum}>
            <div className="flex items-center justify-between gap-3">
              <div>
                <h2 className="text-xl font-black text-ink">{editingAlbum ? "Edit album" : "Create album"}</h2>
                {editingAlbum ? <FormHint>Editing {editingAlbum.title}</FormHint> : null}
              </div>
              {editingAlbum ? (
                <Button type="button" variant="ghost" size="icon" aria-label="Cancel album edit" onClick={cancelEditAlbum}>
                  <X className="h-5 w-5" />
                </Button>
              ) : (
                <Badge tone="sage">Archive</Badge>
              )}
            </div>
            <div className="grid gap-3 md:grid-cols-[1fr_13rem]">
              <Input value={albumForm.title} onChange={(event) => setAlbumForm((form) => ({ ...form, title: event.target.value }))} placeholder="Album title" required />
              <select className="min-h-12 rounded-lg border border-border-warm bg-white px-3 text-base font-bold text-ink" value={albumForm.category} onChange={(event) => setAlbumForm((form) => ({ ...form, category: event.target.value as AlbumCategory }))}>
                {["TET", "WEDDING", "TRAVEL", "BIRTHDAY", "MEMORIAL", "EVERYDAY", "OTHER"].map((category) => <option key={category} value={category}>{category.replace("_", " ")}</option>)}
              </select>
            </div>
            <Textarea value={albumForm.description} onChange={(event) => setAlbumForm((form) => ({ ...form, description: event.target.value }))} placeholder="Description" />
            <FormError message={albumValidation ?? createAlbum.error?.message ?? updateAlbum.error?.message} />
            <Button type="submit" disabled={createAlbum.isPending || updateAlbum.isPending}>
              {editingAlbum ? <Pencil className="h-5 w-5" /> : <Plus className="h-5 w-5" />}
              {updateAlbum.isPending ? "Saving..." : createAlbum.isPending ? "Creating..." : editingAlbum ? "Save album" : "Create album"}
            </Button>
          </form>
        </CardContent>
      </Card>
      ) : null}

      {featuredAlbum && canManageFeaturedAlbum ? (
        <Card>
          <CardContent className="grid gap-3 md:grid-cols-[1fr_auto] md:items-end">
            <div>
              <h2 className="text-xl font-black text-ink">Add media to {featuredAlbum.title}</h2>
              <Input className="mt-3" value={caption} onChange={(event) => setCaption(event.target.value)} placeholder="Caption for next upload" />
            </div>
            <Button variant="secondary" disabled={uploadMedia.isPending || attachMedia.isPending} onClick={() => fileInputRef.current?.click()}>
              <ImagePlus className="h-5 w-5" /> Choose file
            </Button>
            {uploadState ? (
              <div className="grid gap-3 rounded-lg border border-border-warm bg-white p-3 md:col-span-2 md:grid-cols-[8rem_minmax(0,1fr)]">
                <div className="grid aspect-square place-items-center overflow-hidden rounded-lg bg-surface-soft">
                  {uploadState.previewUrl ? (
                    // eslint-disable-next-line @next/next/no-img-element
                    <img src={uploadState.previewUrl} alt={uploadState.fileName} className="h-full w-full object-cover" />
                  ) : (
                    <Play className="h-8 w-8 text-wood" />
                  )}
                </div>
                <div className="grid content-center gap-2">
                  <div className="flex flex-wrap items-center justify-between gap-2">
                    <p className="font-black text-ink">{uploadState.fileName}</p>
                    <Badge tone={uploadState.status === "failed" ? "yellow" : "sage"}>
                      {uploadState.status === "attaching" ? "Attaching" : uploadState.status === "complete" ? "Attached" : uploadState.status === "failed" ? "Failed" : "Uploading"}
                    </Badge>
                  </div>
                  <div className="h-3 overflow-hidden rounded-full bg-surface-soft">
                    <div className="h-full rounded-full bg-wood transition-all" style={{ width: `${uploadState.progress}%` }} />
                  </div>
                  <p className="text-sm font-bold text-muted">
                    {uploadState.metadata
                      ? `${uploadState.metadata.contentType} · ${formatBytes(uploadState.metadata.sizeBytes)}`
                      : `${uploadState.progress}% uploaded`}
                  </p>
                </div>
              </div>
            ) : null}
            {uploadMedia.error || attachMedia.error || removeMedia.error ? (
              <p className="font-bold text-[#C15A4A] md:col-span-2">{uploadMedia.error?.message ?? attachMedia.error?.message ?? removeMedia.error?.message}</p>
            ) : null}
          </CardContent>
        </Card>
      ) : null}

      {error ? <Card><CardContent><p className="font-bold text-[#C15A4A]">{error.message}</p></CardContent></Card> : null}
      {deleteAlbum.error ? <Card><CardContent><p className="font-bold text-[#C15A4A]">{deleteAlbum.error.message}</p></CardContent></Card> : null}

      {isLoading ? <Card><CardContent><p className="font-bold text-muted">Loading albums...</p></CardContent></Card> : null}

      <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-3">
        {albums.map((album) => (
          <div
            key={album.id}
            className="text-left"
          >
            <Card className="h-full overflow-hidden transition hover:-translate-y-1">
              <button className="grid aspect-[4/3] w-full place-items-center bg-[linear-gradient(135deg,#F7EEDC,#FFFFFF)]" onClick={() => setSelectedAlbum(album)}>
                <Play className="h-12 w-12 text-wood" />
              </button>
              <CardContent>
                <div className="flex items-start justify-between gap-3">
                  <div>
                    <Badge tone="sage">{album.category}</Badge>
                    <h3 className="mt-3 text-2xl font-black text-ink">{album.title}</h3>
                  </div>
                  {canManageAlbum(album, user) ? (
                  <div className="flex gap-2">
                    <Button type="button" variant="secondary" size="icon" aria-label={`Edit ${album.title}`} onClick={() => startEditAlbum(album)}>
                      <Pencil className="h-4 w-4" />
                    </Button>
                    <Button type="button" variant="danger" size="icon" aria-label={`Delete ${album.title}`} disabled={deleteAlbum.isPending} onClick={() => void handleDeleteAlbum(album)}>
                      <Trash2 className="h-4 w-4" />
                    </Button>
                  </div>
                  ) : null}
                </div>
                <button className="mt-2 line-clamp-2 text-left font-semibold leading-7 text-muted" onClick={() => setSelectedAlbum(album)}>
                  {album.description ?? "Family photos and memories."}
                </button>
              </CardContent>
            </Card>
          </div>
        ))}
      </div>

      {!albums.length && !isLoading ? (
        <Card><CardContent><p className="text-lg font-bold text-muted">No albums yet. Create one from the backend/API to begin the archive.</p></CardContent></Card>
      ) : null}

      {featuredAlbum ? (
        <Card>
          <CardContent>
            <h3 className="text-2xl font-black text-ink">{featuredAlbum.title}</h3>
            <div className="mt-4 grid gap-3 sm:grid-cols-2 lg:grid-cols-4">
              {media.map((item, index) => (
                <div key={item.id} className="group overflow-hidden rounded-lg bg-surface-soft text-left">
                  <button className="w-full text-left" onClick={() => setSelectedMediaIndex(index)}>
                    <div className="aspect-square overflow-hidden">
                      {item.mediaType === "IMAGE" ? (
                        // eslint-disable-next-line @next/next/no-img-element
                        <img src={item.url} alt={item.caption ?? "Album media"} className="h-full w-full object-cover transition group-hover:scale-105" />
                      ) : (
                        <div className="grid h-full w-full place-items-center bg-wood/10"><Play className="h-10 w-10 text-wood" /></div>
                      )}
                    </div>
                  </button>
                  <div className="flex items-center justify-between gap-2 p-3">
                    <button className="min-w-0 flex-1 text-left font-bold text-muted" onClick={() => setSelectedMediaIndex(index)}>
                      {item.caption ?? "Family memory"}
                    </button>
                    {canManageFeaturedAlbum ? (
                      <Button type="button" variant="danger" size="icon" aria-label="Remove media" disabled={removeMedia.isPending} onClick={() => void handleRemoveMedia(item.id)}>
                        <Trash2 className="h-4 w-4" />
                      </Button>
                    ) : null}
                  </div>
                </div>
              ))}
              {!media.length ? <p className="font-semibold text-muted">Select an album with media to preview the gallery.</p> : null}
            </div>
          </CardContent>
        </Card>
      ) : null}

      <AnimatePresence>
        {selectedMedia ? (
          <motion.div className="fixed inset-0 z-50 grid place-items-center bg-ink/80 p-4" initial={{ opacity: 0 }} animate={{ opacity: 1 }} exit={{ opacity: 0 }}>
            <Button variant="secondary" size="icon" aria-label="Close slideshow" className="absolute right-4 top-4" onClick={() => setSelectedMediaIndex(null)}>
              <X className="h-5 w-5" />
            </Button>
            <div className="max-h-[86vh] max-w-5xl overflow-hidden rounded-lg bg-surface">
              {selectedMedia.mediaType === "IMAGE" ? (
                // eslint-disable-next-line @next/next/no-img-element
                <img src={selectedMedia.url} alt={selectedMedia.caption ?? "Album media"} className="max-h-[74vh] w-full object-contain" />
              ) : (
                <video src={selectedMedia.url} controls className="max-h-[74vh] w-full" />
              )}
              <div className="p-4 text-lg font-bold text-ink">{selectedMedia.caption ?? "Family memory"}</div>
            </div>
          </motion.div>
        ) : null}
      </AnimatePresence>
    </div>
  );
}

function formatBytes(bytes: number) {
  if (bytes < 1024) {
    return `${bytes} B`;
  }
  if (bytes < 1024 * 1024) {
    return `${Math.round(bytes / 1024)} KB`;
  }
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
}

function albumPayload(form: { title: string; description: string; category: AlbumCategory }) {
  if (!form.title.trim()) {
    return null;
  }
  return {
    title: form.title.trim(),
    description: form.description.trim() || null,
    category: form.category
  };
}

function canManageAlbum(album: Album, user?: CurrentUser) {
  return user?.role === "ADMIN" || (user?.role === "MEMBER" && Boolean(user.id) && album.createdById === user.id);
}
