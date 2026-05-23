"use client";

import dynamic from "next/dynamic";
import { Card, CardContent } from "@/components/ui/card";

const AlbumsGallery = dynamic(
  () => import("@/components/albums/albums-gallery").then((module) => module.AlbumsGallery),
  {
    ssr: false,
    loading: () => (
      <Card>
        <CardContent>
          <p className="text-lg font-bold text-muted">Preparing the family albums...</p>
        </CardContent>
      </Card>
    )
  }
);

export function AlbumsPageClient() {
  return <AlbumsGallery />;
}
