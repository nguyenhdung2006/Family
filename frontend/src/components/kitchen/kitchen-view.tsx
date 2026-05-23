"use client";

import { FormEvent, useState } from "react";
import { ChefHat, PlayCircle, Plus } from "lucide-react";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Input, Textarea } from "@/components/ui/input";
import { useCreateRecipe, useRecipes } from "@/features/kitchen/hooks";

export function KitchenView() {
  const { data: recipes = [], isLoading, error } = useRecipes();
  const createRecipe = useCreateRecipe();
  const [form, setForm] = useState({
    title: "",
    description: "",
    ingredients: "",
    instructions: "",
    videoUrl: "",
    notesFromElders: ""
  });

  async function submitRecipe(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!form.title.trim() || !form.ingredients.trim() || !form.instructions.trim()) {
      return;
    }
    await createRecipe.mutateAsync({
      title: form.title.trim(),
      description: form.description.trim() || null,
      ingredients: form.ingredients.trim(),
      instructions: form.instructions.trim(),
      videoUrl: form.videoUrl.trim() || null,
      notesFromElders: form.notesFromElders.trim() || null
    });
    setForm({ title: "", description: "", ingredients: "", instructions: "", videoUrl: "", notesFromElders: "" });
  }

  return (
    <div className="grid gap-5">
      <Card className="bg-[linear-gradient(135deg,#FFF8EA,#FFFFFF)]">
        <CardContent>
          <Badge tone="sage">Family kitchen</Badge>
          <h2 className="mt-3 text-4xl font-black text-ink">Recipes that remember people</h2>
          <p className="mt-3 max-w-3xl text-lg font-semibold leading-8 text-muted">
            Preserve ingredients, elder notes, cooking videos, and the small details that make a dish feel like home.
          </p>
        </CardContent>
      </Card>

      <Card>
        <CardContent>
          <form className="grid gap-3" onSubmit={submitRecipe}>
            <div className="flex items-center justify-between gap-3">
              <h2 className="text-xl font-black text-ink">Add recipe</h2>
              <Badge tone="yellow">Kitchen</Badge>
            </div>
            <div className="grid gap-3 md:grid-cols-2">
              <Input value={form.title} onChange={(event) => setForm((current) => ({ ...current, title: event.target.value }))} placeholder="Recipe title" required />
              <Input value={form.videoUrl} onChange={(event) => setForm((current) => ({ ...current, videoUrl: event.target.value }))} placeholder="Video URL" />
            </div>
            <Textarea value={form.description} onChange={(event) => setForm((current) => ({ ...current, description: event.target.value }))} placeholder="Description" />
            <div className="grid gap-3 md:grid-cols-2">
              <Textarea value={form.ingredients} onChange={(event) => setForm((current) => ({ ...current, ingredients: event.target.value }))} placeholder="Ingredients" required />
              <Textarea value={form.instructions} onChange={(event) => setForm((current) => ({ ...current, instructions: event.target.value }))} placeholder="Instructions" required />
            </div>
            <Textarea value={form.notesFromElders} onChange={(event) => setForm((current) => ({ ...current, notesFromElders: event.target.value }))} placeholder="Notes from elders" />
            {createRecipe.error ? <p className="font-bold text-[#C15A4A]">{createRecipe.error.message}</p> : null}
            <Button type="submit" disabled={createRecipe.isPending}>
              <Plus className="h-5 w-5" /> {createRecipe.isPending ? "Saving..." : "Save recipe"}
            </Button>
          </form>
        </CardContent>
      </Card>

      {error ? <Card><CardContent><p className="font-bold text-[#C15A4A]">{error.message}</p></CardContent></Card> : null}
      {isLoading ? <Card><CardContent><p className="font-bold text-muted">Loading recipes...</p></CardContent></Card> : null}

      <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
        {recipes.map((recipe) => (
          <Card key={recipe.id} className="overflow-hidden">
            <div className="grid aspect-[5/3] place-items-center bg-surface-soft text-wood">
              <ChefHat className="h-14 w-14" />
            </div>
            <CardContent>
              <h3 className="text-2xl font-black text-ink">{recipe.title}</h3>
              <p className="mt-2 line-clamp-3 font-semibold leading-7 text-muted">{recipe.description ?? recipe.notesFromElders ?? "A family recipe."}</p>
              <div className="mt-4 grid gap-3">
                <div>
                  <p className="font-black text-ink">Ingredients</p>
                  <p className="line-clamp-3 whitespace-pre-wrap font-semibold leading-7 text-muted">{recipe.ingredients}</p>
                </div>
                {recipe.videoUrl ? (
                  <a href={recipe.videoUrl} className="inline-flex min-h-11 items-center gap-2 rounded-lg bg-wood px-4 font-bold text-white">
                    <PlayCircle className="h-5 w-5" /> Watch
                  </a>
                ) : null}
              </div>
            </CardContent>
          </Card>
        ))}
      </div>
      {!recipes.length ? <Card><CardContent><p className="text-lg font-bold text-muted">No recipes yet.</p></CardContent></Card> : null}
    </div>
  );
}
