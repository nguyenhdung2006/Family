export type CurrentUser = {
  id: string | null;
  name: string;
  email: string | null;
  avatarUrl: string | null;
  role: "ADMIN" | "MEMBER" | "VIEWER" | null;
  roles: Array<"ADMIN" | "MEMBER" | "VIEWER">;
};

export type LoginOptions = {
  options: {
    googleOAuthPath: string;
    sessionProbePath: string;
  };
};
