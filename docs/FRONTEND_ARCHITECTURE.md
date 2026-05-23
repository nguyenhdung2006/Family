# HomeTree Frontend Architecture

The frontend lives in `frontend/` and is intentionally separated from the Spring Boot backend. It uses Next.js App Router, strict TypeScript, TailwindCSS, React Query, Zustand, React Flow, React Hook Form, Zod, Framer Motion, and STOMP.

## Runtime Layers

- `src/app`: route entrypoints.
- `src/components`: reusable UI and feature-specific visual components.
- `src/features`: typed API calls, React Query hooks, and feature types.
- `src/lib/api`: fetch client, envelope parsing, query keys.
- `src/lib/websocket`: STOMP client and room subscription hooks.
- `src/stores`: client-only UI state via Zustand.

## State Ownership

- React Query owns server state: auth, family members, relationships, timeline, albums, chat, notifications, memorials, kitchen.
- Zustand owns UI state: sidebar, selected tree member, expanded tree nodes, active chat room, chat drafts.
- Components should not copy server data into Zustand unless it is temporary UI state.

## API Contract

The frontend consumes the existing backend envelope:

```ts
type ApiEnvelope<T> = {
  data: T;
  message?: string;
  status?: string;
};
```

Backend base URL is controlled by `NEXT_PUBLIC_API_BASE_URL`.

## WebSocket Contract

Chat subscribes to `/topic/rooms/{roomId}` through `/ws` with SockJS/STOMP.

Current backend STOMP auth requires a Bearer JWT header. The backend OAuth flow sets an httpOnly cookie, so full browser WebSocket auth needs either:

- backend accepting the JWT cookie during STOMP handshake, or
- backend returning a short-lived WebSocket token through an authenticated endpoint.

The frontend is already structured to send `Authorization: Bearer <token>` when a token exists in local storage.
