import type { FamilyBranch } from "@/features/family/types";

export type EventType = "FAMILY_GATHERING" | "BIRTHDAY" | "WEDDING" | "TET" | "TRAVEL" | "MEMORIAL" | "EVERYDAY" | "OTHER";

export type MemoryPost = {
  id: string;
  authorId: string | null;
  authorName: string | null;
  text: string;
  occurredAt: string;
  locationName: string | null;
  eventType: EventType;
  taggedMemberIds: string[];
  media: TimelineMedia[];
};

export type TimelineMedia = {
  id: string;
  url: string;
  storagePublicId: string | null;
  mediaType: "IMAGE" | "VIDEO";
};

export type TimelineFilters = {
  page?: number;
  size?: number;
  year?: number;
  eventType?: EventType;
  authorId?: string;
  branch?: FamilyBranch;
};

export type CreateMemoryPost = {
  text: string;
  occurredAt: string;
  locationName?: string;
  eventType: EventType;
  taggedMemberIds: string[];
  mediaUrl?: string | null;
  mediaStoragePublicId?: string | null;
  mediaType?: "IMAGE" | "VIDEO" | null;
};

export type UpdateMemoryPost = CreateMemoryPost;
