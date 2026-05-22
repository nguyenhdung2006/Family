# HomeTree Architecture Notes

## Backend Modules

- `auth`: Google OAuth session entry point and current-user probe.
- `users`: application user identity and role model.
- `family`: graph-like family member and relationship model.
- `posts`: memory timeline posts with event and year filtering.
- `albums` and `media`: media archive, albums, and external storage references.
- `chat`: room/message persistence plus STOMP broadcast support.
- `memorials`: deceased-member tribute pages with messaging kept separate.
- `kitchen`: family recipe archive with elder notes and video links.
- `notifications`: in-app notification storage and read state.

## Data Modeling Choices

The genealogy tree is modeled relationally as `family_members` plus typed edges in `family_relationships`. This keeps PostgreSQL as the source of truth while still supporting graph visualization in a frontend such as React Flow.

The media layer stores URLs and provider IDs rather than binary files. Cloudinary or S3 can be added behind a storage service without changing album, timeline, or memorial ownership.

## Security Direction

The current implementation supports authenticated API routes, CORS, Google OAuth when the `oauth` Spring profile is active, and role-ready user records. The next security hardening step is to add stateless JWT issuance after OAuth success, then enforce method-level role policies for ADMIN, MEMBER, and VIEWER.

## Frontend Direction

The backend is ready for a Next.js App Router frontend. The first frontend slice should consume:

- `/api/auth/me`
- `/api/family/branches/{branch}/members`
- `/api/family/members/{id}/relationships`
- `/api/timeline/posts`
- `/api/albums`
- `/ws`
