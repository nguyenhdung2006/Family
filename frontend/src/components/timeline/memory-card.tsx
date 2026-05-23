"use client";

import { Heart, MapPin, MessageCircle, UsersRound } from "lucide-react";
import { Avatar } from "@/components/ui/avatar";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import type { MemoryPost } from "@/features/timeline/types";
import { formatFamilyDate } from "@/lib/utils/date";

export function MemoryCard({ post }: { post: MemoryPost }) {
  return (
    <Card>
      <CardContent>
        <div className="flex gap-3">
          <Avatar name="Family memory" className="h-12 w-12" />
          <div className="min-w-0 flex-1">
            <div className="flex flex-wrap items-center gap-2">
              <p className="text-lg font-black text-ink">Family Memory</p>
              <Badge tone="yellow">{post.eventType.replace("_", " ")}</Badge>
            </div>
            <p className="font-bold text-muted">{formatFamilyDate(post.occurredAt)}</p>
          </div>
        </div>

        <p className="mt-4 whitespace-pre-wrap text-lg font-semibold leading-8 text-ink">{post.text}</p>

        <div className="mt-4 flex flex-wrap gap-3 text-sm font-bold text-muted">
          {post.locationName ? <span className="inline-flex items-center gap-1"><MapPin className="h-4 w-4" />{post.locationName}</span> : null}
          {post.taggedMemberIds.length ? <span className="inline-flex items-center gap-1"><UsersRound className="h-4 w-4" />{post.taggedMemberIds.length} tagged</span> : null}
        </div>

        <div className="mt-5 flex gap-2 border-t border-border-warm pt-4">
          <Button variant="ghost" className="flex-1">
            <Heart className="h-5 w-5" /> Love
          </Button>
          <Button variant="ghost" className="flex-1">
            <MessageCircle className="h-5 w-5" /> Comment
          </Button>
        </div>
      </CardContent>
    </Card>
  );
}
