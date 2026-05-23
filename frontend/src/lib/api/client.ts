import { getAccessToken } from "@/lib/auth/token";
import type { ApiEnvelope, PageParams } from "@/lib/api/types";

const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080";

export class ApiError extends Error {
  constructor(
    message: string,
    public readonly status: number,
    public readonly path?: string
  ) {
    super(message);
  }
}

type RequestOptions = Omit<RequestInit, "body"> & {
  body?: unknown;
  params?: Record<string, string | number | boolean | undefined | null> & PageParams;
};

function buildUrl(path: string, params?: RequestOptions["params"]) {
  const baseUrl = API_BASE_URL.trim();
  const url = new URL(
    path.startsWith("http")
      ? path
      : baseUrl
        ? `${baseUrl}${path}`
        : path,
    typeof window === "undefined" ? "http://localhost:3000" : window.location.origin
  );
  Object.entries(params ?? {}).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== "") {
      url.searchParams.set(key, String(value));
    }
  });
  return url.toString();
}

export async function apiFetch<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const { body, params, ...init } = options;
  const token = getAccessToken();
  const headers = new Headers(options.headers);
  headers.set("Accept", "application/json");

  const isFormData = body instanceof FormData;
  if (body !== undefined && !isFormData) {
    headers.set("Content-Type", "application/json");
  }
  if (token) {
    headers.set("Authorization", `Bearer ${token}`);
  }

  const response = await fetch(buildUrl(path, params), {
    ...init,
    headers,
    credentials: "include",
    body: isFormData ? body : body === undefined ? undefined : JSON.stringify(body)
  });

  if (!response.ok) {
    let message = `Request failed with ${response.status}`;
    try {
      const body = (await response.json()) as { message?: string; path?: string };
      message = body.message ?? message;
      throw new ApiError(message, response.status, body.path);
    } catch (error) {
      if (error instanceof ApiError) {
        throw error;
      }
      throw new ApiError(message, response.status, path);
    }
  }

  if (response.status === 204) {
    return undefined as T;
  }

  const envelope = (await response.json()) as ApiEnvelope<T>;
  return envelope.data;
}
