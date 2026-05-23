"use client";

import { useQuery } from "@tanstack/react-query";
import { listAlbumMedia, listAlbums } from "@/features/albums/api";
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
