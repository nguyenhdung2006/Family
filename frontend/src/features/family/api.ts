import { apiFetch } from "@/lib/api/client";
import type { PageParams } from "@/lib/api/types";
import type { FamilyBranch, FamilyMember, FamilyRelationship } from "@/features/family/types";

export type FamilyMemberFilters = PageParams & {
  branch?: FamilyBranch;
};

export async function listFamilyMembers(filters: FamilyMemberFilters = {}) {
  if (filters.branch) {
    return apiFetch<FamilyMember[]>(`/api/family/branches/${filters.branch}/members`, { params: filters });
  }
  return apiFetch<FamilyMember[]>("/api/family/members", { params: filters });
}

export function getFamilyMember(id: string) {
  return apiFetch<FamilyMember>(`/api/family/members/${id}`);
}

export function listRelationships(memberId: string) {
  return apiFetch<FamilyRelationship[]>(`/api/family/members/${memberId}/relationships`);
}
