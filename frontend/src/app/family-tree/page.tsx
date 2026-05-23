import { AppShell } from "@/components/layout/app-shell";
import { FamilyTreePageClient } from "@/components/family-tree/family-tree-page-client";

export default function FamilyTreePage() {
  return (
    <AppShell title="Family Tree">
      <FamilyTreePageClient />
    </AppShell>
  );
}
