"use client";

import { motion, type MotionProps } from "framer-motion";
import { cn } from "@/lib/utils/cn";

export function MotionSection({
  className,
  delay = 0,
  ...props
}: React.HTMLAttributes<HTMLElement> & MotionProps & { delay?: number }) {
  return (
    <motion.section
      initial={{ opacity: 0, y: 18 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.32, delay, ease: "easeOut" }}
      className={cn(className)}
      {...props}
    />
  );
}
