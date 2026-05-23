"use client";

import { useState } from "react";
import { Flame, Heart } from "lucide-react";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent } from "@/components/ui/card";
import { useMemorialMembers, useTributes } from "@/features/memorial/hooks";

export function MemorialView() {
  const { data: members = [] } = useMemorialMembers();
  const [selectedMemberId, setSelectedMemberId] = useState<string | null>(null);
  const activeMember = members.find((member) => member.id === (selectedMemberId ?? members[0]?.id));
  const { data: tributes = [] } = useTributes(activeMember?.id);

  return (
    <div className="grid gap-5 lg:grid-cols-[20rem_minmax(0,1fr)]">
      <Card>
        <CardContent>
          <Badge tone="yellow">Remembered with love</Badge>
          <h2 className="mt-3 text-3xl font-black text-ink">Memorial</h2>
          <div className="mt-5 grid gap-2">
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
