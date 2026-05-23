import { AppShell } from "@/components/layout/app-shell";
import { FamilyTreeCanvas } from "@/components/family-tree/family-tree-canvas";

export default function FamilyTreePage() {
  return (
    <AppShell title="Family Tree">
      <FamilyTreeCanvas />
    </AppShell>
  );
}
