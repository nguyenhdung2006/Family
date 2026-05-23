"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { createRoom, listMessages, listRooms, markMessageSeen, sendMessage } from "@/features/chat/api";
import type { ChatMessage, SendMessageInput } from "@/features/chat/types";
import type { CurrentUser } from "@/features/auth/types";
import { queryKeys } from "@/lib/api/queryKeys";

export function useChatRooms() {
  return useQuery({
    queryKey: queryKeys.chatRooms,
    queryFn: listRooms
  });
}

export function useChatMessages(roomId?: string | null) {
  return useQuery({
    queryKey: roomId ? queryKeys.chatMessages(roomId) : ["chat", "rooms", "none", "messages"],
    queryFn: () => listMessages(roomId as string),
    enabled: Boolean(roomId)
  });
}

export function useSendMessage(roomId: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (input: SendMessageInput) => sendMessage(roomId, input),
    onMutate: async (input) => {
      await queryClient.cancelQueries({ queryKey: queryKeys.chatMessages(roomId) });
      const previous = queryClient.getQueryData<ChatMessage[]>(queryKeys.chatMessages(roomId)) ?? [];
      const user = queryClient.getQueryData<CurrentUser>(queryKeys.authMe);
      const optimistic: ChatMessage = {
        id: `optimistic-${Date.now()}`,
        roomId,
        senderId: user?.id ?? null,
        senderName: user?.name ?? null,
        type: input.type,
        body: input.body,
        mediaUrl: input.mediaUrl ?? null,
        deliveredAt: new Date().toISOString(),
        seenAt: null,
        optimistic: true
      };
      queryClient.setQueryData<ChatMessage[]>(queryKeys.chatMessages(roomId), [...previous, optimistic]);
      return { previous, optimisticId: optimistic.id };
    },
    onError: (_error, _input, context) => {
      if (!context) {
        return;
      }
      queryClient.setQueryData<ChatMessage[]>(
        queryKeys.chatMessages(roomId),
        context.previous.map((message) => (message.id === context.optimisticId ? { ...message, failed: true } : message))
      );
    },
    onSuccess: (created, _input, context) => {
      queryClient.setQueryData<ChatMessage[]>(queryKeys.chatMessages(roomId), (messages = []) =>
        messages.map((message) => (message.id === context?.optimisticId ? created : message))
      );
    },
    onSettled: () => queryClient.invalidateQueries({ queryKey: queryKeys.chatRooms })
  });
}

export function useMarkMessageSeen() {
  return useMutation({ mutationFn: markMessageSeen });
}

export function useCreateChatRoom() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: createRoom,
    onSuccess: (room) => {
      queryClient.invalidateQueries({ queryKey: queryKeys.chatRooms });
      return room;
    }
  });
}
