"use client";

import { useQuery } from "@tanstack/react-query";
import { ApiError } from "@/lib/api/client";
import { queryKeys } from "@/lib/api/queryKeys";
import { getCurrentUser, getLoginOptions } from "@/features/auth/api";

export function useCurrentUser() {
  return useQuery({
    queryKey: queryKeys.authMe,
    queryFn: getCurrentUser,
    retry: false
  });
}

export function useLoginOptions() {
  return useQuery({
    queryKey: ["auth", "login-options"],
    queryFn: getLoginOptions,
    staleTime: 10 * 60_000
  });
}

export function isUnauthorized(error: unknown) {
  return error instanceof ApiError && error.status === 401;
}
