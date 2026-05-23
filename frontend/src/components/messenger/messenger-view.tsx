"use client";

import { FormEvent, useEffect } from "react";
import { CheckCheck, Send, Smile } from "lucide-react";
import { Avatar } from "@/components/ui/avatar";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { useChatMessages, useChatRooms, useMarkMessageSeen, useSendMessage } from "@/features/chat/hooks";
import { useRoomSocket } from "@/lib/websocket/use-room-socket";
import { formatMessageTime } from "@/lib/utils/date";
import { useChatStore } from "@/stores/chat-store";
import { cn } from "@/lib/utils/cn";

export function MessengerView() {
  const { data: rooms = [] } = useChatRooms();
  const { activeRoomId, setActiveRoomId, drafts, setDraft } = useChatStore();
  const roomId = activeRoomId ?? rooms[0]?.id ?? null;
  const activeRoom = rooms.find((room) => room.id === roomId);
  const { data: messages = [] } = useChatMessages(roomId);
  const sendMessage = useSendMessage(roomId ?? "");
  const markSeen = useMarkMessageSeen();

  useRoomSocket(roomId);

  useEffect(() => {
    if (!activeRoomId && rooms[0]) {
      setActiveRoomId(rooms[0].id);
    }
  }, [activeRoomId, rooms, setActiveRoomId]);

  useEffect(() => {
    const latest = [...messages].reverse().find((message) => !message.seenAt && !message.optimistic);
    if (latest) {
      markSeen.mutate(latest.id);
    }
  }, [markSeen, messages]);

  function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!roomId) {
      return;
    }
    const body = (drafts[roomId] ?? "").trim();
    if (!body) {
      return;
    }
    setDraft(roomId, "");
    sendMessage.mutate({ type: "TEXT", body });
  }

  return (
    <Card className="grid h-[calc(100vh-8rem)] min-h-[38rem] overflow-hidden lg:grid-cols-[20rem_minmax(0,1fr)]">
      <aside className={cn("border-b border-border-warm bg-surface lg:border-b-0 lg:border-r", roomId ? "hidden lg:block" : "block")}>
        <div className="border-b border-border-warm p-4">
          <h2 className="text-2xl font-black text-ink">Messages</h2>
          <p className="font-semibold text-muted">Family rooms and daily check-ins</p>
        </div>
        <div className="h-[calc(100%-5.7rem)] overflow-y-auto p-2 hometree-scrollbar">
          {rooms.map((room) => (
            <button
              key={room.id}
              className={cn(
                "flex min-h-16 w-full items-center gap-3 rounded-lg p-3 text-left transition",
                room.id === roomId ? "bg-warm-yellow/25" : "hover:bg-surface-soft"
              )}
              onClick={() => setActiveRoomId(room.id)}
            >
              <Avatar name={room.name} className="h-11 w-11" />
              <div>
                <p className="font-black text-ink">{room.name}</p>
                <p className="text-sm font-bold text-muted">{room.type.toLowerCase()} room</p>
              </div>
            </button>
          ))}
          {!rooms.length ? <p className="p-4 font-semibold text-muted">No chat rooms yet.</p> : null}
        </div>
      </aside>

      <section className={cn("grid min-h-0 grid-rows-[auto_1fr_auto]", !roomId ? "hidden lg:grid" : "grid")}>
        <header className="flex min-h-20 items-center gap-3 border-b border-border-warm bg-white p-4">
          <Avatar name={activeRoom?.name ?? "Room"} />
          <div>
            <h2 className="text-xl font-black text-ink">{activeRoom?.name ?? "Choose a room"}</h2>
            <p className="font-semibold text-muted">Typing indicators ready for backend support</p>
          </div>
        </header>

        <div className="overflow-y-auto bg-[#FFFDF7] p-4 hometree-scrollbar">
          <div className="mx-auto grid max-w-3xl gap-3">
            {messages.map((message, index) => {
              const own = index % 2 === 0;
              return (
                <div key={message.id} className={cn("flex", own ? "justify-end" : "justify-start")}>
                  <div className={cn("max-w-[82%] rounded-lg px-4 py-3 shadow-sm", own ? "bg-wood text-white" : "bg-white text-ink")}>
                    <p className="whitespace-pre-wrap text-base font-semibold leading-7">{message.body}</p>
                    <p className={cn("mt-1 flex items-center justify-end gap-1 text-xs font-bold", own ? "text-white/75" : "text-muted")}>
                      {message.optimistic ? "Sending" : formatMessageTime(message.deliveredAt)}
                      {message.seenAt ? <CheckCheck className="h-4 w-4" /> : null}
                    </p>
                  </div>
                </div>
              );
            })}
            {!messages.length ? <p className="py-12 text-center text-lg font-bold text-muted">No messages here yet.</p> : null}
          </div>
        </div>

        <form className="flex items-center gap-2 border-t border-border-warm bg-white p-3" onSubmit={submit}>
          <Button variant="ghost" size="icon" aria-label="Emoji">
            <Smile className="h-5 w-5" />
          </Button>
          <Input
            disabled={!roomId}
            value={roomId ? drafts[roomId] ?? "" : ""}
            onChange={(event) => roomId && setDraft(roomId, event.target.value)}
            placeholder="Write a message..."
          />
          <Button type="submit" size="icon" disabled={!roomId || sendMessage.isPending} aria-label="Send message">
            <Send className="h-5 w-5" />
          </Button>
        </form>
      </section>
    </Card>
  );
}
