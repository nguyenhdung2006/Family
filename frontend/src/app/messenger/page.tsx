import { AppShell } from "@/components/layout/app-shell";
import { MessengerPageClient } from "@/components/messenger/messenger-page-client";

export default function MessengerPage() {
  return (
    <AppShell title="Messenger">
      <MessengerPageClient />
    </AppShell>
  );
}
