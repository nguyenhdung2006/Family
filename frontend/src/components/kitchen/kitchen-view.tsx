"use client";

import { ChefHat, PlayCircle } from "lucide-react";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent } from "@/components/ui/card";
import { useRecipes } from "@/features/kitchen/hooks";

export function KitchenView() {
  const { data: recipes = [] } = useRecipes();

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
