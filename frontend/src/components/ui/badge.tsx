import { cn } from "@/lib/utils/cn";

const toneClasses = {
  yellow: "bg-warm-yellow/20 text-wood",
  sage: "bg-sage/15 text-[#406344]",
  blue: "bg-muted-blue/15 text-[#355C78]",
  cream: "bg-surface-soft text-muted",
  wood: "bg-wood text-white"
};

export function Badge({
  tone = "cream",
  className,
  ...props
}: React.HTMLAttributes<HTMLSpanElement> & { tone?: keyof typeof toneClasses }) {
  return (
    <span
      className={cn("inline-flex min-h-7 items-center rounded-full px-3 text-sm font-bold", toneClasses[tone], className)}
      {...props}
    />
  );
}
