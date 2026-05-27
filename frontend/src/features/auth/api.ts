import { apiFetch } from "@/lib/api/client";
import type { CurrentUser, LoginOptions, UpdateProfileInput } from "@/features/auth/types";

export function getCurrentUser() {
  return apiFetch<CurrentUser>("/api/auth/me");
}

export function getLoginOptions() {
  return apiFetch<LoginOptions>("/api/auth/login-options");
}

export function logout() {
  return apiFetch<void>("/auth/logout", {
    method: "POST"
  });
}

export function updateCurrentUserProfile(input: UpdateProfileInput) {
  return apiFetch<CurrentUser>("/api/profile/me", {
    method: "PUT",
    body: input
  });
}
