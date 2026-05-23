import { forwardRef } from "react";
import { cn } from "@/lib/utils/cn";

type ButtonVariant = "primary" | "secondary" | "ghost" | "danger";

type ButtonProps = React.ButtonHTMLAttributes<HTMLButtonElement> & {
  variant?: ButtonVariant;
  size?: "sm" | "md" | "lg" | "icon";
};

const variants: Record<ButtonVariant, string> = {
  primary: "bg-wood text-white shadow-sm hover:bg-wood-dark",
  secondary: "border border-border-warm bg-surface text-ink hover:bg-surface-soft",
  ghost: "text-ink hover:bg-surface-soft",
  danger: "bg-[#C15A4A] text-white hover:bg-[#A7483D]"
};

const sizes = {
  sm: "min-h-10 px-3 text-base",
  md: "min-h-11 px-4 text-base",
  lg: "min-h-12 px-5 text-lg",
  icon: "h-11 w-11 p-0"
};

export const Button = forwardRef<HTMLButtonElement, ButtonProps>(
  ({ className, variant = "primary", size = "md", type = "button", ...props }, ref) => (
    <button
      ref={ref}
      type={type}
      className={cn(
        "inline-flex items-center justify-center gap-2 rounded-lg font-bold transition disabled:pointer-events-none disabled:opacity-50",
        variants[variant],
        sizes[size],
        className
      )}
      {...props}
    />
  )
);

Button.displayName = "Button";
