export type AlbumCategory = "TET" | "WEDDING" | "TRAVEL" | "BIRTHDAY" | "MEMORIAL" | "EVERYDAY" | "OTHER";
export type MediaType = "IMAGE" | "VIDEO";

export type Album = {
  id: string;
  title: string;
  description: string | null;
  category: AlbumCategory;
};

export type AlbumMedia = {
  id: string;
  url: string;
  mediaType: MediaType;
  caption: string | null;
  capturedAt: string | null;
};

export type CreateAlbumInput = {
  title: string;
  description?: string | null;
  category: AlbumCategory;
};

export type AttachAlbumMediaInput = {
  url: string;
  storagePublicId?: string | null;
  mediaType: MediaType;
  caption?: string | null;
  capturedAt?: string | null;
};
