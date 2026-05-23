"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";
import { useQueryClient } from "@tanstack/react-query";
import { Card, CardContent } from "@/components/ui/card";
import { queryKeys } from "@/lib/api/queryKeys";

export default function AuthCallbackPage() {
  const router = useRouter();
  const queryClient = useQueryClient();

  useEffect(() => {
    queryClient.invalidateQueries({ queryKey: queryKeys.authMe });
    const timer = window.setTimeout(() => router.replace("/"), 700);
    return () => window.clearTimeout(timer);
  }, [queryClient, router]);

  return (
    <main className="grid min-h-screen place-items-center px-4">
      <Card className="max-w-md">
        <CardContent className="text-center">
          <p className="text-lg font-black text-ink">Bringing you back home...</p>
          <p className="mt-2 text-base font-semibold text-muted">Your private family space is getting ready.</p>
        </CardContent>
      </Card>
    </main>
  );
}
