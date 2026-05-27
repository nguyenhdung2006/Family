"use client";

import { FormEvent, useState } from "react";
import { Pencil, Save, X } from "lucide-react";
import { AppShell } from "@/components/layout/app-shell";
import { Avatar } from "@/components/ui/avatar";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { useCurrentUser, useUpdateCurrentUserProfile } from "@/features/auth/hooks";

export default function ProfilePage() {
  const { data: user, isLoading, error, refetch } = useCurrentUser();
  const updateProfile = useUpdateCurrentUserProfile();
  const [isEditing, setIsEditing] = useState(false);
  const [form, setForm] = useState({ displayName: "", avatarUrl: "" });
  const [validation, setValidation] = useState<string | null>(null);

  function startEdit() {
    setForm({
      displayName: user?.name ?? "",
      avatarUrl: user?.avatarUrl ?? ""
    });
    setValidation(null);
    setIsEditing(true);
  }

  function cancelEdit() {
    setIsEditing(false);
    setValidation(null);
  }

  async function submitProfile(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const displayName = form.displayName.trim();
    if (!displayName) {
      setValidation("Display name is required.");
      return;
    }
    await updateProfile.mutateAsync({
      displayName,
      avatarUrl: form.avatarUrl.trim() || null
    });
    setIsEditing(false);
    setValidation(null);
  }

  return (
    <AppShell title="Profile">
      <Card className="max-w-3xl">
        <CardContent>
          {isLoading ? <p className="font-bold text-muted">Loading profile...</p> : null}
          {error ? (
            <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
              <p className="font-bold text-[#C15A4A]">We could not load your profile. {error.message}</p>
              <Button variant="secondary" onClick={() => void refetch()}>Try again</Button>
            </div>
          ) : null}
          <div className="flex flex-col gap-5 sm:flex-row sm:items-center">
            <Avatar name={user?.name ?? "Family Member"} src={user?.avatarUrl} className="h-24 w-24" />
            {isEditing ? (
              <form className="grid flex-1 gap-3" onSubmit={submitProfile}>
                <div className="flex items-center justify-between gap-3">
                  <h2 className="text-2xl font-black text-ink">Edit profile</h2>
                  <Button type="button" variant="ghost" size="icon" aria-label="Cancel profile edit" onClick={cancelEdit}>
                    <X className="h-5 w-5" />
                  </Button>
                </div>
                <Input
                  value={form.displayName}
                  onChange={(event) => setForm((current) => ({ ...current, displayName: event.target.value }))}
                  placeholder="Display name"
                  maxLength={255}
                  required
                />
                <Input
                  value={form.avatarUrl}
                  onChange={(event) => setForm((current) => ({ ...current, avatarUrl: event.target.value }))}
                  placeholder="Avatar image URL"
                  maxLength={2000}
                />
                <p className="text-base font-semibold text-muted">{user?.email ?? "Signed in family profile"}</p>
                <p className="text-base font-semibold text-muted">Role: {user?.role ?? "MEMBER"}</p>
                {validation || updateProfile.error ? <p className="font-bold text-[#C15A4A]">{validation ?? updateProfile.error?.message}</p> : null}
                <div className="flex flex-wrap gap-2">
                  <Button type="submit" disabled={updateProfile.isPending}>
                    <Save className="h-5 w-5" /> {updateProfile.isPending ? "Saving..." : "Save profile"}
                  </Button>
                  <Button type="button" variant="secondary" onClick={cancelEdit}>Cancel</Button>
                </div>
              </form>
            ) : (
              <div className="flex-1">
                <div className="flex flex-wrap items-start justify-between gap-3">
                  <div>
                    <h2 className="text-3xl font-black text-ink">{user?.name ?? "Family Member"}</h2>
                    <p className="mt-1 text-lg font-bold text-muted">{user?.email ?? "Signed in family profile"}</p>
                  </div>
                  {user ? (
                    <Button type="button" variant="secondary" onClick={startEdit}>
                      <Pencil className="h-5 w-5" /> Edit
                    </Button>
                  ) : null}
                </div>
                <p className="mt-4 max-w-2xl text-base font-semibold leading-7 text-muted">
                  Role: {user?.role ?? "MEMBER"}
                </p>
              </div>
            )}
          </div>
        </CardContent>
      </Card>
    </AppShell>
  );
}
