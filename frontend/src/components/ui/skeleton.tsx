import { cn } from "@/lib/utils/cn";

export function Skeleton({ className, ...props }: React.HTMLAttributes<HTMLDivElement>) {
  return (
    <div
      className={cn("animate-pulse rounded-lg bg-gradient-to-r from-surface-soft via-white to-surface-soft", className)}
      {...props}
    />
  );
}
