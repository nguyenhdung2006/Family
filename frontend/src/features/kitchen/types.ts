export type Recipe = {
  id: string;
  createdById: string | null;
  createdByName: string | null;
  title: string;
  description: string | null;
  ingredients: string;
  instructions: string;
  videoUrl: string | null;
  notesFromElders: string | null;
};

export type CreateRecipeInput = {
  title: string;
  description?: string | null;
  ingredients: string;
  instructions: string;
  videoUrl?: string | null;
  notesFromElders?: string | null;
};

export type UpdateRecipeInput = CreateRecipeInput;
