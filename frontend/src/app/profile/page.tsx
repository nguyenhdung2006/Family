"use client";

import { AppShell } from "@/components/layout/app-shell";
import { Avatar } from "@/components/ui/avatar";
import { Card, CardContent } from "@/components/ui/card";
import { useCurrentUser } from "@/features/auth/hooks";

export default function ProfilePage() {
  const { data: user } = useCurrentUser();

  return (
    <AppShell title="Profile">
      <Card className="max-w-3xl">
        <CardContent>
          <div className="flex flex-col gap-5 sm:flex-row sm:items-center">
            <Avatar name={user?.name ?? "Family Member"} src={user?.avatarUrl} className="h-24 w-24" />
            <div>
              <h2 className="text-3xl font-black text-ink">{user?.name ?? "Family Member"}</h2>
              <p className="mt-1 text-lg font-bold text-muted">{user?.email ?? "Signed in family profile"}</p>
              <p className="mt-4 max-w-2xl text-base font-semibold leading-7 text-muted">
                Profile editing can connect to the user/family-member API when the backend exposes update endpoints.
              </p>
            </div>
          </div>
        </CardContent>
      </Card>
    </AppShell>
  );
}
