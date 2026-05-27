"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { ApiError } from "@/lib/api/client";
import { queryKeys } from "@/lib/api/queryKeys";
import { getCurrentUser, getLoginOptions, logout, updateCurrentUserProfile } from "@/features/auth/api";
import type { CurrentUser } from "@/features/auth/types";
import { clearAccessToken } from "@/lib/auth/token";
import { disconnectAllStompClients } from "@/lib/websocket/stomp-client";

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

export function useLogout() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: logout,
    onSettled: async () => {
      clearAccessToken();
      await disconnectAllStompClients();
      queryClient.clear();
      window.location.assign("/login");
    }
  });
}

export function useUpdateCurrentUserProfile() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: updateCurrentUserProfile,
    onSuccess: (user: CurrentUser) => {
      queryClient.setQueryData(queryKeys.authMe, user);
    }
  });
}

export function isUnauthorized(error: unknown) {
  return error instanceof ApiError && error.status === 401;
}
