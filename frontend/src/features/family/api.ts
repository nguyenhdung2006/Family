import { apiFetch } from "@/lib/api/client";
import type { PageParams } from "@/lib/api/types";
import type {
  CreateFamilyMemberInput,
  CreateFamilyRelationshipInput,
  FamilyBranch,
  FamilyMember,
  FamilyRelationship,
  UpdateFamilyMemberInput,
  UpdateFamilyRelationshipInput
} from "@/features/family/types";

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

export function createFamilyMember(input: CreateFamilyMemberInput) {
  return apiFetch<FamilyMember>("/api/family/members", {
    method: "POST",
    body: input
  });
}

export function updateFamilyMember(id: string, input: UpdateFamilyMemberInput) {
  return apiFetch<FamilyMember>(`/api/family/members/${id}`, {
    method: "PUT",
    body: input
  });
}

export function listRelationships(memberId: string) {
  return apiFetch<FamilyRelationship[]>(`/api/family/members/${memberId}/relationships`);
}

export function createRelationship(input: CreateFamilyRelationshipInput) {
  return apiFetch<FamilyRelationship>("/api/family/relationships", {
    method: "POST",
    body: input
  });
}

export function updateRelationship(id: string, input: UpdateFamilyRelationshipInput) {
  return apiFetch<FamilyRelationship>(`/api/family/relationships/${id}`, {
    method: "PUT",
    body: input
  });
}
