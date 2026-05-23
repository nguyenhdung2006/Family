"use client";

import { useEffect } from "react";
import { useQueryClient } from "@tanstack/react-query";
import type { ChatMessage } from "@/features/chat/types";
import { queryKeys } from "@/lib/api/queryKeys";
import { createStompClient, subscribeJson } from "@/lib/websocket/stomp-client";

export function useRoomSocket(roomId?: string | null) {
  const queryClient = useQueryClient();

  useEffect(() => {
    if (!roomId) {
      return;
    }

    const client = createStompClient();
    client.onConnect = () => {
      subscribeJson<ChatMessage>(client, `/topic/rooms/${roomId}`, (incoming) => {
        queryClient.setQueryData<ChatMessage[]>(queryKeys.chatMessages(roomId), (messages = []) => {
          if (messages.some((message) => message.id === incoming.id)) {
            return messages;
          }
          return [...messages.filter((message) => !message.optimistic), incoming];
        });
      });
    };
    client.activate();
    return () => {
      void client.deactivate();
    };
  }, [queryClient, roomId]);
}
