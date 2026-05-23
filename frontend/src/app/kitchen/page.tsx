import { AppShell } from "@/components/layout/app-shell";
import { KitchenView } from "@/components/kitchen/kitchen-view";

export default function KitchenPage() {
  return (
    <AppShell title="Kitchen">
      <KitchenView />
    </AppShell>
  );
}
