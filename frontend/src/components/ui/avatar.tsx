import { UserRound } from "lucide-react";
import { cn } from "@/lib/utils/cn";

export function Avatar({
  src,
  name,
  className
}: {
  src?: string | null;
  name: string;
  className?: string;
}) {
  return (
    <div className={cn("grid h-12 w-12 shrink-0 place-items-center overflow-hidden rounded-full bg-surface-soft text-wood", className)}>
      {src ? (
        // eslint-disable-next-line @next/next/no-img-element
        <img src={src} alt={name} className="h-full w-full object-cover" />
      ) : (
        <UserRound aria-hidden className="h-6 w-6" />
      )}
    </div>
  );
}
