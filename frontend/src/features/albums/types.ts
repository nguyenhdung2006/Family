export type AlbumCategory = "TET" | "WEDDING" | "TRAVEL" | "BIRTHDAY" | "MEMORIAL" | "EVERYDAY" | "OTHER";
export type MediaType = "IMAGE" | "VIDEO";

export type Album = {
  id: string;
  createdById: string | null;
  createdByName: string | null;
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
  albumId: string | null;
  uploadedById: string | null;
};

export type MediaUploadResponse = {
  url: string;
  storagePublicId: string;
  mediaType: MediaType;
  contentType: string;
  sizeBytes: number;
};

export type CreateAlbumInput = {
  title: string;
  description?: string | null;
  category: AlbumCategory;
};

export type UpdateAlbumInput = CreateAlbumInput;

export type AttachAlbumMediaInput = {
  url: string;
  storagePublicId?: string | null;
  mediaType: MediaType;
  caption?: string | null;
  capturedAt?: string | null;
};
