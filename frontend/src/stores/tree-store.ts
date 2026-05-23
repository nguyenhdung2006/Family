import { create } from "zustand";

type TreeStore = {
  selectedMemberId: string | null;
  expandedMemberIds: string[];
  setSelectedMemberId: (id: string | null) => void;
  toggleExpanded: (id: string) => void;
};

export const useTreeStore = create<TreeStore>((set) => ({
  selectedMemberId: null,
  expandedMemberIds: [],
  setSelectedMemberId: (selectedMemberId) => set({ selectedMemberId }),
  toggleExpanded: (id) =>
    set((state) => ({
      expandedMemberIds: state.expandedMemberIds.includes(id)
        ? state.expandedMemberIds.filter((memberId) => memberId !== id)
        : [...state.expandedMemberIds, id]
    }))
}));
