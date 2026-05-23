# HomeTree Project Context

## Architecture Summary

HomeTree is a private family social network for genealogy, memory timelines, albums, chat, memorial tributes, recipes, notifications, and media uploads. The repository is a split full-stack application:

- Backend: Spring Boot 3.5.14, Java 25, Maven, PostgreSQL, Flyway, Spring Security, optional Redis cache, STOMP/SockJS WebSockets.
- Frontend: Next.js App Router, React 19, TypeScript, React Query, Zustand, Tailwind CSS, React Flow, STOMP/SockJS.
- Persistence: PostgreSQL is the source of truth. Flyway owns schema migrations.
- Deployment: production Compose builds separate API and web containers, then routes traffic through nginx.

The current codebase is feature-sliced but intentionally simple: REST controllers return DTOs in a shared response envelope, services own business logic and transactions, repositories are Spring Data JPA, and the frontend talks to the backend through typed feature API clients.

## Frontend Structure

- `frontend/src/app`: Next.js routes and app providers.
- `frontend/src/components`: layout, UI primitives, and feature-specific views.
- `frontend/src/features`: typed API clients, React Query hooks, and feature types.
- `frontend/src/lib/api`: `apiFetch`, envelope parsing, query keys.
- `frontend/src/lib/auth`: local token helper. Currently stores optional bearer token under `hometree.jwt`.
- `frontend/src/lib/websocket`: STOMP client and room subscription hook.
- `frontend/src/stores`: Zustand UI state for app shell, family tree, and chat drafts/selection.

React Query owns server state. Zustand should remain limited to client UI state and transient inputs.

## Backend Structure

- `auth`: OAuth success handling, JWT issue/validation, current-user endpoints.
- `users`: application identity and `ADMIN`, `MEMBER`, `VIEWER` roles.
- `family`: family members and typed relationships.
- `posts`: memory timeline posts.
- `albums` and `media`: album metadata, media links, Cloudinary upload service.
- `chat`: rooms, messages, REST endpoints, and STOMP broadcasts.
- `memorials`: deceased-member memorial listings and tributes.
- `kitchen`: recipe archive.
- `notifications`: in-app notifications and read state.
- `shared/api`: success/error response envelopes and exception handling.
- `shared/domain`: auditable base entity.
- `shared/audit`: audit logging service hook.
- `config`: security, cache, and WebSocket configuration.

## Auth Architecture

- Google OAuth is enabled only when the `oauth` Spring profile is active.
- OAuth starts at `/oauth2/authorization/google`.
- `OAuth2LoginSuccessHandler` upserts `AppUser`, audits login, issues an HS256 JWT, stores it in the HttpOnly `HOMETREE_TOKEN` cookie, and redirects to `hometree.auth.oauth-success-redirect`.
- `JwtAuthenticationFilter` accepts either `Authorization: Bearer <jwt>` or the `HOMETREE_TOKEN` cookie.
- `AuthController` exposes `GET /api/auth/me` and `GET /api/auth/login-options`.
- Role checks are centralized in `SecurityConfig`.
- Default user role is `MEMBER`; available roles are `ADMIN`, `MEMBER`, and `VIEWER`.
- Production sets `hometree.auth.secure-cookies=true` through `application-prod.yaml`.

## WebSocket Architecture

- STOMP endpoint: `/ws`, with SockJS fallback enabled.
- Message broker: simple in-memory broker on `/topic` and `/queue`.
- Application destination prefix: `/app`.
- Chat broadcasts are sent to `/topic/rooms/{roomId}` from `ChatService`.
- WebSocket auth accepts a JWT from the `HOMETREE_TOKEN` cookie during the handshake or a bearer token in the STOMP `CONNECT` headers.
- Subscriptions to `/topic/rooms/*` reject `VIEWER` principals.
- Frontend creates a STOMP client in `frontend/src/lib/websocket/stomp-client.ts`, subscribes in `use-room-socket.ts`, and sends a bearer header only if `hometree.jwt` exists in localStorage.

## Docker And Deployment

- Root `Dockerfile`: builds backend with `eclipse-temurin:25-jdk-alpine`, runs with `eclipse-temurin:25-jre-alpine`.
- `frontend/Dockerfile`: builds Next.js standalone output on `node:22-alpine`, runs `server.js`.
- `compose.yaml`: local PostgreSQL 16 and Redis 7 only.
- `docker-compose.prod.yaml`: API, web, PostgreSQL, Redis, and nginx.
- `nginx/hometree.conf`: routes `/` to web, `/api/`, `/oauth2/`, `/login/oauth2/`, and `/ws/` to API.
- Backend prod profile uses Redis cache and secure cookies.

## Versions

- Java: 25, fixed in `pom.xml` and backend Dockerfile.
- Spring Boot: 3.5.14.
- Maven wrapper: 3.3.4 wrapper, Maven 3.9.12 distribution.
- Node: frontend requires `>=22 <24`; do not use Node 24 for builds on Windows.
- npm: `>=10`.
- Next.js: package allows `^15.3.2`; lockfile currently resolves to 15.5.18.
- React: package allows `^19.1.0`; lockfile currently resolves to 19.2.6.
- PostgreSQL: 16 Alpine.
- Redis: 7 Alpine.

## API Conventions

- All backend routes live under `/api` except OAuth and `/ws`.
- Success envelope: `{ success, data, message, timestamp }`.
- Error envelope: `{ success, errorCode, message, errors, timestamp, path }`.
- Controllers return DTO records and delegate behavior to services.
- Use `@Valid` request DTOs for validation.
- Services own `@Transactional` boundaries.
- Pagination is currently page/size offset style where present.
- REST methods are conventional: `GET` list/read, `POST` create, `PUT` update, `PATCH` partial state changes.

## Frontend Conventions

- Use `apiFetch<T>()` for backend calls; it unwraps the backend envelope.
- Feature code should stay under `frontend/src/features/<feature>`.
- Server state belongs in React Query hooks.
- UI-only state belongs in Zustand stores.
- Use `@/*` path aliases.
- Keep Next.js App Router route files thin; put substantial UI in components.
- Keep typed route compatibility when adding navigation links.
- Build with the local Node 22 toolchain noted in `frontend/README.md`.

## Hard Constraints

- Do not change the Java 25 / Maven backend baseline without explicit approval.
- Do not replace PostgreSQL/Flyway as the persistence and migration foundation.
- Do not collapse frontend and backend into a single runtime.
- Do not bypass the shared API envelope unless changing all clients intentionally.
- Do not store uploaded media binaries in the application database.
- Do not commit real secrets, `.env` files, credential JSON, generated build outputs, `target/`, `frontend/node_modules/`, or `frontend/.next/`.
- Do not rewrite auth, WebSocket, migration, or media storage code casually; these are cross-cutting surfaces.

## Environment Assumptions

- Local backend defaults to `http://localhost:8080`.
- Local frontend defaults to `http://localhost:3000`.
- Local database defaults to `jdbc:postgresql://localhost:5432/hometree`.
- Local Redis defaults to `localhost:6379`.
- Local CORS default allows `http://localhost:3000`.
- OAuth requires `SPRING_PROFILES_ACTIVE=oauth` plus Google credentials.
- Cloudinary upload requires `CLOUDINARY_CLOUD_NAME` and `CLOUDINARY_UPLOAD_PRESET`.
- Production should set a strong `JWT_SECRET`, exact `CORS_ALLOWED_ORIGINS`, and secure cookie compatible HTTPS routing.

## Important Directories And Files

- `pom.xml`: backend dependencies and Java version.
- `.mvn/wrapper/maven-wrapper.properties`: Maven version.
- `src/main/resources/application*.yaml`: backend configuration profiles.
- `src/main/resources/db/migration`: Flyway schema migrations.
- `src/main/java/com/familyhub/digital_family_hub/config/SecurityConfig.java`: HTTP security and role policy.
- `src/main/java/com/familyhub/digital_family_hub/config/WebSocketConfig.java`: STOMP broker, endpoint, and subscription auth.
- `src/main/java/com/familyhub/digital_family_hub/auth`: JWT/OAuth/current-user flow.
- `frontend/package.json` and `frontend/package-lock.json`: frontend runtime and exact dependency graph.
- `frontend/src/lib/api/client.ts`: frontend API behavior.
- `frontend/src/lib/websocket`: frontend STOMP behavior.
- `Dockerfile`, `frontend/Dockerfile`, `compose.yaml`, `docker-compose.prod.yaml`, `nginx/hometree.conf`: container and proxy setup.
- `docs/`: existing architecture and database notes.

## Known Risks

- WebSocket auth should be tested end-to-end in both same-origin production proxy mode and split localhost mode; frontend bearer tokens are optional and no current endpoint exposes a JS-readable token after OAuth.
- `docs/HOMETREE_ARCHITECTURE.md` has older notes that mention JWT/role hardening as future work; the code now includes JWT issuance and centralized role checks.
- Cloudinary upload is disabled until environment variables are configured; upload attempts return service unavailable when unset.
- Testcontainers integration test is disabled unless Docker is available.
- `client_secret_*.json` exists locally but is gitignored; keep it untracked and rotate if it ever leaked.
- Chat read receipts are still single `seenAt` on messages; `chat_message_reads` exists as future-ready schema but is not wired into services.
- Cursor pagination schema indexes exist, but API pagination still uses offset page/size in several places.

## Completed Milestones

- Spring Boot backend skeleton with feature modules.
- PostgreSQL/Flyway schema for users, family graph, posts, albums, media, chat, notifications, recipes, and memorials.
- Performance/future-ready schema migration with indexes and future tables.
- Google OAuth profile, JWT cookie issuance, bearer/cookie API auth, and role-based route policy.
- Next.js frontend shell with feature routes, typed clients, React Query hooks, and Zustand UI stores.
- STOMP/SockJS chat broadcast path for room messages.
- Cloudinary upload service abstraction.
- Dockerfiles for API and web plus production Compose and nginx proxy.

## Remaining Roadmap

- Verify OAuth and WebSocket auth end-to-end in local split-origin and production proxied modes.
- Add logout and token/session lifecycle UX.
- Add admin user/role management and invitation/onboarding controls.
- Replace offset pagination with cursor pagination for timeline, chat, and notifications.
- Wire future `media_links`, `chat_message_reads`, and `family_member_closure` tables into services when needed.
- Expand backend service/controller tests and frontend component/integration tests.
- Harden production deployment with TLS, secrets management, health checks, backups, and observability.
