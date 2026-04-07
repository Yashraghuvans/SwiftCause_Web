import * as React from "react";
import { cn } from "./utils";

function Skeleton({ className, ...props }: React.ComponentProps<"div">) {
  return (
    <div
      data-slot="skeleton"
      className={cn(
        "relative overflow-hidden rounded-md bg-neutral-50 dark:bg-slate-800/30",
        className
      )}
      {...props}
    >
      <div
        className="pointer-events-none absolute inset-0 bg-gradient-to-r from-white/30 via-white/95 to-white/30 dark:from-transparent dark:via-white/20 dark:to-transparent animate-shimmer"
        aria-hidden
      />
    </div>
  );
}

export { Skeleton };
