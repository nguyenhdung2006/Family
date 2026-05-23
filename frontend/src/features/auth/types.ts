export type CurrentUser = {
  id: string | null;
  name: string;
  email: string | null;
  avatarUrl: string | null;
};

export type LoginOptions = {
  options: {
    googleOAuthPath: string;
    sessionProbePath: string;
  };
};
