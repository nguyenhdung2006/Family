"use client";

import Link from "next/link";
import type { LucideIcon } from "lucide-react";
import { Bell, BookOpen, CalendarHeart, ChefHat, Heart, Home, Image, MessageCircle, Sprout } from "lucide-react";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent } from "@/components/ui/card";
import { MotionSection } from "@/components/ui/motion-section";
import { Skeleton } from "@/components/ui/skeleton";
import { useAlbums } from "@/features/albums/hooks";
import { useChatRooms } from "@/features/chat/hooks";
import { useCurrentUser } from "@/features/auth/hooks";
import { useFamilyMembers } from "@/features/family/hooks";
import { useTimelinePosts } from "@/features/timeline/hooks";
import { formatFamilyDate } from "@/lib/utils/date";

const quickLinks = [
  { href: "/family-tree", label: "Family Tree", icon: Sprout },
  { href: "/timeline", label: "Memories", icon: BookOpen },
  { href: "/albums", label: "Albums", icon: Image },
  { href: "/messenger", label: "Messages", icon: MessageCircle },
  { href: "/memorial", label: "Memorial", icon: Heart },
  { href: "/kitchen", label: "Kitchen", icon: ChefHat }
] as const;

export function HomeDashboard() {
  const { data: user } = useCurrentUser();
  const { data: posts = [], isLoading: memoriesLoading } = useTimelinePosts({ page: 0, size: 3 });
  const { data: members = [], isLoading: membersLoading } = useFamilyMembers({ page: 0, size: 8 });
  const { data: albums = [], isLoading: albumsLoading } = useAlbums();
  const { data: rooms = [], isLoading: roomsLoading } = useChatRooms();

  const birthdays = members.filter((member) => member.birthDate).slice(0, 3);
  const firstName = user?.name?.split(" ")[0] ?? "family";

  return (
    <div className="grid gap-5 lg:grid-cols-[minmax(0,1fr)_22rem]">
      <div className="grid gap-5">
        <MotionSection>
          <section className="overflow-hidden rounded-lg border border-border-warm bg-[linear-gradient(135deg,#FFF8EA_0%,#FFFFFF_50%,#F7EEDC_100%)] p-5 shadow-[0_24px_70px_rgba(80,55,33,0.1)] sm:p-8">
            <div className="flex flex-col gap-7 md:flex-row md:items-center md:justify-between">
              <div className="max-w-2xl">
                <Badge tone="yellow">Private family home</Badge>
                <h2 className="mt-4 text-4xl font-black leading-tight text-ink sm:text-5xl">
                  Welcome back, {firstName}.
                </h2>
                <p className="mt-4 text-lg font-semibold leading-8 text-muted">
                  A warm place for stories, old photos, daily messages, and the people who make them matter.
                </p>
                <div className="mt-6 flex flex-wrap gap-3">
                  <Link
                    href="/timeline"
                    className="inline-flex min-h-12 items-center justify-center rounded-lg bg-wood px-5 text-lg font-bold text-white shadow-sm transition hover:bg-wood-dark"
                  >
                    Share a memory
                  </Link>
                  <Link
                    href="/family-tree"
                    className="inline-flex min-h-12 items-center justify-center rounded-lg border border-border-warm bg-surface px-5 text-lg font-bold text-ink transition hover:bg-surface-soft"
                  >
                    Visit the tree
                  </Link>
                </div>
              </div>
              <div className="grid min-h-56 min-w-56 place-items-center rounded-lg border border-border-warm bg-white/75 p-5 shadow-inner">
                <div className="text-center">
                  <div className="mx-auto grid h-24 w-24 place-items-center rounded-full bg-warm-yellow/25 text-wood">
                    <Home className="h-12 w-12" aria-hidden />
                  </div>
                  <p className="mt-4 text-xl font-black text-wood">Every branch has a story.</p>
                </div>
              </div>
            </div>
          </section>
        </MotionSection>

        <MotionSection delay={0.05}>
          <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-3">
            {quickLinks.map((link) => {
              const Icon = link.icon;
              return (
                <Link key={link.href} href={link.href} className="group">
                  <Card className="h-full transition group-hover:-translate-y-1 group-hover:shadow-[0_24px_55px_rgba(80,55,33,0.12)]">
                    <CardContent className="flex items-center gap-4">
                      <span className="grid h-12 w-12 place-items-center rounded-lg bg-surface-soft text-wood">
                        <Icon className="h-6 w-6" aria-hidden />
                      </span>
                      <div>
                        <p className="text-lg font-black text-ink">{link.label}</p>
                        <p className="text-sm font-bold text-muted">Open</p>
                      </div>
                    </CardContent>
                  </Card>
                </Link>
              );
            })}
          </div>
        </MotionSection>

        <MotionSection delay={0.1}>
          <Card>
            <CardContent>
              <div className="mb-4 flex items-center justify-between">
                <h3 className="text-2xl font-black text-ink">Recent memories</h3>
                <Link href="/timeline" className="text-base font-black text-wood">
                  View all
                </Link>
              </div>
              <div className="grid gap-3">
                {memoriesLoading ? (
                  <MemorySkeletons />
                ) : posts.length ? (
                  posts.map((post) => (
                    <article key={post.id} className="rounded-lg bg-surface-soft p-4">
                      <div className="flex flex-wrap items-center gap-2">
                        <Badge tone="sage">{post.eventType.replace("_", " ")}</Badge>
                        <span className="text-sm font-bold text-muted">{formatFamilyDate(post.occurredAt)}</span>
                      </div>
                      <p className="mt-3 text-lg font-bold leading-8 text-ink">{post.text}</p>
                    </article>
                  ))
                ) : (
                  <EmptyState icon={BookOpen} title="No memories yet" text="The first shared story will appear here." />
                )}
              </div>
            </CardContent>
          </Card>
        </MotionSection>
      </div>

      <aside className="grid content-start gap-5">
        <Card>
          <CardContent>
            <h3 className="flex items-center gap-2 text-xl font-black text-ink">
              <CalendarHeart className="h-5 w-5 text-wood" aria-hidden /> Upcoming birthdays
            </h3>
            <div className="mt-4 grid gap-3">
              {membersLoading ? (
                <SidebarSkeletons />
              ) : birthdays.length ? (
                birthdays.map((member) => (
                  <div key={member.id} className="rounded-lg border border-border-warm bg-white p-3">
                    <p className="font-black text-ink">{member.fullName}</p>
                    <p className="font-semibold text-muted">{formatFamilyDate(member.birthDate)}</p>
                  </div>
                ))
              ) : (
                <p className="font-semibold text-muted">Add birthdays to family profiles to see them here.</p>
              )}
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardContent>
            <h3 className="text-xl font-black text-ink">Family slideshow</h3>
            {albumsLoading ? (
              <Skeleton className="mt-4 h-28" />
            ) : (
              <div className="mt-4 overflow-hidden rounded-lg bg-surface-soft p-4">
                <div className="rounded-lg border border-white/70 bg-white/70 p-4">
                  <p className="text-3xl font-black text-wood">{albums.length}</p>
                  <p className="font-bold text-muted">albums ready for memories</p>
                  <p className="mt-3 line-clamp-2 text-base font-extrabold text-ink">
                    {albums[0]?.title ?? "Add albums to begin a gentle family slideshow."}
                  </p>
                </div>
              </div>
            )}
          </CardContent>
        </Card>

        <Card>
          <CardContent>
            <h3 className="flex items-center gap-2 text-xl font-black text-ink">
              <Bell className="h-5 w-5 text-wood" aria-hidden /> Recent messages
            </h3>
            <div className="mt-4 grid gap-3">
              {roomsLoading ? (
                <SidebarSkeletons />
              ) : (
                rooms.slice(0, 3).map((room) => (
                  <Link key={room.id} href="/messenger" className="rounded-lg border border-border-warm bg-white p-3 transition hover:bg-surface-soft">
                    <p className="font-black text-ink">{room.name}</p>
                    <p className="font-semibold text-muted">{room.type.toLowerCase()} room</p>
                  </Link>
                ))
              )}
              {!roomsLoading && !rooms.length ? <p className="font-semibold text-muted">No message rooms yet.</p> : null}
            </div>
          </CardContent>
        </Card>
      </aside>
    </div>
  );
}

function MemorySkeletons() {
  return (
    <>
      <Skeleton className="h-28" />
      <Skeleton className="h-24" />
      <Skeleton className="h-24" />
    </>
  );
}

function SidebarSkeletons() {
  return (
    <>
      <Skeleton className="h-16" />
      <Skeleton className="h-16" />
      <Skeleton className="h-16" />
    </>
  );
}

function EmptyState({ icon: Icon, title, text }: { icon: LucideIcon; title: string; text: string }) {
  return (
    <div className="grid place-items-center rounded-lg border border-dashed border-border-warm bg-white p-8 text-center">
      <Icon className="h-8 w-8 text-wood" aria-hidden />
      <p className="mt-3 text-lg font-black text-ink">{title}</p>
      <p className="font-semibold text-muted">{text}</p>
    </div>
  );
}
