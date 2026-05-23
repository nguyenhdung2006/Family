import { create } from "zustand";

type ChatStore = {
  activeRoomId: string | null;
  drafts: Record<string, string>;
  setActiveRoomId: (roomId: string | null) => void;
  setDraft: (roomId: string, value: string) => void;
};

export const useChatStore = create<ChatStore>((set) => ({
  activeRoomId: null,
  drafts: {},
  setActiveRoomId: (activeRoomId) => set({ activeRoomId }),
  setDraft: (roomId, value) => set((state) => ({ drafts: { ...state.drafts, [roomId]: value } }))
}));
