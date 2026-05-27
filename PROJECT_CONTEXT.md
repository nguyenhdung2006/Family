# HomeTree Project Context

Last reviewed: 2026-05-27.

This document is the durable project overview and architecture report for HomeTree Digital Family Hub. It is written for long-term maintainers, future AI agents, and developers who need to continue development without a verbal handoff.

## SYSTEM OVERVIEW

HomeTree is a private, full-stack family hub. The product combines a family tree, memory timeline, album archive, messenger, memorial pages, family kitchen, notifications, profile shell, Google OAuth login, and media upload into one private web app.

The project solves a common family-data problem: photos, recipes, genealogy records, chat memories, and stories usually live across separate apps or individual devices. HomeTree centralizes those artifacts behind family-specific authentication and role-based access so a family can preserve relationships, memories, and rituals over time.

Primary target users:

| User type | Needs |
| --- | --- |
| Family administrator | Build and correct the family tree, manage members, eventually manage users and roles. |
| Family member | Share memories, upload media, chat, write tributes, preserve recipes, receive alerts. |
| Viewer or elder/guest | Read family content with limited or no write permissions. |
| Future maintainer | Safely extend privacy, governance, media reuse, scale, and deployment maturity. |

Core feature areas:

- Authentication and profile shell.
- Family tree and relationships.
- Memory timeline.
- Albums and media upload.
- Chat/messenger with STOMP realtime broadcasts.
- Memorial tribute pages.
- Kitchen recipes.
- In-app notifications.
- Dockerized local and production runtime.

High-level runtime flow:

```text
Browser
  -> Next.js App Router frontend
  -> REST calls through apiFetch with credentials
  -> nginx in production
  -> Spring Boot API
  -> JPA repositories
  -> PostgreSQL schema managed by Flyway

Browser
  -> SockJS/STOMP /ws
  -> Spring WebSocket/STOMP broker
  -> ChatService persistence and /topic/rooms/{roomId} broadcasts

Spring Boot
  -> Google OAuth when oauth profile is active
  -> Cloudinary upload API when media env vars are configured
  -> Redis cache when prod profile enables redis cache
```

Assumptions made from the codebase:

- The app is meant to be family-private, not public social media.
- OAuth is the intended first authentication provider.
- Admin and invitation governance is expected before real family production use, but not implemented yet.
- The schema intentionally anticipates larger data volume and more complex media/chat/tree behavior than the services currently expose.
- Deployment is intended as split web/API containers behind nginx.

## PROJECT IDEA AND BUSINESS LOGIC

### Main Purpose

HomeTree stores a family's living knowledge:

- Who people are, how they relate, and which side of the family they belong to.
- What events and memories happened, when, where, and who they involve.
- Which photos and videos belong in family albums.
- Which chat rooms keep family conversations going.
- Which family members should have memorial pages and tributes.
- Which recipes and elder notes should be preserved.
- Which notifications keep the family aware of birthdays, events, new memories, and messages.

### Main Workflows

#### Login and Session

1. User lands on the frontend.
2. `AppShell` calls `GET /api/auth/me`.
3. If unauthenticated, the frontend renders `LoginScreen`.
4. Login starts at `/oauth2/authorization/google`.
5. Spring Security handles Google OAuth.
6. `OAuth2LoginSuccessHandler` upserts `AppUser` by email, writes `HOMETREE_TOKEN`, and redirects to `/auth/callback`.
7. Frontend invalidates the current-user query and redirects home.
8. Later API calls authenticate through the HttpOnly cookie or optional bearer token.
9. Logout calls `POST /auth/logout`, clears token state, disconnects STOMP clients, clears React Query cache, and returns to `/login`.

#### Family Tree

1. Frontend loads family members through `/api/family/members`.
2. Frontend loads relationships per member through `/api/family/members/{id}/relationships`.
3. React Flow graph groups members by `generationLevel`.
4. Admin users can create/update members and relationships.
5. Relationship data is persisted as typed directed edges.

#### Timeline

1. Frontend loads memory posts with optional `year` or `eventType` filters.
2. Members can create posts with text, occurrence time, location, event type, and tagged member ids.
3. Backend validates tagged member ids and stores many-to-many rows in `memory_post_tagged_members`.
4. Timeline cache is evicted after create.

#### Albums and Media

1. User creates or selects an album.
2. User chooses an image/video file.
3. Frontend uploads through XHR to `/api/media/upload` for progress.
4. Backend validates Cloudinary config, content type, and file size.
5. Backend uploads to Cloudinary and returns URL/public id.
6. Frontend attaches the media to an album through `/api/albums/{albumId}/media`.
7. Album gallery loads `media_assets` by album id.

#### Chat and Realtime

1. User lists or creates chat rooms.
2. User selects a room and loads messages through REST.
3. `useRoomSocket` connects to `/ws`.
4. Backend derives the STOMP principal from the `HOMETREE_TOKEN` cookie during handshake or from bearer header during CONNECT.
5. Client subscribes to `/topic/rooms/{roomId}`.
6. REST send or STOMP send persists a message and broadcasts it to the room topic.
7. Frontend merges realtime messages into the React Query cache and removes optimistic duplicates.

#### Memorials

1. Backend lists family members where `deathDate` is non-null.
2. Frontend selects a memorial member.
3. Members/admins create or edit tribute stories.
4. Backend rejects tribute creation for living members.

#### Kitchen

1. Frontend lists all recipes.
2. Members/admins create or update recipes with title, description, ingredients, instructions, video URL, and elder notes.

#### Notifications

1. Frontend lists notifications and refetches every minute.
2. Members/admins can create notifications.
3. Users can mark individual notifications read.
4. Current implementation is global, not recipient-specific.

## TECH STACK

### Backend

| Area | Technology |
| --- | --- |
| Language | Java 25 |
| Framework | Spring Boot 3.5.14 |
| Build | Maven wrapper |
| HTTP | Spring Web MVC |
| Persistence | Spring Data JPA / Hibernate |
| Validation | Jakarta Validation |
| Security | Spring Security, OAuth2 Client |
| Auth token | Custom HS256 JWT service |
| Realtime | Spring WebSocket, STOMP, SockJS |
| Cache | Spring Cache, simple locally, Redis in prod profile |
| Database | PostgreSQL 16 |
| Migration | Flyway |
| External media API | Cloudinary unsigned upload endpoint through Java `HttpClient` |
| Tests | JUnit 5, Spring Boot Test, Spring Security Test, Mockito beans, Testcontainers dependency |

### Frontend

| Area | Technology |
| --- | --- |
| Runtime | Node `>=22 <24`, npm `>=10` |
| Framework | Next.js App Router |
| UI | React 19 |
| Language | TypeScript |
| Styling | Tailwind CSS 4 |
| Server state | TanStack React Query 5 |
| UI state | Zustand 5 |
| Forms | React Hook Form and Zod in timeline composer; manual forms elsewhere |
| Graph | `@xyflow/react` React Flow |
| Realtime client | `@stomp/stompjs`, `sockjs-client` |
| Animation | Framer Motion |
| Icons | Lucide React |
| Utilities | clsx, tailwind-merge, date-fns |

### Deployment and Infrastructure

| File | Purpose |
| --- | --- |
| `compose.yaml` | Local PostgreSQL and Redis. |
| `docker-compose.prod.yaml` | API, web, PostgreSQL, Redis, nginx production topology. |
| `Dockerfile` | Backend image using Eclipse Temurin Java 25. |
| `frontend/Dockerfile` | Next.js standalone image using Node 22. |
| `nginx/hometree.conf` | Reverse proxy for frontend, API, OAuth, logout, and WebSocket paths. |
| `.env.example` | Backend/local environment template. |
| `frontend/.env.example` | Frontend public environment template. |

### AI/ML Integrations

No AI or ML integration is implemented in the codebase. The project name and current features are family/social/network oriented, not AI-oriented.

## FOLDER STRUCTURE OVERVIEW

```text
D:\digital-family-hub
  README.md
  HELP.md
  AI_HANDOFF.md
  PROJECT_CONTEXT.md
  pom.xml
  compose.yaml
  docker-compose.prod.yaml
  Dockerfile
  nginx/hometree.conf
  docs/
  src/main/java/com/familyhub/digital_family_hub/
    auth/
    users/
    family/
    posts/
    albums/
    media/
    chat/
    memorials/
    kitchen/
    notifications/
    shared/
    config/
    system/
  src/main/resources/
    application.yaml
    application-dev.yaml
    application-oauth.yaml
    application-prod.yaml
    db/migration/
  src/test/java/com/familyhub/digital_family_hub/
  frontend/
    package.json
    next.config.ts
    src/app/
    src/components/
    src/features/
    src/lib/
    src/stores/
```

Important durable docs:

- `AI_HANDOFF.md`: current runbook and short-term development cautions.
- `PROJECT_CONTEXT.md`: this full architecture/report document.
- `docs/DATABASE_REVIEW.md`: database index and future-table notes.
- `docs/PROJECT_STATUS_AND_AI_BUILD_NOTES.md`: prior difficulty-ordered status notes, mostly still useful.
- `docs/HOMETREE_ARCHITECTURE.md` and `docs/FRONTEND_ARCHITECTURE.md`: older architecture notes; some auth/WebSocket statements are stale and should not override this document.

## DOMAIN MODEL

### Users

Entity: `users/AppUser.java`.

Table: `app_users`.

Fields:

- `id`, `createdAt`, `updatedAt` from `AuditableEntity`.
- `name`, `email`, `avatarUrl`, `birthday`, `role`.

Relationships:

- `FamilyMember.user` can link a family tree profile to an application user.
- `MemoryPost.author`, `Album.createdBy`, `MediaAsset.uploadedBy`, `ChatMessage.sender`, `InAppNotification.recipient`, `Recipe.createdBy`, and `MemorialTribute.author` refer to users.

Current gap:

- Most services do not populate these ownership fields from the authenticated principal.

### Family Members and Relationships

Entities:

- `family/FamilyMember.java`
- `family/FamilyRelationship.java`

Tables:

- `family_members`
- `family_relationships`

Enums:

- `FamilyBranch`: `PATERNAL`, `MATERNAL`
- `RelationshipType`: `PARENT_CHILD`, `SPOUSE`, `SIBLING`

Relationships:

- `FamilyMember` optionally has one `AppUser`.
- `FamilyRelationship` has many-to-one source and target family members.
- `MemoryPost` can tag many family members.
- `MediaAsset` can link to one family member.
- `MemorialTribute` belongs to one family member.

Lifecycle:

- Admin creates/updates members.
- Admin creates/updates typed relationships.
- Member is considered deceased when `deathDate != null`.
- Memorial pages are derived from deceased members.

Important constraints:

- V2 adds `chk_family_relationships_not_self`.
- V2 adds unique index `uq_family_relationship_edge_type`.
- No semantic validation exists for generation ordering, reciprocal spouse/sibling records, parent/child chronology, or impossible date ranges.

### Memory Posts

Entity: `posts/MemoryPost.java`.

Tables:

- `memory_posts`
- `memory_post_tagged_members`

Enum:

- `EventType`: `FAMILY_GATHERING`, `BIRTHDAY`, `WEDDING`, `TET`, `TRAVEL`, `MEMORIAL`, `EVERYDAY`, `OTHER`.

Lifecycle:

- List posts with page/size and optional year/event/author filter.
- Create post with text, occurred time, location, event type, and tagged members.

Current gaps:

- `author` is not set.
- No update/delete.
- No media attachment through `media_links`.
- Filters are not composable.

### Albums and Media

Entities:

- `albums/Album.java`
- `media/MediaAsset.java`

Tables:

- `albums`
- `media_assets`
- Future table `media_links`

Enums:

- `AlbumCategory`: `TET`, `WEDDING`, `TRAVEL`, `BIRTHDAY`, `MEMORIAL`, `EVERYDAY`, `OTHER`.
- `MediaType`: `IMAGE`, `VIDEO`.

Lifecycle:

- Create/update album.
- Upload a file to Cloudinary through `/api/media/upload`.
- Attach returned URL/public id to an album by creating a `MediaAsset`.
- View album media by album id.

Current gaps:

- `createdBy` and `uploadedBy` are not populated.
- `provider`, `width`, `height`, `duration_seconds`, `file_size_bytes`, `checksum`, and `metadata_json` from V2 are not mapped in JPA.
- `media_links` is not used.
- No delete, reorder, cover photo, media reuse, or provider cleanup.

### Chat

Entities:

- `chat/ChatRoom.java`
- `chat/ChatMessage.java`

Tables:

- `chat_rooms`
- `chat_room_members`
- `chat_messages`
- Future table `chat_message_reads`

Enums:

- `ChatRoomType`: `PRIVATE`, `GROUP`.
- `MessageType`: `TEXT`, `IMAGE`, `EMOJI`.

Lifecycle:

- Create/update/list rooms.
- List messages by room.
- Send message through REST or STOMP.
- Broadcast message to `/topic/rooms/{roomId}`.
- Mark message seen by setting `seenAt`.

Current gaps:

- Participants collection is unused.
- No room membership authorization.
- `PRIVATE` room does not have private-room behavior.
- Group read receipts are not implemented.
- No delete/edit message.
- Chat attachments are only `mediaUrl` strings.

### Memorials

Entity: `memorials/MemorialTribute.java`.

Tables:

- `memorial_tributes`
- Deceased memorial profiles derive from `family_members`.

Lifecycle:

- List deceased family members.
- List tributes for a deceased member.
- Create tribute only if the family member is deceased.
- Update tribute if tribute belongs to path member id.

Current gaps:

- `author` is not populated.
- No delete.
- No moderation.
- No media attachments.

### Kitchen

Entity: `kitchen/Recipe.java`.

Table: `recipes`.

Lifecycle:

- List all recipes.
- Create recipe.
- Update recipe.

Current gaps:

- `createdBy` is not populated.
- No delete.
- No search/filter/tags.
- No media links.

### Notifications

Entity: `notifications/InAppNotification.java`.

Table: `in_app_notifications`.

Enum:

- `NotificationType`: `BIRTHDAY`, `DEATH_ANNIVERSARY`, `FAMILY_EVENT`, `NEW_MEMORY`, `MESSAGE`.

Lifecycle:

- List all notifications.
- Create notification.
- Mark read by id.

Current gaps:

- `recipient` is not populated or filtered.
- Scheduled notifications are stored but not processed by a scheduler.
- No mark-all-read.
- No realtime notifications.
- No cursor pagination.

## REQUEST FLOW

### REST Request Lifecycle

```text
Frontend component
  -> React Query hook in frontend/src/features/<feature>/hooks.ts
  -> API function in frontend/src/features/<feature>/api.ts
  -> apiFetch in frontend/src/lib/api/client.ts
  -> Spring Security filter chain
  -> JwtAuthenticationFilter resolves bearer or HOMETREE_TOKEN
  -> Controller under src/main/java/.../<feature>
  -> DTO validation
  -> Service transaction
  -> Repository/JPA
  -> PostgreSQL
  -> Response DTO
  -> ApiResponse envelope
  -> apiFetch unwraps envelope.data
  -> React Query cache
  -> UI render
```

### WebSocket Request Lifecycle

```text
MessengerView
  -> useRoomSocket(roomId)
  -> createStompClient()
  -> SockJS(NEXT_PUBLIC_WS_URL)
  -> /ws handshake
  -> JwtCookieHandshakeInterceptor extracts HOMETREE_TOKEN
  -> JwtHandshakeHandler binds StompPrincipal
  -> STOMP CONNECT
  -> JwtStompChannelInterceptor authenticates CONNECT
  -> SUBSCRIBE /topic/rooms/{roomId}
  -> SEND /app/rooms/{roomId} or REST POST
  -> ChatService.sendMessage
  -> chat_messages insert
  -> SimpMessagingTemplate.convertAndSend
  -> frontend query cache merge
```

## AUTHENTICATION + SECURITY FLOW

### Auth Mechanism

Files:

- `src/main/java/com/familyhub/digital_family_hub/config/SecurityConfig.java`
- `src/main/java/com/familyhub/digital_family_hub/auth/JwtAuthenticationFilter.java`
- `src/main/java/com/familyhub/digital_family_hub/auth/JwtService.java`
- `src/main/java/com/familyhub/digital_family_hub/auth/OAuth2LoginSuccessHandler.java`
- `src/main/java/com/familyhub/digital_family_hub/auth/AuthController.java`
- `src/main/java/com/familyhub/digital_family_hub/auth/AuthLogoutController.java`
- `src/main/java/com/familyhub/digital_family_hub/auth/AuthLogoutHandler.java`

JWT behavior:

- Custom JWT has `alg=HS256`, `typ=JWT`.
- Payload includes `sub`, `name`, `role`, `iat`, `exp`.
- Signed with HMAC SHA-256 using `hometree.auth.jwt-secret`.
- Validation checks structure, signature, expiration, and role enum.
- No issuer, audience, not-before, kid, revocation list, or rotation mechanism.

Cookie behavior:

- Cookie name: `HOMETREE_TOKEN`.
- HttpOnly: yes.
- Secure: controlled by `hometree.auth.secure-cookies`.
- SameSite: `Lax`.
- Path: `/`.
- OAuth success max age is currently fixed to 2 hours.
- JWT TTL is configurable; keep cookie max age and JWT TTL aligned if TTL changes.

Security policy:

| Route | Access |
| --- | --- |
| `GET /api/health` | Public |
| `GET /api/auth/login-options` | Public |
| `POST /auth/logout`, `POST /api/auth/logout` | Public |
| `OPTIONS /**` | Public |
| `GET /api/family/**` | `ADMIN`, `MEMBER`, `VIEWER` |
| `POST/PUT /api/family/**` | `ADMIN` |
| `GET /api/timeline/**`, `/api/albums/**`, `/api/notifications/**`, `/api/memorials/**`, `/api/kitchen/**` | `ADMIN`, `MEMBER`, `VIEWER` |
| Mutating timeline/albums/notifications/memorials/kitchen | generally `ADMIN`, `MEMBER` |
| `/api/media/**`, `/api/messages/**` | `ADMIN`, `MEMBER` |
| `/api/**` unmatched | authenticated |
| `/ws`, `/ws/**` | `ADMIN`, `MEMBER`, `VIEWER` handshake |
| Other unmatched routes | public because of `.anyRequest().permitAll()` |

Security concerns:

- CSRF is ignored for API routes while cookie auth is used.
- No per-resource ownership/participant checks in services.
- No admin management/invitation gate.
- Viewer mutation controls are not hidden in the frontend.
- Any future non-API backend route may be public unless explicitly secured.

## DATABASE CONTEXT

### Migration Strategy

Flyway is enabled and configured at `classpath:db/migration`.

Default JPA setting:

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate
```

Rules:

- PostgreSQL/Flyway is the schema source of truth.
- Do not depend on Hibernate auto-mutation.
- Do not edit already-applied migrations for real environments.
- Add new migrations for schema changes.
- Keep additive/backward-compatible migrations whenever possible.

### Schema Summary

| Table | Purpose | Key relationships |
| --- | --- | --- |
| `app_users` | Authenticated users and roles | Referenced by family/user links, authors, senders, recipients |
| `family_members` | Genealogy person records | Optional `user_id`, memorial source, tags, media links |
| `family_relationships` | Directed typed family edges | Source and target family members |
| `memory_posts` | Timeline posts | Optional author |
| `memory_post_tagged_members` | Post/member tags | Many-to-many post to family member |
| `albums` | Album metadata | Optional creator |
| `media_assets` | Uploaded/external media records | Optional album/post/member/uploader |
| `chat_rooms` | Room metadata | Type and optional branch |
| `chat_room_members` | Future participant membership | Many-to-many room to user, not enforced |
| `chat_messages` | Room messages | Room and optional sender |
| `in_app_notifications` | Notification records | Optional recipient |
| `recipes` | Family recipes | Optional creator |
| `memorial_tributes` | Stories for deceased members | Member and optional author |
| `media_links` | Future reusable media ownership | Asset to arbitrary owner |
| `chat_message_reads` | Future per-user read receipts | Message/user/read time |
| `family_member_closure` | Future tree traversal acceleration | Ancestor/descendant/depth |

### Indexing Assumptions

V1 adds baseline lookup indexes. V2 adds:

- Family branch/generation indexes.
- Relationship source/target/type indexes.
- Feed cursor indexes on timeline.
- Album/media chronological indexes.
- Chat cursor indexes.
- Notification unread/scheduled partial indexes.
- Reverse lookup indexes for future media links, read receipts, and closure-table queries.

The schema is better prepared for scale than the current API layer. Cursor indexes are present, but APIs still mostly expose offset pagination.

### Transaction Behavior

- Services annotate mutating methods with `@Transactional`.
- Read methods generally use `@Transactional(readOnly = true)`.
- DTO mapping often accesses lazy relationships inside transactions, which is safe in current service methods.
- `spring.jpa.open-in-view=false`, so lazy access must stay inside service transactions.

## API CONTEXT

### Endpoint Inventory

| Method | Path | Purpose |
| --- | --- | --- |
| `GET` | `/api/health` | Service health probe |
| `GET` | `/api/auth/me` | Current authenticated user |
| `GET` | `/api/auth/login-options` | Login metadata |
| `POST` | `/auth/logout` | Main logout route |
| `POST` | `/api/auth/logout` | API-compatible logout |
| `GET` | `/api/family/members` | List members |
| `POST` | `/api/family/members` | Create member |
| `GET` | `/api/family/members/{id}` | Get member |
| `PUT` | `/api/family/members/{id}` | Update member |
| `GET` | `/api/family/branches/{branch}/members` | List members by branch |
| `GET` | `/api/family/members/{id}/relationships` | Relationships touching member |
| `POST` | `/api/family/relationships` | Create relationship |
| `PUT` | `/api/family/relationships/{id}` | Update relationship |
| `GET` | `/api/timeline/posts` | List/filter timeline posts |
| `POST` | `/api/timeline/posts` | Create timeline post |
| `GET` | `/api/albums` | List/filter albums |
| `POST` | `/api/albums` | Create album |
| `PUT` | `/api/albums/{albumId}` | Update album |
| `GET` | `/api/albums/{albumId}/media` | List album media |
| `POST` | `/api/albums/{albumId}/media` | Attach media to album |
| `POST` | `/api/media/upload` | Upload image/video to Cloudinary |
| `GET` | `/api/messages/rooms` | List chat rooms |
| `POST` | `/api/messages/rooms` | Create chat room |
| `PUT` | `/api/messages/rooms/{roomId}` | Update chat room |
| `GET` | `/api/messages/rooms/{roomId}` | List room messages |
| `POST` | `/api/messages/rooms/{roomId}` | Send room message |
| `PATCH` | `/api/messages/{messageId}/seen` | Mark message seen |
| `GET` | `/api/memorials` | List deceased family members |
| `GET` | `/api/memorials/{memberId}/tributes` | List tributes |
| `POST` | `/api/memorials/{memberId}/tributes` | Create tribute |
| `PUT` | `/api/memorials/{memberId}/tributes/{tributeId}` | Update tribute |
| `GET` | `/api/kitchen/recipes` | List recipes |
| `POST` | `/api/kitchen/recipes` | Create recipe |
| `PUT` | `/api/kitchen/recipes/{recipeId}` | Update recipe |
| `GET` | `/api/notifications` | List notifications |
| `POST` | `/api/notifications` | Create notification |
| `PATCH` | `/api/notifications/{id}/read` | Mark notification read |
| STOMP | `/app/rooms/{roomId}` | Send chat message over WebSocket |
| STOMP | `/topic/rooms/{roomId}` | Chat room broadcast subscription |

### Validation Rules

Backend DTO validation:

- Family member: `fullName` required, branch required, generation level minimum 0.
- Relationship: source, target, type required; notes max 2000.
- Timeline post: text required max 8000, occurredAt required, eventType required.
- Album: title required, category required.
- Album media attach: URL required, mediaType required.
- Chat room: name required, type required.
- Chat message: type required, body required max 4000.
- Tribute: title and story required.
- Recipe: title, ingredients, instructions required.
- Notification: type, title, body required.
- Media upload: file must be non-empty image/video and within configured byte limits.

Missing validations:

- Negative `page` values and invalid pagination parameters.
- Birth date before death date.
- URL format validation.
- Duplicate relationship friendly conflict.
- Relationship semantics and graph cycle rules.
- Chat room membership and private room constraints.
- Notification recipient.
- Creator/author ownership.

## CONFIGURATION CONTEXT

### Spring Profiles

`application.yaml` is the base config:

- App name: `hometree`.
- DB URL/user/password from env with local defaults.
- JPA validate mode.
- Flyway enabled.
- Cache type from env, default simple.
- Redis host/port from env.
- CORS allowed origins from env.
- Auth, JWT, OAuth redirect, secure cookie, Cloudinary, and upload size config.

`application-dev.yaml`:

- Simple cache.
- Show SQL.
- Insecure cookies for local dev.

`application-oauth.yaml`:

- Google OAuth client registration from env.
- Scopes: `openid`, `profile`, `email`.

`application-prod.yaml`:

- Redis cache.
- Hide SQL.
- Secure cookies.

### Docker Config

Local `compose.yaml`:

- `postgres:16-alpine`, port `5432`.
- `redis:7-alpine`, port `6379`.
- Named volumes for both.

Production `docker-compose.prod.yaml`:

- `api`: built from root, profile `prod,oauth`, exposes `8080`.
- `web`: built from `frontend`, exposes `3000`.
- `postgres`: internal DB.
- `redis`: internal cache.
- `nginx`: publishes `80` and `443`, mounts `nginx/hometree.conf`.

Nginx config:

- `/` to `web:3000`.
- `/api/`, `/auth/`, `/oauth2/`, `/login/oauth2/`, `/ws` to `api:8080`.
- `/ws` includes websocket upgrade headers.

Production gaps:

- TLS files/certbot/ACME automation absent.
- No health checks in Compose.
- No backup/restore scripts.
- No secret manager integration.
- No logs/metrics/tracing.

## TESTING CONTEXT

Existing tests:

| File | Coverage |
| --- | --- |
| `DigitalFamilyHubApplicationTests.java` | Context load with repository mocks and no DB auto-config. |
| `JwtServiceTest.java` | JWT structure, validation, expiration. |
| `JwtAuthenticationFilterTest.java` | Cookie auth, missing cookie, malformed cookie. |
| `WebSocketIntegrationTest.java` | STOMP connect, subscribe, app send, viewer rejection. |
| `PostgresContainerIntegrationTest.java` | Disabled Docker/Postgres startup smoke test. |

Missing coverage:

- Controller tests for every feature.
- Service tests for business rules.
- Security route matrix tests for REST endpoints.
- Repository/migration integration tests against PostgreSQL.
- Cloudinary service tests with mocked HTTP.
- Frontend unit/component tests.
- Playwright/E2E coverage for auth shell, CRUD flows, upload, messenger, and mobile navigation.
- Production Compose/nginx smoke tests.

Verification commands:

```powershell
.\mvnw.cmd -version
.\mvnw.cmd test
.\mvnw.cmd -DskipTests package
```

```powershell
cd frontend
npm ci
npm run typecheck
npm run lint
npm run build
```

Known caveats:

- Use Node 22.
- Prior frontend build log only showed the Next.js banner and may indicate a local build stall.
- OAuth, WebSocket browser behavior, and Cloudinary require real provider/browser testing.

## DEPLOYMENT CONTEXT

### Local Development

```powershell
docker compose up -d postgres redis
Copy-Item .env.example .env
.\mvnw.cmd spring-boot:run
```

Backend health:

```powershell
Invoke-RestMethod http://localhost:8080/api/health
```

Frontend:

```powershell
cd frontend
Copy-Item .env.example .env.local
npm ci
npm run dev
```

Default URLs:

- Backend: `http://localhost:8080`
- Frontend: `http://localhost:3000`
- WebSocket/SockJS: `http://localhost:8080/ws`

### OAuth Development

```powershell
$env:SPRING_PROFILES_ACTIVE="oauth"
$env:GOOGLE_CLIENT_ID="your-client-id"
$env:GOOGLE_CLIENT_SECRET="your-client-secret"
.\mvnw.cmd spring-boot:run
```

Google redirect URI must match the Spring OAuth callback used by Spring Security, typically:

```text
http://localhost:8080/login/oauth2/code/google
```

Frontend callback after success:

```text
http://localhost:3000/auth/callback
```

### Production Deployment

Intended command:

```powershell
docker compose -f docker-compose.prod.yaml up -d --build
```

Required production values:

- `DB_USERNAME`
- `DB_PASSWORD`
- `JWT_SECRET`
- `CORS_ALLOWED_ORIGINS`
- `GOOGLE_CLIENT_ID`
- `GOOGLE_CLIENT_SECRET`
- `NEXT_PUBLIC_API_BASE_URL` if not same-origin
- `NEXT_PUBLIC_WS_URL`
- `CLOUDINARY_CLOUD_NAME`
- `CLOUDINARY_UPLOAD_PRESET`

Production readiness blockers:

- Add TLS termination and secure cookie verification behind HTTPS.
- Store secrets outside env files.
- Add DB backups.
- Add health checks.
- Add observability.
- Verify OAuth redirect, logout cookie clearing, and WebSocket through nginx.

## FEATURE STATUS AUDIT

### COMPLETED FEATURES

| Feature | What works | Related files | Quality | Edge cases |
| --- | --- | --- | --- | --- |
| Health check | Returns service status and timestamp. | `system/HealthController.java` | Simple and good. | Could add build/version info. |
| API envelope | Success and error response shapes exist. | `shared/api/*` | Good foundation. | Needs more exception mappings. |
| OAuth/JWT login foundation | OAuth profile, success handler, JWT cookie, current user, login options. | `auth/*`, `SecurityConfig.java` | Good MVP design. | Needs browser verification, invitation gate, token hardening. |
| Logout | Clears cookie/session/security context and frontend clears state. | `AuthLogoutHandler.java`, `features/auth/*`, `app-shell.tsx` | Good. | Needs OAuth browser verification. |
| Family member CRUD subset | List/get/create/update members. | `family/*`, `components/family-tree/*` | Useful MVP. | No delete, date rules, role-aware UI. |
| Family relationship CRUD subset | List/create/update relationships. | `family/*`, `features/family/*` | Basic graph support. | No semantic validation, duplicate conflict handling weak. |
| Timeline create/list | Feed, composer, event/year filters. | `posts/*`, `components/timeline/*` | Good first slice. | No author, edit/delete, media, comments. |
| Album create/update/list | Album metadata and frontend cards/forms. | `albums/*`, `components/albums/*` | Good first slice. | No delete, cover, ownership. |
| Media upload to Cloudinary | Validates content type/size/config and uploads externally. | `media/*`, `features/albums/api.ts` | Good provider abstraction. | Full body in memory, no provider metadata persistence, requires env. |
| Album media attach/list | Creates `MediaAsset` for album and displays gallery/slideshow. | `AlbumService.java`, `albums-gallery.tsx` | Usable. | No reorder/delete/reuse. |
| Chat REST | Rooms, messages, send, mark seen. | `chat/*`, `messenger-view.tsx` | Usable MVP. | No participant authorization, read receipts weak. |
| Chat WebSocket | `/ws`, STOMP auth, room topic broadcasts, `/app` send. | `WebSocketConfig.java`, `ChatWebSocketController.java`, `lib/websocket/*` | Stronger than typical MVP. | Needs real browser/nginx verification. |
| Memorial tributes | List deceased members, list/create/update tributes. | `memorials/*`, `memorial-view.tsx` | Good simple flow. | No author/delete/moderation/media. |
| Kitchen recipes | List/create/update recipes. | `kitchen/*`, `kitchen-view.tsx` | Solid simple CRUD. | No delete/search/ownership. |
| Notifications | List/create/mark read and polling frontend. | `notifications/*`, `notifications-view.tsx` | Basic. | Recipient model is not used. |
| Docker topology | Local infra and production service split. | `compose.yaml`, `docker-compose.prod.yaml`, `Dockerfile`, `frontend/Dockerfile`, `nginx/hometree.conf` | Good skeleton. | No TLS/health/backups/observability. |

### PARTIALLY COMPLETED FEATURES

| Feature | What exists | Missing | Blockers | Estimated remaining work |
| --- | --- | --- | --- | --- |
| Production auth | OAuth/JWT/cookie implemented. | Credential rotation, invitation gate, role management, CSRF plan. | Google Console and policy decisions. | 2-5 days for secure MVP, more for governance. |
| Role security | HTTP route matrix and STOMP viewer blocking. | Frontend role gating, resource-level checks. | Need product privacy policy. | 1-3 days for basic guards, longer for privacy. |
| User/admin management | `AppUser` and roles. | Admin API/UI, disable users, role edits, invite flow. | Need initial admin/bootstrap policy. | 3-7 days. |
| Family graph at scale | Members/relationships and React Flow. | Closure table, better layout, delete, import/export. | Complex genealogy cases. | 1-2 weeks depending scope. |
| Timeline social behavior | Posts and fake Love/Comment buttons. | Reactions, comments, author, media, edit/delete. | Need product decision. | 3-10 days. |
| Media model | Upload and direct album/post/member columns. | `media_links`, delete, reuse, metadata, cleanup. | Migration and UI decisions. | 1-2 weeks. |
| Chat product | Rooms/messages/realtime. | Participants, private rooms, per-user reads, attachments. | Authorization model. | 1-2 weeks. |
| Notifications | Storage and polling. | Recipients, realtime, scheduler, mark-all, badges. | Need notification delivery rules. | 3-7 days. |
| Testing | JWT/STOMP tests and scripts. | Broad backend and frontend coverage. | Environment setup. | Ongoing; 1 week for meaningful baseline. |
| Deployment | Docker/nginx files. | TLS, secrets, backups, health checks, monitoring. | Server/domain access. | 2-5 days for basic production hardening. |

### MISSING FEATURES

Features that should exist based on the project idea but are not implemented:

- Invitation/onboarding/approval flow.
- Admin user and role management.
- Account disable/deactivate.
- Family privacy policy by branch, member, album, room, and memorial.
- Audit log persistence and admin audit UI.
- Profile edit.
- Family member/relationship delete.
- GEDCOM or CSV import/export for family tree.
- Better graph layout for large/complex families.
- Timeline edit/delete, comments, reactions, and media attachments.
- Album delete, media delete, album cover, media reorder, and media reuse.
- Cloudinary delete/cleanup for removed assets.
- Chat participant management, private-room membership, message delete/edit, attachments, per-user read receipts.
- Memorial tribute delete, moderation, author display, and media attachments.
- Recipe delete, search/filter, categories/tags, media attachments.
- Notification recipients, mark-all-read, unread counts, scheduler, realtime delivery.
- OpenAPI/Swagger documentation.
- Frontend automated tests and E2E tests.
- CI pipeline.
- Production TLS, health checks, backups, metrics, logs, alerts, and secret management.

## PROBLEMS AND RISKS

### TODO/FIXME Comments

Known TODO-style comments are in environment templates:

- `.env.example`: replace Google OAuth values and configure Cloudinary.
- `frontend/.env.example`: replace Google OAuth client id when OAuth is enabled.

Known disabled test:

- `PostgresContainerIntegrationTest.java` is disabled until Docker is available.

### Dead or Underused Code

- `media_links` table is unused.
- `chat_message_reads` table is unused.
- `family_member_closure` table is unused.
- `ChatRoom.participants` and `chat_room_members` are unused for behavior.
- `AppUser` ownership relationships are often not populated.
- `AuditLogService` is only a logger, not persisted or surfaced.
- Some repository methods return lists but are not used after pageable alternatives were added.

### Duplicated or Inconsistent Code

- `AuthService` and `OAuth2LoginSuccessHandler` both contain OAuth user upsert logic.
- Many frontend forms repeat manual trim/validate/submit patterns.
- Backend services repeat `normalizeSize`.
- Frontend API upload URL builder duplicates logic from `apiFetch`.
- Frontend uses React Hook Form/Zod only for timeline composer; other forms validate manually.

### Security Issues

- CSRF risk for cookie-authenticated API mutations.
- Homemade JWT should be replaced or hardened with a proven library before production.
- No resource-level authorization for chat, notifications, ownership, or private content.
- OAuth-created users default to `MEMBER`.
- No account lifecycle management.
- `anyRequest().permitAll()` is risky for future routes.
- Secret files are present locally; they are gitignored but still operationally sensitive.

### Performance Issues

- Offset pagination will degrade for deep pages.
- Kitchen/notifications list all records.
- Family graph does N+1 relationship API loading.
- Chat rooms and messages lack participant-filtered queries.
- Cloudinary upload buffers entire multipart payload in memory.
- Cache coverage is partial and invalidation is coarse.

### Bad Practices or Maintainability Risks

- Services do not consistently resolve authenticated user/ownership.
- API list responses return bare arrays without total counts or cursors.
- Some DB constraints can surface as generic 500 errors.
- Frontend role behavior relies on backend failures instead of shaping the UI.
- Some older docs are stale and can mislead future maintainers.

### Possible Bugs

- Negative `page` can throw and become 500.
- Duplicate family relationship can throw DB constraint exception and become 500.
- Notification list leaks all notifications to any allowed reader.
- Any member/admin can read or send to any chat room.
- `markSeen` can mark any message read without checking current user or room access.
- Timeline `authorId`, `year`, and `eventType` filters do not combine.
- Album media list for a nonexistent album returns empty instead of 404.
- `createdBy`, `author`, `uploadedBy`, `recipient` nulls make ownership features impossible without backfill.
- React Query retry logic may retry unauthorized errors because it checks error message rather than `ApiError.status`.

## PROJECT READINESS EVALUATION

| Area | Score | Explanation |
| --- | ---: | --- |
| MVP readiness | 7/10 | The app has real end-to-end feature slices across backend and frontend. It is usable for demo/local family data with configured OAuth and Cloudinary. |
| Production readiness | 3/10 | Lacks CSRF strategy, invitation governance, resource-level auth, TLS setup, secret management, backups, health checks, observability, and provider verification. |
| Scalability | 5/10 | PostgreSQL schema/indexes are thoughtful, but APIs still use offset/unpaged lists, frontend has N+1 family relationship loading, and cache usage is limited. |
| Maintainability | 7/10 | Feature packages, DTOs, services, repositories, and frontend feature folders are clear. Main risks are ownership gaps, duplicated auth/upsert logic, and stale docs. |
| Code quality | 6/10 | Clean simple code overall, but validation and authorization depth are thin, custom JWT is risky, and many future schema elements are not wired. |
| UX completeness | 6/10 | Broad screens exist and are pleasant. Missing role-aware controls, richer empty/error states, profile edit, delete flows, and production-grade auth/upload/realtime polish. |
| Test readiness | 4/10 | Good JWT/STOMP start, but backend feature coverage and frontend/E2E coverage are sparse. |
| Deployment readiness | 4/10 | Docker/nginx topology exists, but operational hardening is missing. |

## TECHNICAL DOCUMENTATION

### Setup Instructions

Backend prerequisites:

- Java 25.
- Maven wrapper.
- Docker.

Frontend prerequisites:

- Node 22.
- npm 10+.

Local infrastructure:

```powershell
docker compose up -d postgres redis
```

Backend env:

```powershell
Copy-Item .env.example .env
```

Backend run:

```powershell
.\mvnw.cmd spring-boot:run
```

Frontend env:

```powershell
cd frontend
Copy-Item .env.example .env.local
```

Frontend install/run:

```powershell
npm ci
npm run dev
```

### Environment Variables Needed

Backend:

```text
DB_URL
DB_USERNAME
DB_PASSWORD
CORS_ALLOWED_ORIGINS
SPRING_PROFILES_ACTIVE
GOOGLE_CLIENT_ID
GOOGLE_CLIENT_SECRET
OAUTH_SUCCESS_REDIRECT
SECURE_COOKIES
JWT_SECRET
JWT_TTL_MINUTES
CLOUDINARY_CLOUD_NAME
CLOUDINARY_UPLOAD_PRESET
MAX_IMAGE_BYTES
MAX_VIDEO_BYTES
REDIS_HOST
REDIS_PORT
CACHE_TYPE
```

Frontend:

```text
NEXT_PUBLIC_API_BASE_URL
NEXT_PUBLIC_WS_URL
NEXT_PUBLIC_GOOGLE_CLIENT_ID
```

### Important Commands

```powershell
.\mvnw.cmd test
.\mvnw.cmd -DskipTests package
cd frontend
npm run typecheck
npm run lint
npm run build
docker compose -f docker-compose.prod.yaml up -d --build
```

## ROADMAP

### Immediate Next Tasks

1. Verify OAuth login with rotated/current credentials.
2. Verify logout and cookie clearing in browser.
3. Verify WebSocket chat in browser and through nginx.
4. Fix frontend production build if it still stalls.
5. Add role-aware frontend UI guards.
6. Fix notification recipient scoping.
7. Populate creator/author/uploader fields from authenticated principal.

### Short-Term Improvements

1. Add admin user management and invitation flow.
2. Add service-level room participant authorization.
3. Add delete endpoints and UI for low-risk resources.
4. Add cursor pagination for timeline, chat, and notifications.
5. Add backend controller/service tests for all features.
6. Add frontend component/E2E test baseline.
7. Add OpenAPI documentation.

### Long-Term Improvements

1. Full family privacy/governance model.
2. Media reuse through `media_links`.
3. Group read receipts through `chat_message_reads`.
4. Large-tree traversal and analytics through `family_member_closure`.
5. Realtime notifications and scheduling.
6. Production observability, backups, TLS automation, and secret manager integration.
7. CI/CD with test/build/deploy gates.

### Refactoring Suggestions

- Extract OAuth upsert logic into one service used by both `/me` and success handler.
- Add a common pagination validator/helper.
- Add authenticated user resolver service.
- Add ownership population methods per feature.
- Add service-level authorization helpers for current user role and resource access.
- Consolidate frontend form handling on React Hook Form/Zod or a consistent lightweight pattern.
- Move upload URL construction to a shared frontend helper.
- Replace custom JWT implementation with a standard JOSE/JWT library or harden it substantially.

### Deployment Recommendations

- Use HTTPS only in production and verify secure cookies.
- Set a strong `JWT_SECRET` from a secret manager.
- Do not store `.env` or credential JSON in the repo directory on production hosts.
- Add Compose health checks for api, web, postgres, redis, nginx.
- Add PostgreSQL backup/restore automation.
- Add centralized logs and metrics.
- Add nginx `client_max_body_size` aligned with media limits.
- Verify Cloudinary upload preset restrictions.
- Add a staging environment for OAuth/WebSocket/nginx tests.

## FINAL SUMMARY

HomeTree is a strong full-stack MVP foundation for a private family social network: the backend and frontend are coherently structured, the schema anticipates future scale, and most major product areas already have usable vertical slices. Its biggest strengths are the clear feature-module architecture, Flyway/PostgreSQL foundation, broad Next.js UI coverage, and a thoughtful cookie-first OAuth/STOMP direction. Its biggest weaknesses are production security/governance gaps, missing ownership and resource-level authorization, unfinished future schema wiring, thin tests, and unverified provider/runtime flows. The highest priority next step is to harden and verify authentication end to end, then immediately add user/role/invitation governance and recipient/participant ownership rules before real family data is trusted to the system.

