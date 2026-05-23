"use client";

import { X } from "lucide-react";
import { AnimatePresence, motion } from "framer-motion";
import { Avatar } from "@/components/ui/avatar";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import type { FamilyMember } from "@/features/family/types";
import { formatFamilyDate } from "@/lib/utils/date";

export function MemberDrawer({
  member,
  onClose
}: {
  member?: FamilyMember | null;
  onClose: () => void;
}) {
  return (
    <AnimatePresence>
      {member ? (
        <motion.aside
          initial={{ x: 420, opacity: 0 }}
          animate={{ x: 0, opacity: 1 }}
          exit={{ x: 420, opacity: 0 }}
          transition={{ type: "spring", damping: 32, stiffness: 280 }}
          className="absolute inset-y-0 right-0 z-20 w-full max-w-md border-l border-border-warm bg-surface p-4 shadow-xl sm:p-5"
        >
          <div className="flex items-start justify-between gap-3">
            <div className="flex items-center gap-4">
              <Avatar name={member.fullName} src={member.avatarUrl} className="h-20 w-20" />
              <div>
                <h2 className="text-2xl font-black text-ink">{member.fullName}</h2>
                <p className="font-bold text-muted">{member.roleInFamily ?? "Family member"}</p>
              </div>
            </div>
            <Button variant="ghost" size="icon" aria-label="Close member details" onClick={onClose}>
              <X className="h-5 w-5" />
            </Button>
          </div>

          <div className="mt-5 flex flex-wrap gap-2">
            <Badge tone={member.branch === "PATERNAL" ? "sage" : "blue"}>{member.branch.toLowerCase()}</Badge>
            <Badge tone="yellow">Generation {member.generationLevel}</Badge>
            {member.deceased ? <Badge tone="cream">Remembered</Badge> : null}
          </div>

          <div className="mt-5 grid gap-4">
            <Card>
              <CardContent>
                <p className="text-sm font-black uppercase tracking-wide text-muted">Life</p>
                <p className="mt-2 text-lg font-bold text-ink">
                  {formatFamilyDate(member.birthDate)} {member.deathDate ? `- ${formatFamilyDate(member.deathDate)}` : ""}
                </p>
              </CardContent>
            </Card>
            <Card>
              <CardContent>
                <p className="text-sm font-black uppercase tracking-wide text-muted">Biography</p>
                <p className="mt-2 text-base font-semibold leading-7 text-ink">
                  {member.biography ?? "No biography has been added yet. This space is ready for stories, photos, and memories."}
                </p>
              </CardContent>
            </Card>
          </div>
        </motion.aside>
      ) : null}
    </AnimatePresence>
  );
}
