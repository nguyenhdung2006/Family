"use client";

import dynamic from "next/dynamic";
import { Card, CardContent } from "@/components/ui/card";

const FamilyTreeCanvas = dynamic(
  () => import("@/components/family-tree/family-tree-canvas").then((module) => module.FamilyTreeCanvas),
  {
    ssr: false,
    loading: () => (
      <Card>
        <CardContent>
          <p className="text-lg font-bold text-muted">Preparing the family tree...</p>
        </CardContent>
      </Card>
    )
  }
);

export function FamilyTreePageClient() {
  return <FamilyTreeCanvas />;
}
