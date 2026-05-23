# HomeTree Digital Family Hub

HomeTree is a private family social network backend for genealogy, memories, albums, chat, memorial pages, family recipes, and in-app notifications.

The project is a Spring Boot backend plus a Next.js frontend. Keep the current stack and API contracts intact: Java is fixed at 25, the backend uses Maven, PostgreSQL, Flyway, optional Redis cache, Google OAuth/JWT cookies, Cloudinary media upload, and STOMP/SockJS chat. The frontend uses Next.js, React Query, Zustand, and the existing feature-based API clients.

## Requirements

- Java 25
- Maven through the checked-in wrapper (`mvnw` / `mvnw.cmd`)
- Node 22 and npm 10+ for the frontend
- Docker for local PostgreSQL/Redis and production compose

## Run Locally

Start local infrastructure:

```powershell
docker compose up -d postgres redis
```

Copy the backend environment template if needed and keep local secrets out of Git:

```powershell
Copy-Item .env.example .env
```

Run the backend:

```powershell
.\mvnw.cmd spring-boot:run
```

Health check:

```powershell
Invoke-RestMethod http://localhost:8080/api/health
```

Run the frontend:

```powershell
cd frontend
Copy-Item .env.example .env.local
npm ci
npm run dev
```

Default local endpoints:

- Backend API: `http://localhost:8080`
- Frontend: `http://localhost:3000`
- WebSocket/STOMP SockJS endpoint: `http://localhost:8080/ws`

## Google OAuth

OAuth settings are isolated in `application-oauth.yaml` so local tests and unauthenticated development do not fail when credentials are absent.

```powershell
$env:SPRING_PROFILES_ACTIVE="oauth"
$env:GOOGLE_CLIENT_ID="your-client-id"
$env:GOOGLE_CLIENT_SECRET="your-client-secret"
.\mvnw.cmd spring-boot:run
```

Login starts at `/oauth2/authorization/google`, and the current session can be checked at `/api/auth/me`.

## Production Docker

The production compose file preserves the split services:

- `api`: Spring Boot backend built from the root `Dockerfile` with Eclipse Temurin Java 25
- `web`: Next.js standalone server built from `frontend/Dockerfile` with Node 22
- `postgres`: PostgreSQL 16
- `redis`: Redis 7
- `nginx`: reverse proxy for `/`, `/api/`, `/oauth2/`, `/login/oauth2/`, and `/ws/`

Required production environment values include `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `CORS_ALLOWED_ORIGINS`, `GOOGLE_CLIENT_ID`, and `GOOGLE_CLIENT_SECRET`. Cloudinary uploads require `CLOUDINARY_CLOUD_NAME` and `CLOUDINARY_UPLOAD_PRESET`.

## Implemented Backend Surfaces

- `GET /api/health`
- `GET|POST /api/family/members`
- `GET /api/family/branches/{PATERNAL|MATERNAL}/members`
- `POST /api/family/relationships`
- `GET|POST /api/timeline/posts`
- `GET|POST /api/albums`
- `GET|POST /api/albums/{albumId}/media`
- `GET|POST /api/messages/rooms`
- `GET|POST /api/messages/rooms/{roomId}`
- `GET|POST /api/kitchen/recipes`
- `GET /api/memorials`
- `GET|POST /api/memorials/{memberId}/tributes`
- `GET|POST /api/notifications`
- `PATCH /api/notifications/{id}/read`

WebSocket/STOMP endpoint: `/ws`, with room broadcasts on `/topic/rooms/{roomId}`.

## Verification

```powershell
.\mvnw.cmd -version
.\mvnw.cmd test
.\mvnw.cmd -DskipTests package
cd frontend
npm ci
.\node_modules\.bin\node.cmd node_modules\next\dist\bin\next build
```

Generated outputs such as `target/`, `frontend/node_modules/`, `frontend/.next/`, and local environment or credential files are intentionally ignored and should not be committed.
