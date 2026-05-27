# AI Handoff

Last reviewed: 2026-05-27.

This is the first-read runbook for any future AI engineer or developer taking over HomeTree. It is intentionally practical: what the system is, what is real today, where the risky edges are, and how to keep moving without breaking the project.

## PROJECT SUMMARY

HomeTree Digital Family Hub is a private family social network with a Spring Boot backend and a Next.js frontend. It is built around a multi-generation family use case: preserving family member profiles, genealogy relationships, memories, albums, chat rooms, memorial tributes, recipes, notifications, and authenticated family access.

The target users are family administrators, normal family members, and limited viewers:

| Role | Intended use |
| --- | --- |
| `ADMIN` | Maintains family tree records and has full member capabilities. |
| `MEMBER` | Shares memories, albums, chat messages, recipes, tributes, and notifications. |
| `VIEWER` | Reads selected family content but should not mutate protected areas or use chat. |

Core workflows implemented in some form:

- Sign in with Google OAuth, receive an HttpOnly `HOMETREE_TOKEN` JWT cookie, and load the app through `GET /api/auth/me`.
- Browse and maintain family members and relationships.
- Share memory timeline posts with dates, event types, locations, and tagged family member ids.
- Create albums, upload media to Cloudinary, and attach uploaded media to albums.
- Create chat rooms, send messages through REST or STOMP, receive room broadcasts, and mark messages seen.
- View deceased family members and add/edit memorial tributes.
- Create and edit family recipes.
- Create/list/mark-read in-app notifications.
- Run locally with PostgreSQL and Redis through Docker Compose.
- Deploy as separate API, web, PostgreSQL, Redis, and nginx services.

The product goal is to give a family a private, durable, warm digital home. The current code is a working MVP foundation, but privacy/governance, ownership, pagination, production operations, and end-to-end provider verification are not production-complete.

## CURRENT STATUS

### Completed or Working

- Backend Spring Boot module structure is coherent and feature-oriented.
- PostgreSQL schema exists through Flyway migrations `V1` and `V2`.
- Shared API envelope is implemented through `ApiResponse` and `ApiErrorResponse`.
- Central exception handling exists in `GlobalExceptionHandler`.
- Google OAuth profile is isolated in `application-oauth.yaml`.
- OAuth success handler upserts `AppUser`, issues JWT, writes `HOMETREE_TOKEN`, and redirects to the frontend callback route.
- JWT API authentication supports cookie-first browser auth and bearer-token fallback.
- Role-based HTTP policy exists in `SecurityConfig`.
- Logout exists at both `POST /auth/logout` and `POST /api/auth/logout`.
- WebSocket/STOMP chat endpoint `/ws` uses SockJS, cookie-first handshake auth, bearer fallback, and blocks `VIEWER` from room topics and `/app/**` sends.
- Frontend App Router shell, login screen, profile view, navigation, feature routes, typed API clients, React Query hooks, and Zustand UI stores exist.
- Family tree uses React Flow and editable create/update forms for members and relationships.
- Timeline, albums/media, messenger, memorials, kitchen, and notifications have usable frontend screens.
- Dockerfiles exist for API and web; production Compose includes nginx routing.
- Backend JWT and STOMP tests exist and previously passed.

### Partially Working

- OAuth works by design but requires real Google credentials and browser verification. OAuth-created users default to `MEMBER`.
- Cloudinary upload service is implemented, but real uploads require `CLOUDINARY_CLOUD_NAME` and `CLOUDINARY_UPLOAD_PRESET`.
- Chat realtime path is implemented and tested at Spring/STOMP level, but still needs real browser and nginx verification.
- Family graph works for simple trees, but relationship semantics are shallow.
- Database has future-ready tables for `media_links`, `chat_message_reads`, and `family_member_closure`, but services do not use them.
- Offset pagination exists in many APIs; cursor-ready indexes exist but cursor APIs are not implemented.
- Several models contain ownership fields (`author`, `createdBy`, `recipient`, `participants`) that are not consistently populated or enforced.
- Frontend hides unauthenticated state, but it does not hide mutation UI by role.
- Production Compose is structurally present but lacks TLS, backups, health checks, observability, and secret management.

### Broken, Incomplete, or Risky

- No admin user management, role management, invitation gate, or onboarding approval flow.
- No privacy model by branch, family member, album, memorial, or chat room.
- Notifications are global in practice: list returns all notifications and create does not set a recipient.
- Chat room participants are modeled but unused for authorization; any `ADMIN`/`MEMBER` can list/read/send to any room.
- Message read state is global per message (`seenAt`), not per user.
- Timeline posts, albums, recipes, tributes, notifications, and many media assets do not record the authenticated creator.
- CSRF is ignored for `/api/**` while cookie auth is used. This is acceptable for early local development but risky for production.
- `SecurityConfig` ends with `.anyRequest().permitAll()`, so newly added non-API backend routes could become public unless explicitly secured.
- Frontend production build previously stalled after only printing the Next.js banner in this environment.
- A local `client_secret_*.json` exists in the workspace and is gitignored. Do not commit it. Rotate any real Google secret that was exposed outside a secret manager.

## ACTIVE ARCHITECTURE

### Backend Architecture

Backend root package: `src/main/java/com/familyhub/digital_family_hub`.

| Package | Responsibility |
| --- | --- |
| `auth` | OAuth success handling, JWT issue/validation, current user, login options, logout. |
| `users` | `AppUser`, roles, user repository. |
| `family` | Family members, branches, relationships, relationship DTOs, family service. |
| `posts` | Memory timeline posts, event types, tagging family members. |
| `albums` | Album metadata and attaching media assets to albums. |
| `media` | Media asset entity and Cloudinary upload abstraction. |
| `chat` | Chat rooms, messages, REST API, STOMP app send handler, broadcasts. |
| `memorials` | Deceased-member memorial listing and tributes. |
| `kitchen` | Family recipe archive. |
| `notifications` | In-app notification records and read state. |
| `shared/api` | Response envelope and global exception mapping. |
| `shared/domain` | `AuditableEntity` id/timestamps base class. |
| `shared/audit` | Audit log hook for log-based events. |
| `config` | Security, WebSocket/STOMP, cache configuration. |
| `system` | Health endpoint. |

Backend pattern:

```text
HTTP request
  -> Controller validates DTO with @Valid
  -> Service owns transaction and business behavior
  -> Repository performs JPA persistence
  -> Service maps entity to response DTO
  -> Controller wraps with ApiResponse
```

### Frontend Architecture

Frontend root: `frontend/`.

| Folder | Responsibility |
| --- | --- |
| `frontend/src/app` | Next.js App Router pages and providers. |
| `frontend/src/components` | Layout, UI primitives, and feature-specific views. |
| `frontend/src/features` | Feature API clients, hooks, and TypeScript types. |
| `frontend/src/lib/api` | `apiFetch`, envelope parsing, query keys, shared API types. |
| `frontend/src/lib/auth` | Optional local bearer-token fallback storage. |
| `frontend/src/lib/websocket` | STOMP client and room subscription hook. |
| `frontend/src/stores` | Zustand stores for UI-only state. |

Frontend data ownership:

- React Query owns server data.
- Zustand owns UI state only: sidebar, selected tree member, chat drafts, active room.
- `apiFetch<T>()` is the standard REST client and expects backend envelope responses.
- `uploadMedia()` uses XHR instead of `apiFetch` so it can report upload progress.

### API Structure

Important endpoint families:

- `GET /api/health`
- `GET /api/auth/me`
- `GET /api/auth/login-options`
- `POST /auth/logout`
- `POST /api/auth/logout`
- `/api/family/**`
- `/api/timeline/posts`
- `/api/albums/**`
- `/api/media/upload`
- `/api/messages/**`
- `/api/memorials/**`
- `/api/kitchen/recipes`
- `/api/notifications/**`
- WebSocket/STOMP endpoint `/ws`

All normal REST responses should stay in this envelope:

```json
{
  "success": true,
  "data": {},
  "message": "OK",
  "timestamp": "..."
}
```

Errors use:

```json
{
  "success": false,
  "errorCode": "VALIDATION_ERROR",
  "message": "...",
  "errors": [],
  "timestamp": "...",
  "path": "/api/..."
}
```

### Database Structure

Flyway migrations:

- `src/main/resources/db/migration/V1__initial_hometree_schema.sql`
- `src/main/resources/db/migration/V2__performance_indexes_and_future_ready_schema.sql`

Core tables:

- `app_users`
- `family_members`
- `family_relationships`
- `memory_posts`
- `memory_post_tagged_members`
- `albums`
- `media_assets`
- `chat_rooms`
- `chat_room_members`
- `chat_messages`
- `in_app_notifications`
- `recipes`
- `memorial_tributes`

Future-ready but mostly unused:

- `media_links`
- `chat_message_reads`
- `family_member_closure`

Rules:

- Do not edit already-applied migrations in a real environment.
- Add a new `V{next}__description.sql` migration for schema changes.
- Keep `spring.jpa.hibernate.ddl-auto=validate` as the default source-of-truth stance.

### Deployment Architecture

Local:

- `compose.yaml` runs PostgreSQL 16 and Redis 7.
- Backend runs on `localhost:8080`.
- Frontend runs on `localhost:3000`.

Production:

- `docker-compose.prod.yaml` defines `api`, `web`, `postgres`, `redis`, and `nginx`.
- Root `Dockerfile` builds and runs the Spring Boot app on Eclipse Temurin Java 25.
- `frontend/Dockerfile` builds Next.js standalone output on Node 22.
- `nginx/hometree.conf` proxies `/` to web and `/api/`, `/auth/`, `/oauth2/`, `/login/oauth2/`, and `/ws` to API.

## DEVELOPMENT PATTERNS

### Naming Conventions

- Backend package is `com.familyhub.digital_family_hub`.
- Feature packages use singular domain names where already established: `family`, `posts`, `albums`, `chat`, `media`, `memorials`, `kitchen`, `notifications`.
- Entity classes are nouns: `FamilyMember`, `MemoryPost`, `Album`, `MediaAsset`, `ChatRoom`.
- DTO containers end in `DTO` and usually contain `Request` and `Response` records.
- Repositories extend `JpaRepository<Entity, UUID>`.
- Frontend feature folders mirror backend feature names.
- Frontend input types use `CreateXInput` and `UpdateXInput`.

Mandatory canonical names:

- `HOMETREE_TOKEN` for the session cookie.
- `ADMIN`, `MEMBER`, `VIEWER` for roles.
- `/ws` for WebSocket/STOMP.
- `/topic/rooms/{roomId}` for chat room broadcasts.
- `/app/rooms/{roomId}` for STOMP chat sends.

### Controller Patterns

- Controllers are thin and route-scoped.
- Controllers return `ApiResponse<T>` for JSON responses.
- Create endpoints use `@ResponseStatus(HttpStatus.CREATED)`.
- Request bodies use `@Valid`.
- Path ids use `UUID`.
- Pagination parameters default to `page=0` and feature-specific sizes.

### DTO Patterns

- Request DTOs are Java records with Jakarta validation annotations.
- Response DTOs are Java records with static `from(entity)` mappers.
- DTOs intentionally avoid exposing full nested JPA entities.
- Current response DTOs often omit creator/author metadata even when entities have relationships.

### Service Patterns

- Services own `@Transactional` boundaries.
- Read methods use `@Transactional(readOnly = true)`.
- Services throw `ResponseStatusException` for expected 404/400/401 cases.
- Cache annotations are currently used on family member listing and timeline listing only.
- Audit logging is present in family, timeline, auth, and chat, but not every feature.

### Validation Style

- Bean validation handles required fields and lengths.
- Services add a few cross-field checks, such as no self-relationship and tribute only for deceased members.
- Missing today: birth/death chronology, relationship semantic rules, page parameter validation, duplicate relationship friendly errors, URL/content validation beyond media upload.

### Error Handling Style

- `GlobalExceptionHandler` maps validation, `ResponseStatusException`, auth exceptions, and unexpected exceptions.
- Constraint violations and illegal page parameters can still become generic 500s because there is no dedicated handler for all common invalid request exceptions.
- WebSocket authorization failures currently throw `IllegalArgumentException`, which STOMP tests assert as transport/session errors.

### Testing Style

- Backend tests use JUnit 5, Spring Boot Test, Spring Security Test, Mockito beans, and Testcontainers dependency.
- Existing tests cover JWT issue/validation, JWT cookie auth filter, Spring context, and WebSocket/STOMP auth behavior.
- `PostgresContainerIntegrationTest` is disabled until Docker is available.
- Frontend has `typecheck`, `lint`, and `build` scripts but no committed component/E2E test suite.

## IMPORTANT BUSINESS RULES

### Authentication and Session Rules

- Google OAuth starts at `/oauth2/authorization/google`.
- OAuth config is active only with Spring profile `oauth`.
- OAuth success creates or updates an `AppUser` by email.
- New OAuth users default to `MEMBER` because `AppUser.role` defaults to `UserRole.MEMBER`.
- OAuth success issues a custom HS256 JWT and stores it in an HttpOnly `HOMETREE_TOKEN` cookie.
- API auth reads `Authorization: Bearer` first, then `HOMETREE_TOKEN`.
- Logout clears `HOMETREE_TOKEN`, invalidates servlet session if present, and clears `SecurityContext`.

### Role and Permission Rules

- Public: `GET /api/health`, `GET /api/auth/login-options`, logout endpoints, OPTIONS.
- Authenticated: `GET /api/auth/me` and all unmatched `/api/**`.
- Family reads allow `ADMIN`, `MEMBER`, `VIEWER`.
- Family mutations require `ADMIN`.
- Timeline/albums/notifications/memorial/kitchen reads allow `ADMIN`, `MEMBER`, `VIEWER`.
- Most content mutations allow `ADMIN` and `MEMBER`.
- Media and messages require `ADMIN` or `MEMBER`.
- WebSocket handshake allows all roles, but STOMP room subscriptions and `/app/**` sends block `VIEWER`.

### Domain Rules

- A family member is considered deceased when `deathDate` is non-null.
- Memorial profiles are derived from family members with a death date.
- Tributes can only be created for deceased members.
- Family relationship source and target must be different.
- Relationship types are `PARENT_CHILD`, `SPOUSE`, and `SIBLING`.
- Chat message body is required even for `IMAGE`/`EMOJI` message types.
- Media upload only accepts `image/*` and `video/*` content types.
- Cloudinary must be configured before upload works.

## ENVIRONMENT + INFRASTRUCTURE

### Required Backend Runtime

- Java 25.
- Maven wrapper from `mvnw` or `mvnw.cmd`.
- PostgreSQL.
- Optional Redis.

### Required Frontend Runtime

- Node `>=22 <24`.
- npm `>=10`.
- Use Node 22 for this project. Prior notes mention Node 24 caused build instability.

### Backend Environment Variables

| Variable | Purpose | Default or local value |
| --- | --- | --- |
| `DB_URL` | JDBC URL | `jdbc:postgresql://localhost:5432/hometree` |
| `DB_USERNAME` | DB username | `hometree` |
| `DB_PASSWORD` | DB password | `hometree` |
| `SERVER_PORT` | API port | `8080` |
| `CORS_ALLOWED_ORIGINS` | Comma-separated browser origins | `http://localhost:3000` |
| `SPRING_PROFILES_ACTIVE` | Enables `oauth`, `prod`, etc. | unset locally unless needed |
| `SPRING_DOCKER_COMPOSE_ENABLED` | Spring Docker Compose integration switch | usually `false` locally |
| `JWT_SECRET` | HMAC secret for JWT | must be strong in real envs |
| `JWT_TTL_MINUTES` | JWT lifetime | `120` |
| `OAUTH_SUCCESS_REDIRECT` | Frontend callback URL | `http://localhost:3000/auth/callback` |
| `SECURE_COOKIES` | Secure cookie flag | `false` local, `true` prod |
| `GOOGLE_CLIENT_ID` | Google OAuth client id | required for OAuth profile |
| `GOOGLE_CLIENT_SECRET` | Google OAuth secret | required for OAuth profile |
| `CLOUDINARY_CLOUD_NAME` | Cloudinary cloud name | required for upload |
| `CLOUDINARY_UPLOAD_PRESET` | Cloudinary upload preset | required for upload |
| `MAX_IMAGE_BYTES` | Image upload limit | `10485760` |
| `MAX_VIDEO_BYTES` | Video upload limit | `104857600` |
| `CACHE_TYPE` | Spring cache type | `simple`; prod profile uses Redis |
| `REDIS_HOST` | Redis host | `localhost`; prod `redis` |
| `REDIS_PORT` | Redis port | `6379` |

### Frontend Environment Variables

| Variable | Purpose |
| --- | --- |
| `NEXT_PUBLIC_API_BASE_URL` | Backend base URL. Use `http://localhost:8080` locally or empty/same-origin behind nginx. |
| `NEXT_PUBLIC_WS_URL` | SockJS endpoint. Use `http://localhost:8080/ws` locally or `/ws` behind nginx. |
| `NEXT_PUBLIC_GOOGLE_CLIENT_ID` | Display/config metadata for OAuth login screen. |

### Important Commands

```powershell
docker compose up -d postgres redis
.\mvnw.cmd spring-boot:run
.\mvnw.cmd test
.\mvnw.cmd -DskipTests package
```

```powershell
cd frontend
npm ci
npm run dev
npm run typecheck
npm run lint
npm run build
```

## KNOWN ISSUES

### Security and Privacy

- CSRF protection is disabled for `/api/**` while browser auth uses cookies.
- Homemade JWT implementation lacks issuer/audience claims, key rotation, and library hardening.
- Default `JWT_SECRET` is development-only and must never be used in production.
- OAuth-created users default to `MEMBER`; there is no invitation or approval gate.
- No role management UI/API exists.
- No account disable/ban/deactivate flow exists.
- No branch/member/album/chat privacy policy exists.
- `anyRequest().permitAll()` can accidentally expose future backend routes.
- Local secret files are present in the workspace but gitignored.

### Business Logic

- Many ownership fields are not populated from the authenticated principal.
- Notifications ignore recipients and list all notifications.
- Chat room participants are unused and not enforced.
- Marking a message seen is not tied to the current user.
- Timeline filters cannot be combined meaningfully because service precedence is `authorId`, then `year`, then `eventType`.
- Family relationship semantics are not validated beyond self-reference.
- Duplicate relationship creation relies on DB constraint failure instead of friendly service handling.
- No delete endpoints exist for most resources.

### Performance and Scalability

- Timeline, chat, notifications, family, and albums use offset pagination or unpaged list calls.
- Family tree frontend issues relationship queries for each member, which creates an API storm for large trees.
- Kitchen and notifications list all rows.
- Cloudinary upload builds the full multipart body in memory.
- Simple in-memory cache is default locally; prod switches to Redis but cache coverage is limited.

### UX and Frontend

- Mutation UI is shown to `VIEWER` users and then fails at the API layer.
- React Query retry predicate checks `error.message.includes("401")`; `ApiError.status` would be more reliable.
- React Query Devtools are included unconditionally.
- OAuth, logout, WebSocket, and upload flows need real browser/provider verification.
- Several screens lack richer empty/error/loading states.
- Profile page is read-only.

### Testing and Documentation

- Frontend has no unit/component/E2E tests.
- Backend service/controller coverage is thin outside JWT and WebSocket.
- Testcontainers PostgreSQL test is disabled.
- No OpenAPI/Swagger docs exist.
- Older docs under `docs/HOMETREE_ARCHITECTURE.md` and `docs/FRONTEND_ARCHITECTURE.md` contain stale auth/WebSocket direction; prefer this file and `PROJECT_CONTEXT.md`.

## NEXT DEVELOPMENT PRIORITIES

### Immediate Tasks

1. Rotate/confirm Google OAuth credentials and verify OAuth login in a real browser.
2. Verify `POST /auth/logout` clears the cookie after OAuth in a real browser.
3. Verify WebSocket chat connect/subscribe/send through both split localhost and nginx same-origin setups.
4. Re-run frontend `npm run typecheck`, `npm run lint`, and `npm run build` under Node 22.
5. Add role-aware frontend guards so `VIEWER` does not see mutation/chat controls.
6. Fix notification ownership: recipient-aware list/create/read.
7. Populate creator/author fields from authenticated principal for posts, albums, recipes, tributes, media, and notifications.

### Short-Term Roadmap

1. Add admin user/role management and invitation/onboarding flow.
2. Add service-level authorization for chat room participants and future private rooms.
3. Add friendly validation and conflict handling for duplicate family relationships.
4. Add delete endpoints where product-safe: recipes, tributes, albums, album media, posts, rooms/messages if desired.
5. Convert timeline, chat, and notifications to cursor pagination using existing V2 indexes.
6. Add controller/service tests for each feature package.
7. Add frontend tests for critical auth shell, family tree, timeline, albums upload states, and messenger.

### Long-Term Roadmap

1. Design and implement family governance: invitations, approvals, branch privacy, minor data rules, audit UI.
2. Wire `media_links` to support reusable media across albums, posts, memorials, family members, and recipes.
3. Wire `chat_message_reads` for per-user group read receipts.
4. Wire `family_member_closure` for fast ancestor/descendant queries and large-tree graph operations.
5. Add notification realtime delivery, badge counts, mark-all-read, and scheduling worker.
6. Add comments/reactions to timeline posts if that remains part of the social-network vision.
7. Add observability, backups, health checks, TLS automation, and secret management for production.

## AI ENGINEER NOTES

- Read `PROJECT_CONTEXT.md` after this file before making broad changes.
- Run `git status --short` before edits. The worktree may contain user or prior-session changes; never revert them without explicit permission.
- Keep Java 25, Spring Boot, Maven, PostgreSQL, Flyway, Next.js, React Query, Zustand, and the existing REST envelope unless explicitly asked to change architecture.
- Do not collapse backend and frontend into one runtime.
- Do not bypass `apiFetch<T>()` for JSON API calls unless changing the client contract intentionally.
- Do not edit old migrations for real deployments; append a new migration.
- Avoid casual rewrites of `SecurityConfig`, `JwtAuthenticationFilter`, `JwtService`, `OAuth2LoginSuccessHandler`, `WebSocketConfig`, `frontend/src/lib/api/client.ts`, and `frontend/src/lib/websocket/*`.
- When touching backend contracts, inspect the matching frontend feature API/hook/type files first.
- When touching frontend API clients, inspect the backend controller/service/DTO first.
- If adding a feature, populate authenticated ownership fields at the service layer from `Principal`.
- If adding mutation APIs, define role policy in `SecurityConfig` and consider CSRF implications.
- If adding chat behavior, decide whether room membership is enforced and write tests for it.
- If adding media behavior, decide whether to use legacy direct columns on `media_assets` or move toward `media_links`.
- If fixing WebSocket, test cookie-first OAuth flow and bearer fallback separately.
- If running frontend build, use Node 22 and capture logs because this project has had build-stall reports.
- Do not commit `.env`, credential JSON, `target/`, `frontend/node_modules/`, `frontend/.next/`, logs, or `tsconfig.tsbuildinfo`.

## FIRST 15 MINUTES CHECKLIST

1. Run `git status --short`.
2. Read `AI_HANDOFF.md`, then `PROJECT_CONTEXT.md`.
3. Read the controller, service, DTO, repository, and frontend feature files for the area being changed.
4. Check `SecurityConfig` before adding or moving endpoints.
5. Check migrations before changing entities.
6. Check frontend hooks and query keys before changing response shapes.
7. Run focused tests first, then broader verification if the change crosses module boundaries.

