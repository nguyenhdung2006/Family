import { AppShell } from "@/components/layout/app-shell";
import { MessengerView } from "@/components/messenger/messenger-view";

export default function MessengerPage() {
  return (
    <AppShell title="Messenger">
      <MessengerView />
    </AppShell>
  );
}
