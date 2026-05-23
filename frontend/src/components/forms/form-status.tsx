"use client";

import { cn } from "@/lib/utils/cn";

export function FormError({ message, className }: { message?: string | null; className?: string }) {
  if (!message) {
    return null;
  }
  return <p className={cn("font-bold text-[#C15A4A]", className)}>{message}</p>;
}

export function FormHint({ children, className }: { children: React.ReactNode; className?: string }) {
  return <p className={cn("text-sm font-bold text-muted", className)}>{children}</p>;
}
