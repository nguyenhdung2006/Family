import { apiFetch } from "@/lib/api/client";
import type { PageParams } from "@/lib/api/types";
import type { Album, AlbumCategory, AlbumMedia } from "@/features/albums/types";

export function listAlbums(filters: PageParams & { category?: AlbumCategory } = { page: 0, size: 30 }) {
  return apiFetch<Album[]>("/api/albums", { params: filters });
}

export function listAlbumMedia(albumId: string) {
  return apiFetch<AlbumMedia[]>(`/api/albums/${albumId}/media`);
}

export function uploadMedia(file: File) {
  const form = new FormData();
  form.append("file", file);
  return apiFetch<{ url: string; storagePublicId: string; mediaType: "IMAGE" | "VIDEO"; contentType: string; sizeBytes: number }>(
    "/api/media/upload",
    { method: "POST", body: form }
  );
}
