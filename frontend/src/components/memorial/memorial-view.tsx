"use client";

import { FormEvent, useState } from "react";
import { Flame, Heart, Pencil, Plus, Trash2, X } from "lucide-react";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { FormError, FormHint } from "@/components/forms/form-status";
import { Input, Textarea } from "@/components/ui/input";
import { useCurrentUser } from "@/features/auth/hooks";
import type { CurrentUser } from "@/features/auth/types";
import { useCreateTribute, useDeleteTribute, useMemorialMembers, useTributes, useUpdateTribute } from "@/features/memorial/hooks";
import type { Tribute } from "@/features/memorial/types";

export function MemorialView() {
  const { data: user } = useCurrentUser();
  const { data: members = [], isLoading, error } = useMemorialMembers();
  const [selectedMemberId, setSelectedMemberId] = useState<string | null>(null);
  const [tributeForm, setTributeForm] = useState({ title: "", story: "" });
  const [editingTributeId, setEditingTributeId] = useState<string | null>(null);
  const [tributeValidation, setTributeValidation] = useState<string | null>(null);
  const activeMember = members.find((member) => member.id === (selectedMemberId ?? members[0]?.id));
  const { data: tributes = [], isLoading: tributesLoading, error: tributesError } = useTributes(activeMember?.id);
  const editingTribute = tributes.find((tribute) => tribute.id === editingTributeId);
  const createTribute = useCreateTribute(activeMember?.id ?? "");
  const updateTribute = useUpdateTribute(activeMember?.id ?? "", editingTributeId ?? "");
  const deleteTribute = useDeleteTribute(activeMember?.id ?? "");
  const isViewer = user?.role === "VIEWER";

  async function submitTribute(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const payload = tributePayload(tributeForm);
    if (!activeMember || !payload) {
      setTributeValidation("Tribute title and story are required.");
      return;
    }
    setTributeValidation(null);
    if (editingTributeId) {
      await updateTribute.mutateAsync(payload);
      setEditingTributeId(null);
    } else {
      await createTribute.mutateAsync(payload);
    }
    setTributeForm({ title: "", story: "" });
  }

  function startEditTribute(tribute: Tribute) {
    if (!canManageTribute(tribute, user)) {
      return;
    }
    setEditingTributeId(tribute.id);
    setTributeValidation(null);
    setTributeForm({ title: tribute.title, story: tribute.story });
  }

  function cancelEditTribute() {
    setEditingTributeId(null);
    setTributeValidation(null);
    setTributeForm({ title: "", story: "" });
  }

  async function handleDeleteTribute(tribute: Tribute) {
    if (!canManageTribute(tribute, user) || !window.confirm(`Delete ${tribute.title}?`)) {
      return;
    }
    await deleteTribute.mutateAsync(tribute.id);
    if (editingTributeId === tribute.id) {
      cancelEditTribute();
    }
  }

  return (
    <div className="grid gap-5 lg:grid-cols-[20rem_minmax(0,1fr)]">
      <Card>
        <CardContent>
          <Badge tone="yellow">Remembered with love</Badge>
          <h2 className="mt-3 text-3xl font-black text-ink">Memorial</h2>
          <div className="mt-5 grid gap-2">
            {isLoading ? <p className="font-semibold text-muted">Loading memorial profiles...</p> : null}
            {members.map((member) => (
              <button
                key={member.id}
                onClick={() => setSelectedMemberId(member.id)}
                className="rounded-lg border border-border-warm bg-white p-4 text-left transition hover:bg-surface-soft"
              >
                <p className="text-lg font-black text-ink">{member.fullName}</p>
                <p className="font-semibold text-muted">{member.roleInFamily ?? "Beloved family member"}</p>
              </button>
            ))}
            {!members.length && !isLoading && !error ? <p className="font-semibold text-muted">No memorial profiles yet.</p> : null}
            {error ? <p className="font-bold text-[#C15A4A]">{error.message}</p> : null}
          </div>
        </CardContent>
      </Card>

      <section className="grid gap-5">
        <Card className="overflow-hidden bg-[linear-gradient(135deg,#FFF8EA,#FFFFFF_55%,#F7EEDC)]">
          <CardContent className="text-center">
            <div className="mx-auto grid h-20 w-20 place-items-center rounded-full bg-warm-yellow/25 text-wood">
              <Flame className="h-10 w-10" />
            </div>
            <h2 className="mt-4 text-4xl font-black text-ink">{activeMember?.fullName ?? "A peaceful place to remember"}</h2>
            <p className="mx-auto mt-3 max-w-2xl text-lg font-semibold leading-8 text-muted">
              Stories, gratitude, and love are kept here quietly, with room for every generation to remember.
            </p>
          </CardContent>
        </Card>

        <div className="grid gap-4 md:grid-cols-2">
          {activeMember && !isViewer ? (
            <Card className="md:col-span-2">
              <CardContent>
                <form className="grid gap-3" onSubmit={submitTribute}>
                  <div className="flex items-center justify-between gap-3">
                    <div>
                      <h3 className="text-xl font-black text-ink">{editingTribute ? "Edit tribute" : "Add tribute"}</h3>
                      {editingTribute ? <FormHint>Editing {editingTribute.title}</FormHint> : null}
                    </div>
                    {editingTribute ? (
                      <Button type="button" variant="ghost" size="icon" aria-label="Cancel tribute edit" onClick={cancelEditTribute}>
                        <X className="h-5 w-5" />
                      </Button>
                    ) : (
                      <Badge tone="sage">{activeMember.fullName}</Badge>
                    )}
                  </div>
                  <Input value={tributeForm.title} onChange={(event) => setTributeForm((form) => ({ ...form, title: event.target.value }))} placeholder="Tribute title" required />
                  <Textarea value={tributeForm.story} onChange={(event) => setTributeForm((form) => ({ ...form, story: event.target.value }))} placeholder="Story" required />
                  <FormError message={tributeValidation ?? createTribute.error?.message ?? updateTribute.error?.message} />
                  <Button type="submit" disabled={createTribute.isPending || updateTribute.isPending}>
                    {editingTribute ? <Pencil className="h-5 w-5" /> : <Plus className="h-5 w-5" />}
                    {createTribute.isPending || updateTribute.isPending ? "Saving..." : editingTribute ? "Save tribute" : "Save tribute"}
                  </Button>
                </form>
              </CardContent>
            </Card>
          ) : null}
          {activeMember && tributesLoading ? <Card className="md:col-span-2"><CardContent><p className="font-bold text-muted">Loading tributes...</p></CardContent></Card> : null}
          {tributesError ? <Card className="md:col-span-2"><CardContent><p className="font-bold text-[#C15A4A]">{tributesError.message}</p></CardContent></Card> : null}
          {deleteTribute.error ? <Card className="md:col-span-2"><CardContent><p className="font-bold text-[#C15A4A]">{deleteTribute.error.message}</p></CardContent></Card> : null}
          {tributes.map((tribute) => (
            <Card key={tribute.id}>
              <CardContent>
                <div className="flex items-start justify-between gap-3">
                  <Heart className="h-6 w-6 text-wood" />
                  {canManageTribute(tribute, user) ? (
                  <div className="flex gap-2">
                    <Button type="button" variant="secondary" size="icon" aria-label={`Edit ${tribute.title}`} onClick={() => startEditTribute(tribute)}>
                      <Pencil className="h-4 w-4" />
                    </Button>
                    <Button type="button" variant="danger" size="icon" aria-label={`Delete ${tribute.title}`} disabled={deleteTribute.isPending} onClick={() => void handleDeleteTribute(tribute)}>
                      <Trash2 className="h-4 w-4" />
                    </Button>
                  </div>
                  ) : null}
                </div>
                <h3 className="mt-3 text-2xl font-black text-ink">{tribute.title}</h3>
                <p className="mt-2 whitespace-pre-wrap text-base font-semibold leading-8 text-muted">{tribute.story}</p>
              </CardContent>
            </Card>
          ))}
          {activeMember && !tributesLoading && !tributesError && !tributes.length ? (
            <Card>
              <CardContent>
                <p className="text-lg font-black text-ink">No tributes yet.</p>
                <p className="mt-2 font-semibold text-muted">
                  {isViewer ? "Tributes for this memorial profile will appear here." : "Add the first tribute from the form above."}
                </p>
              </CardContent>
            </Card>
          ) : null}
        </div>
      </section>
    </div>
  );
}

function tributePayload(form: { title: string; story: string }) {
  if (!form.title.trim() || !form.story.trim()) {
    return null;
  }
  return {
    title: form.title.trim(),
    story: form.story.trim()
  };
}

function canManageTribute(tribute: Tribute, user?: CurrentUser) {
  return user?.role === "ADMIN" || (user?.role === "MEMBER" && Boolean(user.id) && tribute.authorId === user.id);
}
