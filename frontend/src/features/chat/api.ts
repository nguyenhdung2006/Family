import { apiFetch } from "@/lib/api/client";
import type { ChatMessage, ChatRoom, CreateChatRoomInput, SendMessageInput, UpdateChatRoomInput } from "@/features/chat/types";

export function listRooms() {
  return apiFetch<ChatRoom[]>("/api/messages/rooms");
}

export function createRoom(input: CreateChatRoomInput) {
  return apiFetch<ChatRoom>("/api/messages/rooms", {
    method: "POST",
    body: input
  });
}

export function updateRoom(roomId: string, input: UpdateChatRoomInput) {
  return apiFetch<ChatRoom>(`/api/messages/rooms/${roomId}`, {
    method: "PUT",
    body: input
  });
}

export function listMessages(roomId: string, page = 0, size = 50) {
  return apiFetch<ChatMessage[]>(`/api/messages/rooms/${roomId}`, { params: { page, size } });
}

export function sendMessage(roomId: string, input: SendMessageInput) {
  return apiFetch<ChatMessage>(`/api/messages/rooms/${roomId}`, {
    method: "POST",
    body: input
  });
}

export function markMessageSeen(messageId: string) {
  return apiFetch<ChatMessage>(`/api/messages/${messageId}/seen`, {
    method: "PATCH"
  });
}
