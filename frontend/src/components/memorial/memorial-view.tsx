"use client";

import { FormEvent, useState } from "react";
import { Flame, Heart, Plus } from "lucide-react";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Input, Textarea } from "@/components/ui/input";
import { useCreateTribute, useMemorialMembers, useTributes } from "@/features/memorial/hooks";

export function MemorialView() {
  const { data: members = [], isLoading, error } = useMemorialMembers();
  const [selectedMemberId, setSelectedMemberId] = useState<string | null>(null);
  const [tributeForm, setTributeForm] = useState({ title: "", story: "" });
  const activeMember = members.find((member) => member.id === (selectedMemberId ?? members[0]?.id));
  const { data: tributes = [], error: tributesError } = useTributes(activeMember?.id);
  const createTribute = useCreateTribute(activeMember?.id ?? "");

  async function submitTribute(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!activeMember || !tributeForm.title.trim() || !tributeForm.story.trim()) {
      return;
    }
    await createTribute.mutateAsync({
      title: tributeForm.title.trim(),
      story: tributeForm.story.trim()
    });
    setTributeForm({ title: "", story: "" });
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
            {!members.length ? <p className="font-semibold text-muted">No memorial profiles yet.</p> : null}
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
          {activeMember ? (
            <Card className="md:col-span-2">
              <CardContent>
                <form className="grid gap-3" onSubmit={submitTribute}>
                  <div className="flex items-center justify-between gap-3">
                    <h3 className="text-xl font-black text-ink">Add tribute</h3>
                    <Badge tone="sage">{activeMember.fullName}</Badge>
                  </div>
                  <Input value={tributeForm.title} onChange={(event) => setTributeForm((form) => ({ ...form, title: event.target.value }))} placeholder="Tribute title" required />
                  <Textarea value={tributeForm.story} onChange={(event) => setTributeForm((form) => ({ ...form, story: event.target.value }))} placeholder="Story" required />
                  {createTribute.error ? <p className="font-bold text-[#C15A4A]">{createTribute.error.message}</p> : null}
                  <Button type="submit" disabled={createTribute.isPending}>
                    <Plus className="h-5 w-5" /> {createTribute.isPending ? "Saving..." : "Save tribute"}
                  </Button>
                </form>
              </CardContent>
            </Card>
          ) : null}
          {tributesError ? <Card className="md:col-span-2"><CardContent><p className="font-bold text-[#C15A4A]">{tributesError.message}</p></CardContent></Card> : null}
          {tributes.map((tribute) => (
            <Card key={tribute.id}>
              <CardContent>
                <Heart className="h-6 w-6 text-wood" />
                <h3 className="mt-3 text-2xl font-black text-ink">{tribute.title}</h3>
                <p className="mt-2 whitespace-pre-wrap text-base font-semibold leading-8 text-muted">{tribute.story}</p>
              </CardContent>
            </Card>
          ))}
          {!tributes.length ? (
            <Card><CardContent><p className="text-lg font-bold text-muted">No tributes yet. The first tribute will appear here.</p></CardContent></Card>
          ) : null}
        </div>
      </section>
    </div>
  );
}
