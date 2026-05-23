import { AlbumsGallery } from "@/components/albums/albums-gallery";
import { AppShell } from "@/components/layout/app-shell";

export default function AlbumsPage() {
  return (
    <AppShell title="Albums">
      <AlbumsGallery />
    </AppShell>
  );
}
