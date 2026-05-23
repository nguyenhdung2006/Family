import { apiFetch } from "@/lib/api/client";
import type { MemorialMember, Tribute } from "@/features/memorial/types";

export function listMemorialMembers() {
  return apiFetch<MemorialMember[]>("/api/memorials");
}

export function listTributes(memberId: string) {
  return apiFetch<Tribute[]>(`/api/memorials/${memberId}/tributes`);
}
