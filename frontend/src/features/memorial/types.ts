export type MemorialMember = {
  id: string;
  fullName: string;
  roleInFamily: string | null;
};

export type Tribute = {
  id: string;
  memberId: string;
  title: string;
  story: string;
};
