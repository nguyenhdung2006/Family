# AI Handoff

## Current Implementation Status

The repository is a working full-stack HomeTree application with Spring Boot backend, Next.js frontend, PostgreSQL schema migrations, OAuth/JWT auth, role-gated REST APIs, STOMP/SockJS chat broadcasts, Cloudinary upload plumbing, Dockerfiles, production Compose, and nginx routing.

No application code should be rewritten just to "modernize" it. Future sessions should make small, feature-scoped changes that preserve the existing contracts.

## Latest Stable Milestone

Stable baseline: split Spring Boot API plus Next.js frontend with Google OAuth-driven JWT cookie auth and the main product surfaces scaffolded end-to-end:

- family tree
- timeline
- albums/media
- messenger
- memorials
- kitchen recipes
- notifications
- profile/current user shell

The latest stable architecture is captured by the current root `README.md`, `docs/FRONTEND_ARCHITECTURE.md`, and this file. Treat older notes in `docs/HOMETREE_ARCHITECTURE.md` as partially stale around auth hardening.

## Known Bugs And Issues

- WebSocket auth needs browser verification. Backend supports cookie handshake and bearer `CONNECT`; frontend only sends a bearer header when localStorage has `hometree.jwt`, while OAuth uses an HttpOnly cookie.
- Logout is not implemented in the inspected code.
- User role/admin management is not implemented; OAuth-created users default to `MEMBER`.
- Cloudinary upload returns unavailable until Cloudinary env vars are configured.
- Cursor pagination is not implemented in APIs even though supporting indexes exist.
- Group chat read receipts are not implemented; schema has future `chat_message_reads`, but services still use `seenAt`.
- Testcontainers PostgreSQL test is disabled.
- A local `client_secret_*.json` file exists and is gitignored. Do not commit it.

## Next Recommended Tasks

1. Verify local OAuth flow with `SPRING_PROFILES_ACTIVE=oauth`.
2. Verify `/ws` chat subscription after OAuth in both local split-origin and nginx same-origin modes.
3. Add a logout endpoint that clears `HOMETREE_TOKEN`, then wire frontend logout UI.
4. Add admin role management or invitation gating before any real family deployment.
5. Add focused integration tests for auth-protected endpoints and WebSocket subscription authorization.
6. Add cursor pagination for chat, timeline, and notifications.
7. Wire `media_links` and `chat_message_reads` only when product behavior needs them.

## Auth Flow Summary

- Frontend unauthenticated state shows a Google login link to `${NEXT_PUBLIC_API_BASE_URL}/oauth2/authorization/google`.
- Spring Security handles Google OAuth when the `oauth` profile is active.
- `OAuth2LoginSuccessHandler` upserts the user, audits login, issues a custom HS256 JWT, sets it in HttpOnly cookie `HOMETREE_TOKEN`, and redirects to `/auth/callback`.
- Frontend callback invalidates `queryKeys.authMe` and returns home.
- `JwtAuthenticationFilter` authenticates later API requests from either the cookie or `Authorization: Bearer`.
- `GET /api/auth/me` returns current user DTO and drives the app shell.
- Security role policy lives in `SecurityConfig`; keep changes there explicit and reviewed.

## WebSocket Status Summary

- Backend endpoint: `/ws` with SockJS.
- Broker destinations: `/topic`, `/queue`.
- Application prefix: `/app`.
- Current product use: chat room updates on `/topic/rooms/{roomId}`.
- `ChatService.sendMessage` persists the message and broadcasts the DTO.
- Backend authenticates the handshake from `HOMETREE_TOKEN` cookie or STOMP `CONNECT` from bearer token.
- `VIEWER` role cannot subscribe to room topics.
- Frontend subscribes through `useRoomSocket(roomId)` and merges incoming messages into React Query cache, filtering optimistic duplicates.

## Important Implementation Decisions

- PostgreSQL remains canonical; family graph is relational, not a graph database.
- Flyway owns schema. Do not use Hibernate schema generation beyond validation.
- API responses use the shared `ApiResponse<T>` envelope.
- DTO records are used at controller boundaries.
- Services own transactions and audit calls.
- Cloudinary stores media bytes; database stores URLs/provider metadata.
- React Query owns server state; Zustand owns UI state.
- Next.js runs as standalone output in production.
- nginx is the production edge router for API, OAuth, WebSocket, and frontend traffic.

## Rules For Future AI Sessions

- Inspect before editing. This repo has cross-cutting auth, WebSocket, and deployment assumptions.
- Keep edits narrow and feature-scoped.
- Do not modify architecture unless explicitly asked.
- Preserve Java 25, Spring Boot, Maven wrapper, PostgreSQL, Flyway, Next.js, React Query, and Zustand choices.
- Use existing package/module patterns instead of introducing parallel abstractions.
- Keep backend API envelopes and frontend `apiFetch` behavior aligned.
- Add migrations for schema changes; never rely on `ddl-auto` to mutate production schema.
- Keep secrets out of Git. Never commit `.env`, credential JSON, keys, generated builds, or local caches.
- Prefer tests around security and shared contracts when touching auth, WebSocket, migrations, or API envelopes.
- On Windows frontend builds, use Node 22 as documented; Node 24 has been observed to hang.

## Dangerous Areas To Avoid Rewriting

- `SecurityConfig.java`: route and role policy.
- `JwtAuthenticationFilter.java`, `JwtService.java`, and `OAuth2LoginSuccessHandler.java`: active auth/session contract.
- `WebSocketConfig.java`: handshake auth and subscription authorization.
- `src/main/resources/db/migration`: append migrations only; do not rewrite applied migrations.
- `frontend/src/lib/api/client.ts`: shared fetch/envelope/auth behavior.
- `frontend/src/lib/websocket/*`: STOMP/SockJS integration.
- `docker-compose.prod.yaml` and `nginx/hometree.conf`: production service routing.
- `frontend/package-lock.json`: exact dependency graph; do not churn without a dependency task.
- `media` storage abstractions: external upload behavior and limits are environment-sensitive.
