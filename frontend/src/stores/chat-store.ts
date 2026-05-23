import { create } from "zustand";
import type { ChatRoomType } from "@/features/chat/types";
import type { FamilyBranch } from "@/features/family/types";

type NewRoomDraft = {
  name: string;
  type: ChatRoomType;
  branch: FamilyBranch | "";
};

type ChatStore = {
  activeRoomId: string | null;
  drafts: Record<string, string>;
  newRoomDraft: NewRoomDraft;
  setActiveRoomId: (roomId: string | null) => void;
  setDraft: (roomId: string, value: string) => void;
  setNewRoomDraft: (draft: NewRoomDraft) => void;
};

export const useChatStore = create<ChatStore>((set) => ({
  activeRoomId: null,
  drafts: {},
  newRoomDraft: { name: "", type: "GROUP", branch: "" },
  setActiveRoomId: (activeRoomId) => set({ activeRoomId }),
  setDraft: (roomId, value) => set((state) => ({ drafts: { ...state.drafts, [roomId]: value } })),
  setNewRoomDraft: (newRoomDraft) => set({ newRoomDraft })
}));
