"use client";

import { FormEvent, useEffect, useMemo, useState } from "react";
import { CheckCheck, Pencil, Plus, Send, Smile, X } from "lucide-react";
import { Avatar } from "@/components/ui/avatar";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { FormError, FormHint } from "@/components/forms/form-status";
import { useChatMessages, useChatRooms, useCreateChatRoom, useMarkMessageSeen, useSendMessage, useUpdateChatRoom } from "@/features/chat/hooks";
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
  const [editingRoomId, setEditingRoomId] = useState<string | null>(null);
  const [roomValidation, setRoomValidation] = useState<string | null>(null);
  const roomId = activeRoomId ?? rooms[0]?.id ?? null;
  const activeRoom = rooms.find((room) => room.id === roomId);
  const editingRoom = rooms.find((room) => room.id === editingRoomId);
  const { data: messages = [], isLoading: messagesLoading, error: messagesError } = useChatMessages(roomId);
  const sendMessage = useSendMessage(roomId ?? "");
  const markSeen = useMarkMessageSeen();
  const createRoom = useCreateChatRoom();
  const updateRoom = useUpdateChatRoom(editingRoomId ?? "");
  const isViewer = user?.role === "VIEWER";
  const chronologicalMessages = useMemo(
    () => [...messages].sort((left, right) => messageTimestamp(left.deliveredAt) - messageTimestamp(right.deliveredAt)),
    [messages]
  );

  useRoomSocket(roomId);

  useEffect(() => {
    if (!activeRoomId && rooms[0]) {
      setActiveRoomId(rooms[0].id);
    }
  }, [activeRoomId, rooms, setActiveRoomId]);

  useEffect(() => {
    if (isViewer) {
      return;
    }
    const latest = [...chronologicalMessages].reverse().find((message) => !message.seenAt && !message.optimistic && message.senderId !== user?.id);
    if (latest) {
      markSeen.mutate(latest.id);
    }
  }, [chronologicalMessages, isViewer, markSeen, user?.id]);

  async function submitRoom(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const name = newRoomDraft.name.trim();
    if (!name) {
      setRoomValidation("Room name is required.");
      return;
    }
    setRoomValidation(null);
    const payload = {
      name,
      type: newRoomDraft.type,
      branch: newRoomDraft.branch || null
    };
    const room = editingRoomId ? await updateRoom.mutateAsync(payload) : await createRoom.mutateAsync(payload);
    setNewRoomDraft({ name: "", type: "GROUP", branch: "" });
    setEditingRoomId(null);
    setActiveRoomId(room.id);
  }

  function startEditRoom(roomId: string) {
    const room = rooms.find((candidate) => candidate.id === roomId);
    if (!room) {
      return;
    }
    setEditingRoomId(room.id);
    setRoomValidation(null);
    setNewRoomDraft({
      name: room.name,
      type: room.type,
      branch: room.branch ?? ""
    });
  }

  function cancelEditRoom() {
    setEditingRoomId(null);
    setRoomValidation(null);
    setNewRoomDraft({ name: "", type: "GROUP", branch: "" });
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
        {!isViewer ? (
        <form className="grid gap-2 border-b border-border-warm p-3" onSubmit={submitRoom}>
          <div className="flex items-center justify-between gap-2">
            <div>
              <Badge tone="sage">{editingRoom ? "Edit room" : "New room"}</Badge>
              {editingRoom ? <FormHint className="mt-1">Editing {editingRoom.name}</FormHint> : null}
            </div>
            <div className="flex gap-2">
              {editingRoom ? (
                <Button type="button" variant="ghost" size="icon" aria-label="Cancel room edit" onClick={cancelEditRoom}>
                  <X className="h-4 w-4" />
                </Button>
              ) : null}
              <Button type="submit" size="sm" disabled={createRoom.isPending || updateRoom.isPending}>
                {editingRoom ? <Pencil className="h-4 w-4" /> : <Plus className="h-4 w-4" />}
                {updateRoom.isPending ? "Saving" : createRoom.isPending ? "Creating" : editingRoom ? "Save" : "Create"}
              </Button>
            </div>
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
          <FormError message={roomValidation ?? createRoom.error?.message ?? updateRoom.error?.message} />
        </form>
        ) : null}
        <div className="h-[calc(100%-17rem)] overflow-y-auto p-2 hometree-scrollbar">
          {roomsLoading ? <p className="p-4 font-semibold text-muted">Loading rooms...</p> : null}
          {roomsError ? <p className="p-4 font-bold text-[#C15A4A]">We could not load rooms. {roomsError.message}</p> : null}
          {rooms.map((room) => (
            <div
              key={room.id}
              className={cn(
                "flex min-h-16 w-full items-center gap-3 rounded-lg p-3 text-left transition",
                room.id === roomId ? "bg-warm-yellow/25" : "hover:bg-surface-soft"
              )}
            >
              <button className="flex min-w-0 flex-1 items-center gap-3 text-left" onClick={() => setActiveRoomId(room.id)}>
                <Avatar name={room.name} className="h-11 w-11" />
                <div className="min-w-0">
                  <p className="truncate font-black text-ink">{room.name}</p>
                  <p className="text-sm font-bold text-muted">{room.type.toLowerCase()} room</p>
                </div>
              </button>
              {!isViewer ? (
              <Button type="button" variant="ghost" size="icon" aria-label={`Edit ${room.name}`} onClick={() => startEditRoom(room.id)}>
                <Pencil className="h-4 w-4" />
              </Button>
              ) : null}
            </div>
          ))}
          {!roomsLoading && !roomsError && !rooms.length ? (
            <div className="p-4">
              <p className="font-black text-ink">No chat rooms yet.</p>
              <p className="mt-1 font-semibold text-muted">
                {isViewer ? "Family message rooms will appear here when you are added." : "Create the first room above to start a conversation."}
              </p>
            </div>
          ) : null}
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
            {chronologicalMessages.map((message) => {
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
            {messagesError ? <p className="py-12 text-center text-lg font-bold text-[#C15A4A]">We could not load messages. {messagesError.message}</p> : null}
            {!roomId && !roomsLoading ? (
              <div className="py-12 text-center">
                <p className="text-lg font-black text-ink">Choose a room</p>
                <p className="font-semibold text-muted">
                  {isViewer ? "Message rooms will appear when you are added." : "Create or choose a room to start chatting."}
                </p>
              </div>
            ) : null}
            {roomId && !messagesLoading && !messagesError && !messages.length ? (
              <div className="py-12 text-center">
                <p className="text-lg font-black text-ink">No messages here yet.</p>
                <p className="font-semibold text-muted">
                  {isViewer ? "New messages from family members will appear here." : "Send the first message below."}
                </p>
              </div>
            ) : null}
          </div>
        </div>

        {!isViewer ? (
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
        ) : null}
      </section>
    </Card>
  );
}

function messageTimestamp(value: string | null) {
  if (!value) {
    return 0;
  }
  const timestamp = Date.parse(value);
  return Number.isNaN(timestamp) ? 0 : timestamp;
}
