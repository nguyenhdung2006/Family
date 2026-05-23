import { AppShell } from "@/components/layout/app-shell";
import { TimelineFeed } from "@/components/timeline/timeline-feed";

export default function TimelinePage() {
  return (
    <AppShell title="Timeline">
      <TimelineFeed />
    </AppShell>
  );
}
