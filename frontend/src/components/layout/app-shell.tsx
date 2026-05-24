"use client";

import Link from "next/link";
import type { Route } from "next";
import { usePathname } from "next/navigation";
import {
  Bell,
  BookOpen,
  ChefHat,
  Heart,
  Home,
  Image,
  LogOut,
  Menu,
  MessageCircle,
  Sprout,
  UserRound,
  X,
  type LucideIcon
} from "lucide-react";
import { AnimatePresence, motion } from "framer-motion";
import { Avatar } from "@/components/ui/avatar";
import { Button } from "@/components/ui/button";
import { LoginScreen } from "@/components/layout/login-screen";
import { isUnauthorized, useCurrentUser, useLogout } from "@/features/auth/hooks";
import { useAppStore } from "@/stores/app-store";
import { cn } from "@/lib/utils/cn";

type NavigationItem = {
  href: Route;
  label: string;
  icon: LucideIcon;
};

const navItems = [
  { href: "/", label: "Home", icon: Home },
  { href: "/family-tree", label: "Family Tree", icon: Sprout },
  { href: "/timeline", label: "Timeline", icon: BookOpen },
  { href: "/albums", label: "Albums", icon: Image },
  { href: "/messenger", label: "Messenger", icon: MessageCircle },
  { href: "/memorial", label: "Memorial", icon: Heart },
  { href: "/kitchen", label: "Kitchen", icon: ChefHat },
  { href: "/notifications", label: "Alerts", icon: Bell },
  { href: "/profile", label: "Profile", icon: UserRound }
] satisfies NavigationItem[];

export function AppShell({ title, children }: { title: string; children: React.ReactNode }) {
  const pathname = usePathname();
  const { data: user, error, isLoading } = useCurrentUser();
  const logout = useLogout();
  const { sidebarOpen, setSidebarOpen } = useAppStore();

  if (!isLoading && isUnauthorized(error)) {
    return <LoginScreen />;
  }

  return (
    <div className="min-h-screen pb-24 lg:pb-0">
      <aside className="fixed inset-y-0 left-0 z-40 hidden w-72 border-r border-border-warm bg-surface/90 p-4 shadow-sm backdrop-blur-xl lg:block">
        <Brand />
        <nav className="mt-8 grid gap-2">
          {navItems.map((item) => (
            <NavItem key={item.href} item={item} active={pathname === item.href} />
          ))}
        </nav>
      </aside>

      <header className="sticky top-0 z-30 border-b border-border-warm/70 bg-background/85 backdrop-blur-xl lg:ml-72">
        <div className="flex min-h-20 items-center justify-between gap-3 px-4 sm:px-6 lg:px-8">
          <div className="flex items-center gap-3">
            <Button
              variant="secondary"
              size="icon"
              className="lg:hidden"
              aria-label="Open navigation"
              onClick={() => setSidebarOpen(true)}
            >
              <Menu className="h-5 w-5" />
            </Button>
            <div>
              <p className="text-sm font-bold text-muted">HomeTree</p>
              <h1 className="text-2xl font-black tracking-normal text-ink sm:text-3xl">{title}</h1>
            </div>
          </div>
          <div className="flex items-center gap-2">
            <Link href="/profile" className="flex items-center gap-3 rounded-lg p-2 transition hover:bg-surface-soft">
              <div className="hidden text-right sm:block">
                <p className="font-extrabold text-ink">{user?.name ?? "Family Member"}</p>
                <p className="text-sm font-semibold text-muted">{user?.email ?? "Private family space"}</p>
              </div>
              <Avatar name={user?.name ?? "Family Member"} src={user?.avatarUrl} />
            </Link>
            <Button
              variant="ghost"
              size="icon"
              aria-label="Log out"
              title="Log out"
              disabled={logout.isPending}
              onClick={() => logout.mutate()}
            >
              <LogOut className="h-5 w-5" />
            </Button>
          </div>
        </div>
      </header>

      <main className="lg:ml-72">
        <div className="mx-auto w-full max-w-7xl px-4 py-5 sm:px-6 lg:px-8">{children}</div>
      </main>

      <MobileNav pathname={pathname} />

      <AnimatePresence>
        {sidebarOpen ? (
          <motion.div className="fixed inset-0 z-50 lg:hidden" initial={{ opacity: 0 }} animate={{ opacity: 1 }} exit={{ opacity: 0 }}>
            <button className="absolute inset-0 bg-ink/35" aria-label="Close navigation" onClick={() => setSidebarOpen(false)} />
            <motion.aside
              initial={{ x: -320 }}
              animate={{ x: 0 }}
              exit={{ x: -320 }}
              transition={{ type: "spring", damping: 30, stiffness: 300 }}
              className="relative h-full w-[min(22rem,86vw)] border-r border-border-warm bg-surface p-4 shadow-xl"
            >
              <div className="flex items-center justify-between">
                <Brand />
                <Button variant="ghost" size="icon" aria-label="Close navigation" onClick={() => setSidebarOpen(false)}>
                  <X className="h-5 w-5" />
                </Button>
              </div>
              <nav className="mt-8 grid gap-2">
                {navItems.map((item) => (
                  <NavItem key={item.href} item={item} active={pathname === item.href} onClick={() => setSidebarOpen(false)} />
                ))}
              </nav>
            </motion.aside>
          </motion.div>
        ) : null}
      </AnimatePresence>
    </div>
  );
}

function Brand() {
  return (
    <Link href="/" className="flex items-center gap-3">
      <span className="grid h-12 w-12 place-items-center rounded-lg bg-wood text-xl font-black text-white">HT</span>
      <span>
        <span className="block text-xl font-black text-ink">HomeTree</span>
        <span className="block text-sm font-bold text-muted">Digital Family Hub</span>
      </span>
    </Link>
  );
}

function NavItem({
  item,
  active,
  onClick
}: {
  item: (typeof navItems)[number];
  active: boolean;
  onClick?: () => void;
}) {
  const Icon = item.icon;
  return (
    <Link
      href={item.href}
      onClick={onClick}
      className={cn(
        "flex min-h-12 items-center gap-3 rounded-lg px-3 text-base font-extrabold transition",
        active ? "bg-warm-yellow/25 text-wood" : "text-muted hover:bg-surface-soft hover:text-ink"
      )}
    >
      <Icon className="h-5 w-5" aria-hidden />
      {item.label}
    </Link>
  );
}

function MobileNav({ pathname }: { pathname: string }) {
  const items = navItems.slice(0, 5);
  return (
    <nav className="fixed inset-x-0 bottom-0 z-40 border-t border-border-warm bg-surface/95 px-2 py-2 shadow-[0_-10px_30px_rgba(80,55,33,0.1)] backdrop-blur-xl lg:hidden">
      <div className="grid grid-cols-5 gap-1">
        {items.map((item) => {
          const Icon = item.icon;
          const active = pathname === item.href;
          return (
            <Link
              key={item.href}
              href={item.href}
              className={cn(
                "flex min-h-14 flex-col items-center justify-center gap-1 rounded-lg text-xs font-black transition",
                active ? "bg-warm-yellow/25 text-wood" : "text-muted"
              )}
            >
              <Icon className="h-5 w-5" aria-hidden />
              {item.label}
            </Link>
          );
        })}
      </div>
    </nav>
  );
}
