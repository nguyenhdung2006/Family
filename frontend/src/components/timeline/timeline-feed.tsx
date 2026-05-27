"use client";

import { useMemo, useState } from "react";
import { Filter } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { MemoryCard } from "@/components/timeline/memory-card";
import { MemoryComposer } from "@/components/timeline/memory-composer";
import { useCurrentUser } from "@/features/auth/hooks";
import { useTimelinePosts } from "@/features/timeline/hooks";
import type { EventType } from "@/features/timeline/types";

const events: Array<EventType | ""> = ["", "FAMILY_GATHERING", "BIRTHDAY", "WEDDING", "TET", "TRAVEL", "MEMORIAL", "EVERYDAY", "OTHER"];

export function TimelineFeed() {
  const [year, setYear] = useState("");
  const [eventType, setEventType] = useState<EventType | "">("");
  const filters = useMemo(
    () => ({ page: 0, size: 30, year: year ? Number(year) : undefined, eventType: eventType || undefined }),
    [eventType, year]
  );
  const { data: user } = useCurrentUser();
  const { data: posts = [], isLoading, error, refetch } = useTimelinePosts(filters);
  const isViewer = user?.role === "VIEWER";
  const hasFilters = Boolean(year || eventType);

  return (
    <div className="grid gap-5 lg:grid-cols-[minmax(0,1fr)_20rem]">
      <div className="grid gap-5">
        {!isViewer ? <MemoryComposer /> : null}
        {isLoading ? (
          <Card><CardContent><p className="font-bold text-muted">Loading memories...</p></CardContent></Card>
        ) : error ? (
          <Card>
            <CardContent className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
              <p className="font-bold text-[#C15A4A]">We could not load memories. {error.message}</p>
              <Button variant="secondary" onClick={() => void refetch()}>Try again</Button>
            </CardContent>
          </Card>
        ) : posts.length ? (
          posts.map((post) => <MemoryCard key={post.id} post={post} />)
        ) : (
          <Card>
            <CardContent>
              <p className="text-lg font-black text-ink">{hasFilters ? "No memories match these filters yet." : "No memories yet."}</p>
              <p className="mt-2 font-semibold text-muted">
                {isViewer ? "Shared family memories will appear here when members add them." : "Share the first family memory from the composer above."}
              </p>
            </CardContent>
          </Card>
        )}
      </div>
      <aside className="lg:sticky lg:top-24 lg:self-start">
        <Card>
          <CardContent>
            <h2 className="flex items-center gap-2 text-xl font-black text-ink"><Filter className="h-5 w-5" /> Filters</h2>
            <div className="mt-4 grid gap-3">
              <Input placeholder="Year" inputMode="numeric" value={year} onChange={(event) => setYear(event.target.value)} />
              <select
                className="min-h-12 rounded-lg border border-border-warm bg-white px-3 text-base font-bold text-ink"
                value={eventType}
                onChange={(event) => setEventType(event.target.value as EventType | "")}
              >
                {events.map((event) => (
                  <option key={event || "all"} value={event}>{event ? event.replace("_", " ") : "All events"}</option>
                ))}
              </select>
            </div>
          </CardContent>
        </Card>
      </aside>
    </div>
  );
}
