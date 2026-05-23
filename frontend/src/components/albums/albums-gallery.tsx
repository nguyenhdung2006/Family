"use client";

import { FormEvent, useMemo, useRef, useState } from "react";
import { AnimatePresence, motion } from "framer-motion";
import { ImagePlus, Play, Plus, X } from "lucide-react";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Input, Textarea } from "@/components/ui/input";
import { useAlbumMedia, useAlbums, useAttachAlbumMedia, useCreateAlbum, useUploadMedia } from "@/features/albums/hooks";
import type { Album, AlbumCategory } from "@/features/albums/types";

export function AlbumsGallery() {
  const { data: albums = [], isLoading, error } = useAlbums();
  const [selectedAlbum, setSelectedAlbum] = useState<Album | null>(null);
  const [selectedMediaIndex, setSelectedMediaIndex] = useState<number | null>(null);
  const [albumForm, setAlbumForm] = useState({ title: "", description: "", category: "EVERYDAY" as AlbumCategory });
  const [caption, setCaption] = useState("");
  const fileInputRef = useRef<HTMLInputElement>(null);
  const { data: media = [] } = useAlbumMedia(selectedAlbum?.id);
  const selectedMedia = selectedMediaIndex !== null ? media[selectedMediaIndex] : null;

  const featuredAlbum = useMemo(() => selectedAlbum ?? albums[0], [albums, selectedAlbum]);
  const createAlbum = useCreateAlbum();
  const uploadMedia = useUploadMedia();
  const attachMedia = useAttachAlbumMedia(featuredAlbum?.id ?? "");

  async function submitAlbum(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!albumForm.title.trim()) {
      return;
    }
    const created = await createAlbum.mutateAsync({
      title: albumForm.title.trim(),
      description: albumForm.description.trim() || null,
      category: albumForm.category
    });
    setSelectedAlbum(created);
    setAlbumForm({ title: "", description: "", category: "EVERYDAY" });
  }

  async function handleFileChange(file?: File) {
    if (!file || !featuredAlbum) {
      return;
    }
    const uploaded = await uploadMedia.mutateAsync(file);
    await attachMedia.mutateAsync({
      url: uploaded.url,
      storagePublicId: uploaded.storagePublicId,
      mediaType: uploaded.mediaType,
      caption: caption.trim() || file.name,
      capturedAt: new Date().toISOString()
    });
    setCaption("");
    if (fileInputRef.current) {
      fileInputRef.current.value = "";
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
          <Button disabled={!featuredAlbum || uploadMedia.isPending || attachMedia.isPending} onClick={() => fileInputRef.current?.click()}>
            <ImagePlus className="h-5 w-5" /> {uploadMedia.isPending || attachMedia.isPending ? "Uploading..." : "Upload"}
          </Button>
          <input ref={fileInputRef} type="file" accept="image/*,video/*" className="hidden" onChange={(event) => void handleFileChange(event.target.files?.[0])} />
        </CardContent>
      </Card>

      <Card>
        <CardContent>
          <form className="grid gap-3" onSubmit={submitAlbum}>
            <div className="flex items-center justify-between gap-3">
              <h2 className="text-xl font-black text-ink">Create album</h2>
              <Badge tone="sage">Archive</Badge>
            </div>
            <div className="grid gap-3 md:grid-cols-[1fr_13rem]">
              <Input value={albumForm.title} onChange={(event) => setAlbumForm((form) => ({ ...form, title: event.target.value }))} placeholder="Album title" required />
              <select className="min-h-12 rounded-lg border border-border-warm bg-white px-3 text-base font-bold text-ink" value={albumForm.category} onChange={(event) => setAlbumForm((form) => ({ ...form, category: event.target.value as AlbumCategory }))}>
                {["TET", "WEDDING", "TRAVEL", "BIRTHDAY", "MEMORIAL", "EVERYDAY", "OTHER"].map((category) => <option key={category} value={category}>{category.replace("_", " ")}</option>)}
              </select>
            </div>
            <Textarea value={albumForm.description} onChange={(event) => setAlbumForm((form) => ({ ...form, description: event.target.value }))} placeholder="Description" />
            {createAlbum.error ? <p className="font-bold text-[#C15A4A]">{createAlbum.error.message}</p> : null}
            <Button type="submit" disabled={createAlbum.isPending}>
              <Plus className="h-5 w-5" /> {createAlbum.isPending ? "Creating..." : "Create album"}
            </Button>
          </form>
        </CardContent>
      </Card>

      {featuredAlbum ? (
        <Card>
          <CardContent className="grid gap-3 md:grid-cols-[1fr_auto] md:items-end">
            <div>
              <h2 className="text-xl font-black text-ink">Add media to {featuredAlbum.title}</h2>
              <Input className="mt-3" value={caption} onChange={(event) => setCaption(event.target.value)} placeholder="Caption for next upload" />
            </div>
            <Button variant="secondary" disabled={uploadMedia.isPending || attachMedia.isPending} onClick={() => fileInputRef.current?.click()}>
              <ImagePlus className="h-5 w-5" /> Choose file
            </Button>
            {uploadMedia.error || attachMedia.error ? <p className="font-bold text-[#C15A4A] md:col-span-2">{uploadMedia.error?.message ?? attachMedia.error?.message}</p> : null}
          </CardContent>
        </Card>
      ) : null}

      {error ? <Card><CardContent><p className="font-bold text-[#C15A4A]">{error.message}</p></CardContent></Card> : null}

      {isLoading ? <Card><CardContent><p className="font-bold text-muted">Loading albums...</p></CardContent></Card> : null}

      <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-3">
        {albums.map((album) => (
          <button
            key={album.id}
            className="text-left"
            onClick={() => setSelectedAlbum(album)}
          >
            <Card className="h-full overflow-hidden transition hover:-translate-y-1">
              <div className="grid aspect-[4/3] place-items-center bg-[linear-gradient(135deg,#F7EEDC,#FFFFFF)]">
                <Play className="h-12 w-12 text-wood" />
              </div>
              <CardContent>
                <Badge tone="sage">{album.category}</Badge>
                <h3 className="mt-3 text-2xl font-black text-ink">{album.title}</h3>
                <p className="mt-2 line-clamp-2 font-semibold leading-7 text-muted">{album.description ?? "Family photos and memories."}</p>
              </CardContent>
            </Card>
          </button>
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
                <button key={item.id} className="group overflow-hidden rounded-lg bg-surface-soft text-left" onClick={() => setSelectedMediaIndex(index)}>
                  <div className="aspect-square overflow-hidden">
                    {item.mediaType === "IMAGE" ? (
                      // eslint-disable-next-line @next/next/no-img-element
                      <img src={item.url} alt={item.caption ?? "Album media"} className="h-full w-full object-cover transition group-hover:scale-105" />
                    ) : (
                      <div className="grid h-full w-full place-items-center bg-wood/10"><Play className="h-10 w-10 text-wood" /></div>
                    )}
                  </div>
                  <p className="p-3 font-bold text-muted">{item.caption ?? "Family memory"}</p>
                </button>
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
