import type { FamilyBranch } from "@/features/family/types";

export type ChatRoomType = "PRIVATE" | "GROUP";
export type MessageType = "TEXT" | "IMAGE" | "EMOJI";

export type ChatRoom = {
  id: string;
  name: string;
  type: ChatRoomType;
  branch: FamilyBranch | null;
};

export type ChatMessage = {
  id: string;
  roomId: string;
  senderId: string | null;
  senderName: string | null;
  type: MessageType;
  body: string;
  mediaUrl: string | null;
  deliveredAt: string | null;
  seenAt: string | null;
  optimistic?: boolean;
  failed?: boolean;
};

export type SendMessageInput = {
  type: MessageType;
  body: string;
  mediaUrl?: string | null;
};

export type CreateChatRoomInput = {
  name: string;
  type: ChatRoomType;
  branch?: FamilyBranch | null;
};

export type UpdateChatRoomInput = CreateChatRoomInput;
