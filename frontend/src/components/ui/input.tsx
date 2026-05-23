import { forwardRef } from "react";
import { cn } from "@/lib/utils/cn";

export const Input = forwardRef<HTMLInputElement, React.InputHTMLAttributes<HTMLInputElement>>(
  ({ className, ...props }, ref) => (
    <input
      ref={ref}
      className={cn(
        "min-h-12 w-full rounded-lg border border-border-warm bg-white px-4 text-base text-ink placeholder:text-muted/70 shadow-sm transition focus:border-warm-yellow",
        className
      )}
      {...props}
    />
  )
);

Input.displayName = "Input";

export const Textarea = forwardRef<HTMLTextAreaElement, React.TextareaHTMLAttributes<HTMLTextAreaElement>>(
  ({ className, ...props }, ref) => (
    <textarea
      ref={ref}
      className={cn(
        "min-h-32 w-full resize-none rounded-lg border border-border-warm bg-white px-4 py-3 text-base text-ink placeholder:text-muted/70 shadow-sm transition focus:border-warm-yellow",
        className
      )}
      {...props}
    />
  )
);

Textarea.displayName = "Textarea";
