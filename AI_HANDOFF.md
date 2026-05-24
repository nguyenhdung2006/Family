# AI Handoff

This file is the first-read runbook for future AI sessions. Keep it short enough to scan, but precise enough to prevent unsafe rewrites.

Last reviewed: 2026-05-24.

## Mission

HomeTree is a working split full-stack application for a private family hub: family tree, timeline memories, albums/media, messenger, memorial tributes, kitchen recipes, notifications, and profile/current-user shell.

Do not modernize or redesign casually. Make small, feature-scoped changes that preserve the current contracts unless the user explicitly asks for an architecture change.

## Current Stable Baseline

- Backend: Spring Boot 3.5.14, Java 25, Maven, PostgreSQL, Flyway, Spring Security, optional Redis, STOMP/SockJS.
- Frontend: Next.js App Router, React 19, TypeScript, React Query, Zustand, Tailwind CSS, React Flow, STOMP/SockJS.
- Auth: Google OAuth profile, custom JWT, HttpOnly `HOMETREE_TOKEN` cookie, bearer-token fallback.
- Deployment: separate API and web containers behind nginx; PostgreSQL and Redis in Compose.
- API contract: backend responses use the shared envelope; frontend uses `apiFetch<T>()` to unwrap it.

## Current Worktree Awareness

As of the latest inspection, these files had local modifications before this document rewrite:

- `PROJECT_CONTEXT.md`
- `AI_HANDOFF.md`
- `nginx/hometree.conf`
- `src/main/java/com/familyhub/digital_family_hub/config/WebSocketConfig.java`

Treat existing uncommitted changes as user or prior-session work. Do not revert them unless the user explicitly asks.

## Update Policy For Future AI Sessions

Update this file after any change that affects how another AI should debug, run, deploy, or safely modify the project.

Always update `AI_HANDOFF.md` when changing:

- Auth, cookies, roles, `SecurityConfig`, JWT, OAuth redirects, logout/session lifecycle.
- WebSocket/STOMP/SockJS behavior, nginx `/ws` routing, reconnect/auth assumptions.
- API response envelope, `apiFetch`, shared DTO conventions, pagination contracts.
- Database schema migrations, entity relationships, Flyway strategy.
- Docker, nginx, env vars, build commands, runtime versions.
- Known bugs, blocked verification, failing tests, or provider setup requirements.

Update `PROJECT_CONTEXT.md` when changing architecture, module boundaries, feature ownership, runtime versions, API conventions, deployment topology, or long-lived roadmap assumptions.

If the code change is tiny and self-contained, add one line under "Recent Change Notes" only when it affects future debugging.

## Recent Change Notes

- Logout is implemented: `POST /auth/logout` is the public route, with compatible `POST /api/auth/logout`; both clear `HOMETREE_TOKEN`, invalidate any servlet session, clear `SecurityContext`, and return `200 OK`.
- Frontend logout clears local bearer fallback state, disconnects tracked STOMP clients, clears React Query cache, and redirects to `/login`.
- WebSocket auth is cookie-first: the backend reads the `HOMETREE_TOKEN` cookie during handshake, binds a `StompPrincipal`, stores it in session attributes, and falls back to STOMP bearer auth.
- WebSocket authorization blocks `VIEWER` from subscribing to room topics and sending to `/app/**`.
- STOMP app sends are supported at `/app/rooms/{roomId}` and delegate to the existing chat send service; REST chat send remains backward compatible.
- nginx now uses one `/ws` location for direct and SockJS subpaths, with websocket upgrade headers, and proxies `/auth/` to the API for logout.
- Frontend production build previously stalled after the Next.js banner in this environment. TypeScript and ESLint were reported passing, but `next build` must be verified in a clean Node 22 shell or CI before release.
- A local `client_secret_*.json` exists and is gitignored. The Google client secret was exposed in chat; rotate it in Google Cloud Console before real use.

## Verification Status

VERIFIED:

- Backend compile/test pass.
- Frontend TypeScript check pass.
- Frontend lint pass.
- Spring STOMP integration tests pass for authenticated connect, room subscribe, `/app/**` send, and `VIEWER` rejection.

NOT VERIFIED:

- Next.js production build stability.
- Real browser OAuth login flow.
- WebSocket runtime in real browser.
- Reconnect + multi-tab behavior.
- `nginx -t` locally; nginx is not installed in this Windows shell.
- CI pipeline.

## Execution Order Plan

1. Fix Next.js build stability.
2. Verify OAuth login in real browser.
3. Verify WebSocket connection + messaging.
4. Verify logout from the real browser after OAuth.
5. Verify nginx `/auth/` and `/ws` routing in the compose environment.

## Naming Standardization

Mandatory consistency:

- `HOMETREE_TOKEN` = primary session cookie.
- `VIEWER` = restricted role.
- `/ws` = WebSocket endpoint.

Do not use alternative naming in documentation for these concepts.

## First 15 Minutes Checklist

1. Run `git status --short` and inspect relevant diffs before editing.
2. Read this file, then `PROJECT_CONTEXT.md`, then the feature files you will touch.
3. If touching backend contracts, inspect matching frontend `features/<name>/api.ts`, `hooks.ts`, and `types.ts`.
4. If touching frontend API calls, inspect the backend controller/service/DTO first.
5. If touching auth/WebSocket/media/deployment/migrations, add or update tests/docs unless the change is purely investigative.
6. Preserve local secrets and generated outputs. Never commit `.env`, credential JSON, `target/`, `frontend/node_modules/`, or `frontend/.next/`.

## Known Bugs, Gaps, And Verification Risks

High impact:

- OAuth must be verified locally with `SPRING_PROFILES_ACTIVE=oauth` and rotated Google credentials.
- WebSocket auth needs browser verification after OAuth in both split localhost mode and same-origin nginx mode.
- Logout is implemented but still needs real-browser verification after OAuth.
- User role/admin management is not implemented; OAuth-created users default to `MEMBER`.
- Production deployment still needs TLS, secret handling, backups, health checks, and observability.

Medium impact:

- Cloudinary upload returns `503 Service Unavailable` until `CLOUDINARY_CLOUD_NAME` and `CLOUDINARY_UPLOAD_PRESET` are configured.
- Cursor pagination indexes exist, but timeline, chat, and notifications still use offset pagination in APIs.
- Group chat read receipts are not implemented; `chat_message_reads` exists but services still use single-message `seenAt`.
- Future-ready `media_links` and `family_member_closure` tables are not wired into services.
- Testcontainers PostgreSQL test is disabled unless Docker is available.

Build/test risk:

- Use Node 22 for frontend work. Node 24 has been observed to hang or behave badly on this project.
- Re-run `npm run typecheck`, `npm run lint`, and `npm run build` in `frontend/` after meaningful frontend changes.
- Re-run `.\mvnw.cmd test` after meaningful backend changes.

## Debug Maps

### Auth

Core files:

- `src/main/java/com/familyhub/digital_family_hub/config/SecurityConfig.java`
- `src/main/java/com/familyhub/digital_family_hub/auth/JwtAuthenticationFilter.java`
- `src/main/java/com/familyhub/digital_family_hub/auth/JwtService.java`
- `src/main/java/com/familyhub/digital_family_hub/auth/OAuth2LoginSuccessHandler.java`
- `src/main/java/com/familyhub/digital_family_hub/auth/AuthController.java`
- `frontend/src/components/layout/app-shell.tsx`
- `frontend/src/features/auth/*`

Current flow:

1. Frontend unauthenticated state links to `${NEXT_PUBLIC_API_BASE_URL}/oauth2/authorization/google`.
2. Spring OAuth runs only when the `oauth` profile is active.
3. Success handler upserts `AppUser`, audits login, issues JWT, sets HttpOnly `HOMETREE_TOKEN`, then redirects to `/auth/callback`.
4. Frontend callback invalidates `queryKeys.authMe` and returns home.
5. Later requests authenticate from cookie or `Authorization: Bearer`.
6. `GET /api/auth/me` drives the app shell.
7. `POST /auth/logout` clears `HOMETREE_TOKEN`, invalidates any servlet session, and returns the app to `/login`; `POST /api/auth/logout` is available for API-namespace compatibility.

Common failure points:

- Wrong Google redirect URI or stale client secret.
- Missing `SPRING_PROFILES_ACTIVE=oauth`.
- Cookie blocked by SameSite/Secure/domain mismatch.
- `CORS_ALLOWED_ORIGINS` not matching frontend origin.
- User exists with unexpected role.

### WebSocket / Chat Realtime

Core files:

- `src/main/java/com/familyhub/digital_family_hub/config/WebSocketConfig.java`
- `src/main/java/com/familyhub/digital_family_hub/chat/ChatService.java`
- `frontend/src/lib/websocket/stomp-client.ts`
- `frontend/src/lib/websocket/use-room-socket.ts`
- `frontend/src/components/messenger/messenger-view.tsx`
- `nginx/hometree.conf`

Current flow:

1. Client connects to `/ws` with SockJS/STOMP.
2. Browser sends `HOMETREE_TOKEN` cookie when cookie policy allows it.
3. Backend extracts cookie during handshake and binds `StompPrincipal`.
4. STOMP `CONNECT` can also authenticate from bearer header as fallback.
5. Client subscribes to `/topic/rooms/{roomId}`.
6. REST send persists message and broadcasts to that topic; STOMP send can also use `/app/rooms/{roomId}` and delegates to the same chat service.

Common failure points:

- Cookie not sent during SockJS handshake.
- nginx not proxying exact `/ws` or SockJS subpaths.
- `allowedOrigins` mismatch.
- `VIEWER` role attempting messaging.
- Frontend cache duplicate handling hides expected message.

### Media Upload

Core files:

- `src/main/java/com/familyhub/digital_family_hub/media/CloudinaryMediaStorageService.java`
- `src/main/java/com/familyhub/digital_family_hub/media/MediaController.java`
- `frontend/src/components/albums/albums-gallery.tsx`
- `frontend/src/features/albums/*`

Common failure points:

- Missing Cloudinary env vars.
- Upload preset not unsigned or not allowed for target file type.
- File exceeds configured max bytes.
- Network/provider response hidden behind generic `BAD_GATEWAY`.

### Database / Migrations

Core files:

- `src/main/resources/db/migration/V1__initial_hometree_schema.sql`
- `src/main/resources/db/migration/V2__performance_indexes_and_future_ready_schema.sql`
- JPA entities and repositories under each feature package.

Rules:

- Never edit an already-applied migration for a real environment.
- Add a new `V{next}__description.sql` migration for schema changes.
- Keep PostgreSQL as source of truth; do not rely on Hibernate schema mutation.

## Feature Status Snapshot

- Family tree: CRUD for members/relationships is scaffolded; graph UI exists; complex genealogy rules and closure table are not wired.
- Timeline: list/create and frontend feed/composer exist; cursor pagination, media links, edit/delete need work.
- Albums/media: albums and upload flow exist; Cloudinary must be configured; `media_links`, delete/reorder/reuse need work.
- Messenger: rooms/messages REST and STOMP broadcasts exist; group read receipts and participant model need work.
- Memorials: listings and tributes exist; media/moderation/delete are future work.
- Kitchen: recipe list/create/update exists; delete/search/filter are straightforward future work.
- Notifications: list/create/mark-read exists; realtime, mark-all, and cursor pagination are future work.
- Admin/governance: roles exist, but role management, invitations, approval, and privacy policy are not implemented.

## Next Recommended Tasks

1. Fix Next.js build stability.
2. Verify OAuth login in real browser.
3. Verify WebSocket connection + messaging in a real browser.
4. Verify logout end-to-end after OAuth.
5. Add admin role management or invitation gating before real family deployment.
6. Move timeline, chat, and notifications to cursor pagination.
7. Wire `chat_message_reads` and `media_links` when product behavior requires them.

## Dangerous Areas

Do not rewrite these casually:

- `SecurityConfig.java`: route and role policy.
- `JwtAuthenticationFilter.java`, `JwtService.java`, `OAuth2LoginSuccessHandler.java`: auth/session contract.
- `WebSocketConfig.java`: cookie-first handshake auth, reconnect-safe principal binding, subscription/send authorization.
- `src/main/resources/db/migration`: append only.
- `frontend/src/lib/api/client.ts`: envelope/fetch behavior.
- `frontend/src/lib/websocket/*`: STOMP/SockJS integration.
- `docker-compose.prod.yaml` and `nginx/hometree.conf`: production routing.
- `frontend/package-lock.json`: exact dependency graph.
- Media storage abstractions: provider behavior is environment-sensitive.
