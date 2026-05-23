import { AlbumsPageClient } from "@/components/albums/albums-page-client";
import { AppShell } from "@/components/layout/app-shell";

export default function AlbumsPage() {
  return (
    <AppShell title="Albums">
      <AlbumsPageClient />
    </AppShell>
  );
}
