import { apiFetch } from "@/lib/api/client";
import { ApiError } from "@/lib/api/client";
import type { PageParams } from "@/lib/api/types";
import type { ApiEnvelope } from "@/lib/api/types";
import { getAccessToken } from "@/lib/auth/token";
import type { Album, AlbumCategory, AlbumMedia, AttachAlbumMediaInput, CreateAlbumInput, MediaUploadResponse, UpdateAlbumInput } from "@/features/albums/types";

const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080";

export function listAlbums(filters: PageParams & { category?: AlbumCategory } = { page: 0, size: 30 }) {
  return apiFetch<Album[]>("/api/albums", { params: filters });
}

export function listAlbumMedia(albumId: string) {
  return apiFetch<AlbumMedia[]>(`/api/albums/${albumId}/media`);
}

export function createAlbum(input: CreateAlbumInput) {
  return apiFetch<Album>("/api/albums", {
    method: "POST",
    body: input
  });
}

export function updateAlbum(albumId: string, input: UpdateAlbumInput) {
  return apiFetch<Album>(`/api/albums/${albumId}`, {
    method: "PUT",
    body: input
  });
}

export function deleteAlbum(albumId: string) {
  return apiFetch<void>(`/api/albums/${albumId}`, {
    method: "DELETE"
  });
}

export function attachAlbumMedia(albumId: string, input: AttachAlbumMediaInput) {
  return apiFetch<AlbumMedia>(`/api/albums/${albumId}/media`, {
    method: "POST",
    body: input
  });
}

export function removeAlbumMedia(albumId: string, mediaId: string) {
  return apiFetch<void>(`/api/albums/${albumId}/media/${mediaId}`, {
    method: "DELETE"
  });
}

export function uploadMedia(file: File, onProgress?: (progress: number) => void) {
  const form = new FormData();
  form.append("file", file);
  return uploadFormData<MediaUploadResponse>("/api/media/upload", form, onProgress);
}

function uploadFormData<T>(path: string, body: FormData, onProgress?: (progress: number) => void) {
  return new Promise<T>((resolve, reject) => {
    const request = new XMLHttpRequest();
    request.open("POST", buildUrl(path));
    request.withCredentials = true;
    request.setRequestHeader("Accept", "application/json");

    const token = getAccessToken();
    if (token) {
      request.setRequestHeader("Authorization", `Bearer ${token}`);
    }

    request.upload.onprogress = (event) => {
      if (event.lengthComputable && onProgress) {
        onProgress(Math.round((event.loaded / event.total) * 100));
      }
    };

    request.onload = () => {
      if (request.status < 200 || request.status >= 300) {
        reject(parseUploadError(request, path));
        return;
      }
      try {
        const envelope = JSON.parse(request.responseText) as ApiEnvelope<T>;
        onProgress?.(100);
        resolve(envelope.data);
      } catch {
        reject(new ApiError("Upload response could not be parsed", request.status, path));
      }
    };

    request.onerror = () => reject(new ApiError("Upload request failed", request.status || 0, path));
    request.onabort = () => reject(new ApiError("Upload was cancelled", request.status || 0, path));
    request.send(body);
  });
}

function parseUploadError(request: XMLHttpRequest, path: string) {
  try {
    const body = JSON.parse(request.responseText) as { message?: string; path?: string };
    return new ApiError(body.message ?? `Upload failed with ${request.status}`, request.status, body.path ?? path);
  } catch {
    return new ApiError(`Upload failed with ${request.status}`, request.status, path);
  }
}

function buildUrl(path: string) {
  const baseUrl = API_BASE_URL.trim();
  return new URL(
    path.startsWith("http") ? path : baseUrl ? `${baseUrl}${path}` : path,
    typeof window === "undefined" ? "http://localhost:3000" : window.location.origin
  ).toString();
}
