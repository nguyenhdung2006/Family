export type FamilyBranch = "PATERNAL" | "MATERNAL";
export type RelationshipType = "PARENT_CHILD" | "SPOUSE" | "SIBLING";

export type FamilyMember = {
  id: string;
  fullName: string;
  birthDate: string | null;
  deathDate: string | null;
  roleInFamily: string | null;
  branch: FamilyBranch;
  avatarUrl: string | null;
  biography: string | null;
  generationLevel: number;
  deceased: boolean;
};

export type FamilyRelationship = {
  id: string;
  sourceMemberId: string;
  targetMemberId: string;
  type: RelationshipType;
  notes: string | null;
};

export type CreateFamilyMemberInput = {
  fullName: string;
  birthDate?: string | null;
  deathDate?: string | null;
  roleInFamily?: string | null;
  branch: FamilyBranch;
  avatarUrl?: string | null;
  biography?: string | null;
  generationLevel: number;
};

export type CreateFamilyRelationshipInput = {
  sourceMemberId: string;
  targetMemberId: string;
  type: RelationshipType;
  notes?: string | null;
};
