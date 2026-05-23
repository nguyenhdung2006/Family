"use client";

import { FormEvent, useEffect } from "react";
import { CheckCheck, Plus, Send, Smile } from "lucide-react";
import { Avatar } from "@/components/ui/avatar";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { useChatMessages, useChatRooms, useCreateChatRoom, useMarkMessageSeen, useSendMessage } from "@/features/chat/hooks";
import { useCurrentUser } from "@/features/auth/hooks";
import type { ChatRoomType } from "@/features/chat/types";
import type { FamilyBranch } from "@/features/family/types";
import { useRoomSocket } from "@/lib/websocket/use-room-socket";
import { formatMessageTime } from "@/lib/utils/date";
import { useChatStore } from "@/stores/chat-store";
import { cn } from "@/lib/utils/cn";

export function MessengerView() {
  const { data: user } = useCurrentUser();
  const { data: rooms = [], isLoading: roomsLoading, error: roomsError } = useChatRooms();
  const { activeRoomId, setActiveRoomId, drafts, setDraft, newRoomDraft, setNewRoomDraft } = useChatStore();
  const roomId = activeRoomId ?? rooms[0]?.id ?? null;
  const activeRoom = rooms.find((room) => room.id === roomId);
  const { data: messages = [], isLoading: messagesLoading, error: messagesError } = useChatMessages(roomId);
  const sendMessage = useSendMessage(roomId ?? "");
  const markSeen = useMarkMessageSeen();
  const createRoom = useCreateChatRoom();

  useRoomSocket(roomId);

  useEffect(() => {
    if (!activeRoomId && rooms[0]) {
      setActiveRoomId(rooms[0].id);
    }
  }, [activeRoomId, rooms, setActiveRoomId]);

  useEffect(() => {
    const latest = [...messages].reverse().find((message) => !message.seenAt && !message.optimistic && message.senderId !== user?.id);
    if (latest) {
      markSeen.mutate(latest.id);
    }
  }, [markSeen, messages, user?.id]);

  async function submitRoom(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const name = newRoomDraft.name.trim();
    if (!name) {
      return;
    }
    const room = await createRoom.mutateAsync({
      name,
      type: newRoomDraft.type,
      branch: newRoomDraft.branch || null
    });
    setNewRoomDraft({ name: "", type: "GROUP", branch: "" });
    setActiveRoomId(room.id);
  }

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
        <form className="grid gap-2 border-b border-border-warm p-3" onSubmit={submitRoom}>
          <div className="flex items-center justify-between gap-2">
            <Badge tone="sage">New room</Badge>
            <Button type="submit" size="sm" disabled={createRoom.isPending}>
              <Plus className="h-4 w-4" /> {createRoom.isPending ? "Creating" : "Create"}
            </Button>
          </div>
          <Input value={newRoomDraft.name} onChange={(event) => setNewRoomDraft({ ...newRoomDraft, name: event.target.value })} placeholder="Room name" required />
          <div className="grid grid-cols-2 gap-2">
            <select className="min-h-11 rounded-lg border border-border-warm bg-white px-3 text-sm font-bold text-ink" value={newRoomDraft.type} onChange={(event) => setNewRoomDraft({ ...newRoomDraft, type: event.target.value as ChatRoomType })}>
              <option value="GROUP">Group</option>
              <option value="PRIVATE">Private</option>
            </select>
            <select className="min-h-11 rounded-lg border border-border-warm bg-white px-3 text-sm font-bold text-ink" value={newRoomDraft.branch} onChange={(event) => setNewRoomDraft({ ...newRoomDraft, branch: event.target.value as FamilyBranch | "" })}>
              <option value="">All</option>
              <option value="PATERNAL">Paternal</option>
              <option value="MATERNAL">Maternal</option>
            </select>
          </div>
          {createRoom.error ? <p className="font-bold text-[#C15A4A]">{createRoom.error.message}</p> : null}
        </form>
        <div className="h-[calc(100%-17rem)] overflow-y-auto p-2 hometree-scrollbar">
          {roomsLoading ? <p className="p-4 font-semibold text-muted">Loading rooms...</p> : null}
          {roomsError ? <p className="p-4 font-bold text-[#C15A4A]">{roomsError.message}</p> : null}
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
            <p className="font-semibold text-muted">{activeRoom ? `${activeRoom.type.toLowerCase()} room` : "Create or choose a room"}</p>
          </div>
        </header>

        <div className="overflow-y-auto bg-[#FFFDF7] p-4 hometree-scrollbar">
          <div className="mx-auto grid max-w-3xl gap-3">
            {messages.map((message) => {
              const own = Boolean(message.optimistic || (message.senderId && user?.id && message.senderId === user.id));
              return (
                <div key={message.id} className={cn("flex", own ? "justify-end" : "justify-start")}>
                  <div className={cn("max-w-[82%] rounded-lg px-4 py-3 shadow-sm", own ? "bg-wood text-white" : "bg-white text-ink")}>
                    {!own && message.senderName ? <p className="mb-1 text-xs font-black text-muted">{message.senderName}</p> : null}
                    <p className="whitespace-pre-wrap text-base font-semibold leading-7">{message.body}</p>
                    <p className={cn("mt-1 flex items-center justify-end gap-1 text-xs font-bold", own ? "text-white/75" : "text-muted")}>
                      {message.optimistic ? "Sending" : formatMessageTime(message.deliveredAt)}
                      {message.seenAt ? <CheckCheck className="h-4 w-4" /> : null}
                    </p>
                  </div>
                </div>
              );
            })}
            {messagesLoading ? <p className="py-12 text-center text-lg font-bold text-muted">Loading messages...</p> : null}
            {messagesError ? <p className="py-12 text-center text-lg font-bold text-[#C15A4A]">{messagesError.message}</p> : null}
            {!messagesLoading && !messages.length ? <p className="py-12 text-center text-lg font-bold text-muted">No messages here yet.</p> : null}
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
