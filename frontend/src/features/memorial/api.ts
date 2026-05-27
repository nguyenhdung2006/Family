import { apiFetch } from "@/lib/api/client";
import type { CreateTributeInput, MemorialMember, Tribute, UpdateTributeInput } from "@/features/memorial/types";

export function listMemorialMembers() {
  return apiFetch<MemorialMember[]>("/api/memorials");
}

export function listTributes(memberId: string) {
  return apiFetch<Tribute[]>(`/api/memorials/${memberId}/tributes`);
}

export function createTribute(memberId: string, input: CreateTributeInput) {
  return apiFetch<Tribute>(`/api/memorials/${memberId}/tributes`, {
    method: "POST",
    body: input
  });
}

export function updateTribute(memberId: string, tributeId: string, input: UpdateTributeInput) {
  return apiFetch<Tribute>(`/api/memorials/${memberId}/tributes/${tributeId}`, {
    method: "PUT",
    body: input
  });
}

export function deleteTribute(memberId: string, tributeId: string) {
  return apiFetch<void>(`/api/memorials/${memberId}/tributes/${tributeId}`, {
    method: "DELETE"
  });
}
