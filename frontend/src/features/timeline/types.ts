import type { FamilyBranch } from "@/features/family/types";

export type EventType = "FAMILY_GATHERING" | "BIRTHDAY" | "WEDDING" | "TET" | "TRAVEL" | "MEMORIAL" | "EVERYDAY" | "OTHER";

export type MemoryPost = {
  id: string;
  text: string;
  occurredAt: string;
  locationName: string | null;
  eventType: EventType;
  taggedMemberIds: string[];
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
};
