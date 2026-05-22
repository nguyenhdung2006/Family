# HomeTree Digital Family Hub

HomeTree is a private family social network backend for genealogy, memories, albums, chat, memorial pages, family recipes, and in-app notifications.

This repository currently contains the Spring Boot backend foundation. The existing workspace was already a Java/Spring project, so the implementation keeps that production-grade stack instead of introducing an uninstalled Node/Nest workspace without dependencies.

## Run Locally

```powershell
docker compose up -d postgres
.\mvnw.cmd spring-boot:run
```

Health check:

```powershell
Invoke-RestMethod http://localhost:8080/api/health
```

## Google OAuth

OAuth settings are isolated in `application-oauth.yaml` so local tests and unauthenticated development do not fail when credentials are absent.

```powershell
$env:SPRING_PROFILES_ACTIVE="oauth"
$env:GOOGLE_CLIENT_ID="your-client-id"
$env:GOOGLE_CLIENT_SECRET="your-client-secret"
.\mvnw.cmd spring-boot:run
```

Login starts at `/oauth2/authorization/google`, and the current session can be checked at `/api/auth/me`.

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
.\mvnw.cmd test
```
