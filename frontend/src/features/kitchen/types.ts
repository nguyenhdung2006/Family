export type Recipe = {
  id: string;
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
