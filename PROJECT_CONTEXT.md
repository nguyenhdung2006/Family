# HomeTree Project Context

This is the durable architecture map for HomeTree. Use `AI_HANDOFF.md` for the current debug runbook and short-term known issues.

Last reviewed: 2026-05-24.

## Product Summary

HomeTree is a private family social network for genealogy, memory timelines, albums, chat, memorial tributes, recipes, notifications, and media uploads.

The project is intentionally split:

- Backend: Spring Boot API.
- Frontend: Next.js web app.
- Persistence: PostgreSQL managed by Flyway.
- Edge: nginx routes browser, API, OAuth, and WebSocket traffic in production.

## Current Architecture

Text system flow diagram:

```text
Browser
  |
  | HTTPS in production / localhost in development
  v
Next.js frontend
  |
  | REST calls with credentials
  | Auth state check: GET /api/auth/me
  v
Nginx reverse proxy
  |
  | /api/**, /auth/**, /oauth2/**, /login/oauth2/**, /ws
  v
Spring Boot backend
  |
  | OAuth login start: /oauth2/authorization/google
  v
Google OAuth
  |
  | OAuth callback handled by Spring Security
  v
Spring Boot backend
  |
  | Issues JWT and stores it in HttpOnly cookie: HOMETREE_TOKEN
  v
Browser cookie jar
  |
  | Sends HOMETREE_TOKEN on later API and /ws requests
  v
Spring Boot backend
  |
  | Validates HOMETREE_TOKEN, binds authenticated principal and role
  +--> PostgreSQL via JPA/Flyway-managed schema
  +--> Redis cache when enabled by profile
  +--> Cloudinary upload API when media env vars are configured

Realtime path:

Browser
  |
  | SockJS/STOMP connect to /ws with HOMETREE_TOKEN cookie
  v
Nginx reverse proxy
  |
  | Proxies /ws to Spring Boot, including SockJS subpaths
  v
Spring Boot WebSocket/STOMP
  |
  | Binds principal from HOMETREE_TOKEN during handshake/CONNECT
  | Subscribe: /topic/rooms/{id}
  | Send authorization: /app/**
  | Send endpoint: /app/rooms/{id}
  v
Chat room broadcasts on /topic/rooms/{id}
```

## Architecture Principles

- Keep the backend and frontend as separate runtimes.
- Keep PostgreSQL/Flyway as the persistence and migration foundation.
- Keep REST controllers thin: controllers return DTOs, services own behavior and transactions, repositories own persistence.
- Keep all backend API responses in the shared success/error envelope.
- Keep frontend server state in React Query and UI-only state in Zustand.
- Add migrations for schema changes. Do not rely on Hibernate `ddl-auto` for production schema mutation.
- Prefer feature-scoped changes over cross-cutting rewrites.

## Stack And Versions

- Java: 25, fixed in `pom.xml` and backend Dockerfile.
- Spring Boot: 3.5.14.
- Maven wrapper: 3.3.4 wrapper, Maven 3.9.12 distribution.
- Node: `>=22 <24`; use Node 22 on Windows.
- npm: `>=10`.
- Next.js: package allows `^15.3.2`; lockfile currently resolves to 15.5.18.
- React: package allows `^19.1.0`; lockfile currently resolves to 19.2.6.
- PostgreSQL: 16 Alpine.
- Redis: 7 Alpine.

## Backend Module Map

- `auth`: OAuth success handling, JWT issue/validation, current-user endpoints.
- `users`: application identity and `ADMIN`, `MEMBER`, `VIEWER` roles.
- `family`: family members, branches, and typed relationships.
- `posts`: memory timeline posts.
- `albums`: album metadata and album/media attachment.
- `media`: media asset records and Cloudinary upload abstraction.
- `chat`: rooms, messages, REST endpoints, and STOMP broadcasts.
- `memorials`: deceased-member memorial listings and tributes.
- `kitchen`: recipe archive.
- `notifications`: in-app notifications and read state.
- `shared/api`: success/error response envelopes and exception handling.
- `shared/domain`: auditable base entity.
- `shared/audit`: audit logging service hook.
- `config`: security, cache, and WebSocket configuration.
- `system`: health endpoint.

## Frontend Module Map

- `frontend/src/app`: Next.js App Router routes and providers.
- `frontend/src/components`: layout, UI primitives, and feature-specific views.
- `frontend/src/features`: typed API clients, React Query hooks, and feature types.
- `frontend/src/lib/api`: `apiFetch`, envelope parsing, query keys.
- `frontend/src/lib/auth`: optional local bearer-token helper using `hometree.jwt`.
- `frontend/src/lib/websocket`: STOMP client and room subscription hook.
- `frontend/src/stores`: Zustand state for app shell, family tree, and chat UI.

## API Conventions

- Backend routes live under `/api` except OAuth and `/ws`.
- Success envelope: `{ success, data, message, timestamp }`.
- Error envelope: `{ success, errorCode, message, errors, timestamp, path }`.
- Controllers return DTO records and delegate behavior to services.
- Use `@Valid` request DTOs for validation.
- Services own `@Transactional` boundaries.
- Pagination is currently page/size offset style where present.
- REST method convention: `GET` list/read, `POST` create, `PUT` replace/update, `PATCH` partial state change.
- Frontend calls backend through `apiFetch<T>()`; do not bypass it without updating all affected clients.

## Implemented API Surfaces

- `GET /api/health`
- `GET /api/auth/me`
- `GET /api/auth/login-options`
- `POST /auth/logout`
- `POST /api/auth/logout`
- `GET|POST /api/family/members`
- `GET|PUT /api/family/members/{id}`
- `GET /api/family/branches/{branch}/members`
- `GET /api/family/members/{id}/relationships`
- `POST|PUT /api/family/relationships`
- `GET|POST /api/timeline/posts`
- `GET|POST /api/albums`
- `PUT /api/albums/{albumId}`
- `GET|POST /api/albums/{albumId}/media`
- `POST /api/media/upload`
- `GET|POST /api/messages/rooms`
- `PUT /api/messages/rooms/{roomId}`
- `GET|POST /api/messages/rooms/{roomId}`
- `PATCH /api/messages/{messageId}/seen`
- `GET|POST /api/kitchen/recipes`
- `PUT /api/kitchen/recipes/{recipeId}`
- `GET /api/memorials`
- `GET|POST /api/memorials/{memberId}/tributes`
- `PUT /api/memorials/{memberId}/tributes/{tributeId}`
- `GET|POST /api/notifications`
- `PATCH /api/notifications/{id}/read`

## Security And Role Policy

Roles:

- `ADMIN`: can manage family data and normal member surfaces.
- `MEMBER`: can create most family content and use messaging.
- `VIEWER`: can read selected surfaces but cannot mutate family/chat content.

Current policy is centralized in `SecurityConfig`:

- `/api/health` and `/api/auth/login-options` are public.
- `GET /api/family/**`, `GET /api/timeline/**`, `GET /api/albums/**`, `GET /api/notifications/**`, `GET /api/memorials/**`, and `GET /api/kitchen/**` allow `ADMIN`, `MEMBER`, `VIEWER`.
- Mutating family endpoints require `ADMIN`.
- Timeline, albums, notifications, memorials, and kitchen mutations generally require `ADMIN` or `MEMBER`.
- `/api/media/**` and `/api/messages/**` require `ADMIN` or `MEMBER`.
- `/ws` and `/ws/**` allow authenticated `ADMIN`, `MEMBER`, and `VIEWER` handshakes; STOMP inbound rules block `VIEWER` from `/app/**` sends and restricted `/topic/rooms/**` subscriptions.
- Remaining `/api/**` routes require authentication.

## Auth Flow Summary (Cookie-First)

- `HOMETREE_TOKEN` is the primary session cookie.
- Google OAuth is enabled only when the `oauth` Spring profile is active.
- OAuth starts at `/oauth2/authorization/google`.
- After OAuth success, `OAuth2LoginSuccessHandler` upserts `AppUser`, audits login, issues an HS256 JWT, stores that JWT in the HttpOnly `HOMETREE_TOKEN` cookie, and redirects to `hometree.auth.oauth-success-redirect`.
- Browser JavaScript does not need to read `HOMETREE_TOKEN`; the browser sends it automatically when cookie policy allows credentials.
- `JwtAuthenticationFilter` validates backend requests by first accepting `Authorization: Bearer <jwt>` when present, otherwise reading `HOMETREE_TOKEN` from cookies.
- Bearer JWT is a compatibility fallback for clients that explicitly send an `Authorization` header; the normal browser session path is `HOMETREE_TOKEN`.
- Frontend determines auth state by calling `GET /api/auth/me` through React Query. A successful response means authenticated; an unauthorized response shows the Google login screen.
- `AuthController` exposes `GET /api/auth/me`, `GET /api/auth/login-options`, and compatible `POST /api/auth/logout`.
- `POST /auth/logout` is the public logout route; it clears the HttpOnly `HOMETREE_TOKEN` cookie, invalidates any servlet session, clears the security context, and returns `200 OK`.
- Default OAuth-created user role is `MEMBER`.
- Production sets `hometree.auth.secure-cookies=true` through `application-prod.yaml`.

## WebSocket Flow Summary

- STOMP endpoint: `/ws`, with SockJS fallback enabled.
- STOMP lifecycle: client connects to `/ws`, sends STOMP `CONNECT`, subscribes to destinations, receives broker messages, and reconnects through the configured frontend STOMP client when needed.
- Message broker destinations: `/topic` and `/queue`.
- Application destination prefix: `/app`.
- Chat room subscriptions use `/topic/rooms/{id}`.
- Chat broadcasts are sent to `/topic/rooms/{id}` from `ChatService`.
- STOMP sends to `/app/rooms/{roomId}` delegate to the existing `ChatService.sendMessage(...)`; REST `POST /api/messages/rooms/{roomId}` remains supported.
- WebSocket authentication is cookie-first: backend extracts and validates `HOMETREE_TOKEN` during the `/ws` handshake, binds a `StompPrincipal`, and stores it in handshake/session attributes so STOMP `CONNECT` can reuse the same authenticated identity.
- STOMP bearer auth remains as a compatibility fallback when a client sends `Authorization: Bearer <jwt>` in `CONNECT` headers.
- Subscribe authorization: `/topic/rooms/{id}` requires an authenticated principal whose role is not `VIEWER`.
- Send authorization: `/app/**` requires an authenticated principal whose role is not `VIEWER`.
- `VIEWER` cannot subscribe to blocked chat room topics and cannot send to `/app/**`.
- Frontend creates the STOMP client in `frontend/src/lib/websocket/stomp-client.ts`, subscribes in `use-room-socket.ts`, and sends a bearer header only if `hometree.jwt` exists in localStorage.
- Spring STOMP integration tests cover authenticated connect, room subscribe, `/app/**` send success, and `VIEWER` rejection. Browser end-to-end verification is still required in split localhost and same-origin nginx modes.

## Data Model Direction

Current schema:

- `V1__initial_hometree_schema.sql`: core users, family graph, posts, albums, media, chat, notifications, recipes, and memorials.
- `V2__performance_indexes_and_future_ready_schema.sql`: performance indexes and future-ready tables.

Future-ready tables not fully wired:

- `media_links`: reusable media ownership across albums, posts, members, tributes, and recipes.
- `chat_message_reads`: proper per-user group chat read receipts.
- `family_member_closure`: fast ancestor/descendant graph queries.

Pagination direction:

- Offset pagination is acceptable for early pages.
- Timeline, chat, and notifications should move to cursor pagination using the existing cursor-friendly indexes.

## Feature Matrix

| Feature | Backend | Frontend | Current gaps |
| --- | --- | --- | --- |
| Health | Implemented | N/A | None significant |
| Auth/profile | OAuth/JWT/current user/logout | Login gate/profile shell/logout | Role management, invitation gate, real-browser OAuth logout verification |
| Family tree | Members and relationships | React Flow graph, create/edit forms | Delete, complex genealogy rules, closure table |
| Timeline | List/create posts | Feed/composer/year filter | Edit/delete, cursor pagination, media/tag depth |
| Albums/media | Albums, media attach, Cloudinary upload | Upload/progress/gallery/slideshow | Cloudinary config, delete/reorder/reuse media |
| Messenger | Rooms/messages/seen/broadcast | Rooms, composer, realtime cache merge | Participants, group read receipts, cursor pagination |
| Memorials | Memorials and tributes | Tribute view/form | Delete, media links, moderation |
| Kitchen | Recipe list/create/update | Recipe form/list | Delete, search/filter |
| Notifications | List/create/mark read | Alerts page/form | Realtime, mark all read, cursor pagination |
| Admin/governance | Roles only | Not implemented | User management, invitations, privacy rules |

## Docker And Deployment

- Root `Dockerfile`: builds backend with `eclipse-temurin:25-jdk-alpine`, runs with `eclipse-temurin:25-jre-alpine`.
- `frontend/Dockerfile`: builds Next.js standalone output on `node:22-alpine`, runs `server.js`.
- `compose.yaml`: local PostgreSQL 16 and Redis 7 only.
- `docker-compose.prod.yaml`: API, web, PostgreSQL, Redis, and nginx.
- `nginx/hometree.conf`: routes `/` to web and `/api/`, `/auth/`, `/oauth2/`, `/login/oauth2/`, and `/ws` to API.
- Backend prod profile uses Redis cache and secure cookies.

Production still needs:

- TLS/cert management.
- Secret management.
- Database backup/restore plan.
- Health checks.
- Centralized logs/metrics/alerts.
- Real OAuth, logout, and WebSocket verification behind HTTPS.

## Environment Assumptions

- Local backend: `http://localhost:8080`.
- Local frontend: `http://localhost:3000`.
- Local database: `jdbc:postgresql://localhost:5432/hometree`.
- Local Redis: `localhost:6379`.
- Local CORS default: `http://localhost:3000`.
- OAuth requires `SPRING_PROFILES_ACTIVE=oauth`, `GOOGLE_CLIENT_ID`, and `GOOGLE_CLIENT_SECRET`.
- Cloudinary upload requires `CLOUDINARY_CLOUD_NAME` and `CLOUDINARY_UPLOAD_PRESET`.
- Production should set a strong `JWT_SECRET`, exact `CORS_ALLOWED_ORIGINS`, and HTTPS-compatible secure cookie settings.

## Verification Commands

Backend:

```powershell
.\mvnw.cmd -version
.\mvnw.cmd test
.\mvnw.cmd -DskipTests package
```

Frontend:

```powershell
cd frontend
npm ci
npm run typecheck
npm run lint
npm run build
```

Local infrastructure:

```powershell
docker compose up -d postgres redis
```

Known verification caveats:

- Testcontainers PostgreSQL test is disabled unless Docker is available.
- Frontend production build previously stalled after the Next.js banner in this environment. Use Node 22 and verify in a clean shell or CI.
- OAuth and Cloudinary require external credentials.

## Important Files

- `pom.xml`: backend dependencies and Java version.
- `.mvn/wrapper/maven-wrapper.properties`: Maven version.
- `src/main/resources/application*.yaml`: backend configuration profiles.
- `src/main/resources/db/migration`: Flyway schema migrations.
- `src/main/java/com/familyhub/digital_family_hub/config/SecurityConfig.java`: HTTP security and role policy.
- `src/main/java/com/familyhub/digital_family_hub/config/WebSocketConfig.java`: STOMP broker, endpoint, and WebSocket authorization.
- `src/main/java/com/familyhub/digital_family_hub/auth`: JWT/OAuth/current-user flow.
- `frontend/package.json` and `frontend/package-lock.json`: frontend runtime and exact dependency graph.
- `frontend/src/lib/api/client.ts`: frontend API behavior.
- `frontend/src/lib/websocket`: frontend STOMP behavior.
- `Dockerfile`, `frontend/Dockerfile`, `compose.yaml`, `docker-compose.prod.yaml`, `nginx/hometree.conf`: container and proxy setup.
- `docs/PROJECT_STATUS_AND_AI_BUILD_NOTES.md`: detailed difficulty-ordered build status and AI capability notes.
- `AI_HANDOFF.md`: current-session runbook and known debug risks.

## Hard Constraints

- Do not change the Java 25 / Maven backend baseline without explicit approval.
- Do not replace PostgreSQL/Flyway.
- Do not collapse frontend and backend into a single runtime.
- Do not bypass the shared API envelope unless changing all clients intentionally.
- Do not store uploaded media binaries in the application database.
- Do not commit real secrets, `.env` files, credential JSON, generated build outputs, `target/`, `frontend/node_modules/`, or `frontend/.next/`.
- Do not rewrite auth, WebSocket, migrations, media storage, or deployment routing casually.

## Known Risks

- WebSocket auth is cookie-first with bearer fallback, but still needs end-to-end browser verification.
- Logout is implemented but still needs real-browser OAuth session verification.
- OAuth credentials must be rotated because the Google client secret was exposed in chat.
- `docs/HOMETREE_ARCHITECTURE.md` contains older notes around JWT/role hardening; prefer this file and `AI_HANDOFF.md` for current auth status.
- Cloudinary upload is disabled until env vars are configured.
- Chat read receipts are still single-message `seenAt`.
- Cursor pagination schema support exists, but APIs still use offset pagination.
- Admin/governance/privacy rules are not implemented and should be specified before real family deployment.

## Completed Milestones

- Spring Boot backend skeleton with feature modules.
- PostgreSQL/Flyway schema for users, family graph, posts, albums, media, chat, notifications, recipes, and memorials.
- Performance/future-ready schema migration with indexes and future tables.
- Google OAuth profile, JWT cookie issuance, bearer/cookie API auth, and role-based route policy.
- Logout endpoint, cookie/session cleanup, frontend logout UX, and explicit STOMP disconnect.
- Next.js frontend shell with feature routes, typed clients, React Query hooks, and Zustand UI stores.
- STOMP/SockJS chat broadcast path with cookie-first authenticated handshake, authenticated room subscriptions, `/app/rooms/{roomId}` sends, and `VIEWER` denial tests.
- Cloudinary upload service abstraction.
- Dockerfiles for API and web plus production Compose and nginx proxy.

## Roadmap

1. Rotate Google secret and verify OAuth locally.
2. Verify logout and WebSocket auth end-to-end in split localhost and nginx same-origin modes.
3. Add admin user/role management and invitation/onboarding controls.
4. Replace offset pagination with cursor pagination for timeline, chat, and notifications.
5. Wire `media_links`, `chat_message_reads`, and `family_member_closure` when product behavior needs them.
6. Expand backend service/controller tests and frontend component/integration tests.
7. Harden production deployment with TLS, secrets management, health checks, backups, and observability.

## Documentation Maintenance

When code changes, update docs in the same change set:

- Update `AI_HANDOFF.md` for short-term status, debug risks, verification blockers, dangerous files, and next recommended tasks.
- Update this file for durable architecture, module ownership, API conventions, runtime versions, deployment topology, and long-lived roadmap changes.
- Update `README.md` when commands, environment variables, or user-facing setup steps change.
- Update feature docs under `docs/` when deep behavior changes.
- Remove or clearly mark stale notes instead of letting contradictory docs accumulate.
