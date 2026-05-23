"use client";

import dynamic from "next/dynamic";
import { Card, CardContent } from "@/components/ui/card";

const MessengerView = dynamic(
  () => import("@/components/messenger/messenger-view").then((module) => module.MessengerView),
  {
    ssr: false,
    loading: () => (
      <Card>
        <CardContent>
          <p className="text-lg font-bold text-muted">Opening family messages...</p>
        </CardContent>
      </Card>
    )
  }
);

export function MessengerPageClient() {
  return <MessengerView />;
}
