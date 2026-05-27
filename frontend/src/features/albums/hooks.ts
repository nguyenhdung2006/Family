"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { attachAlbumMedia, createAlbum, deleteAlbum, listAlbumMedia, listAlbums, removeAlbumMedia, updateAlbum, uploadMedia } from "@/features/albums/api";
import type { AlbumCategory } from "@/features/albums/types";
import { queryKeys } from "@/lib/api/queryKeys";

export function useAlbums(category?: AlbumCategory) {
  return useQuery({
    queryKey: queryKeys.albums({ category }),
    queryFn: () => listAlbums({ page: 0, size: 30, category })
  });
}

export function useAlbumMedia(albumId?: string | null) {
  return useQuery({
    queryKey: albumId ? queryKeys.albumMedia(albumId) : ["albums", "none", "media"],
    queryFn: () => listAlbumMedia(albumId as string),
    enabled: Boolean(albumId)
  });
}

export function useCreateAlbum() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: createAlbum,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["albums"] })
  });
}

export function useUpdateAlbum(albumId: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (input: Parameters<typeof updateAlbum>[1]) => updateAlbum(albumId, input),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["albums"] })
  });
}

export function useDeleteAlbum() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: deleteAlbum,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["albums"] })
  });
}

export function useAttachAlbumMedia(albumId: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (input: Parameters<typeof attachAlbumMedia>[1]) => attachAlbumMedia(albumId, input),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.albumMedia(albumId) });
      queryClient.invalidateQueries({ queryKey: ["albums"] });
    }
  });
}

export function useRemoveAlbumMedia(albumId: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (mediaId: string) => removeAlbumMedia(albumId, mediaId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.albumMedia(albumId) });
      queryClient.invalidateQueries({ queryKey: ["albums"] });
    }
  });
}

export function useUploadMedia() {
  return useMutation({
    mutationFn: (input: { file: File; onProgress?: (progress: number) => void }) => uploadMedia(input.file, input.onProgress)
  });
}
