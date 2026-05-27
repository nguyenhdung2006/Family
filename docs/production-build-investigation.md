# Production Build Investigation Checklist

## 1. Purpose

Tài liệu này dùng để điều tra và xác minh production build cho Digital Family Hub.

Mục tiêu:

- Xác định vì sao production build trước đây inconclusive/hanging.
- Tách riêng việc debug build khỏi feature work.
- Kiểm tra từng subsystem một cách có thể lặp lại.
- Ghi lại lệnh, log, env và giả định runtime trước khi thay đổi cấu hình.

Project hiện có:

- Spring Boot backend.
- Next.js frontend.
- PostgreSQL.
- Redis.
- Docker Compose.
- OAuth/JWT auth.
- WebSocket/STOMP chat.

## 2. Current Known Local Runtime Assumptions

- Backend chạy với OAuth profile.
- PostgreSQL Docker dùng host port `5433` vì Windows local PostgreSQL đang chiếm `5432`.
- Frontend chạy ở `http://localhost:3000`.
- Docker Compose tồn tại cho PostgreSQL/Redis.
- Production build trước đây chưa được xác minh vì từng inconclusive/hanging.

## 3. Backend Build Verification

### Clean Build

- Chạy clean build từ repo root.
- Ghi lại toàn bộ log nếu fail hoặc treo.
- Kiểm tra có file/process nào lock target jar trên Windows không.

Command:

```powershell
.\mvnw.cmd clean test
```

Checklist:

- [ ] Maven wrapper chạy được.
- [ ] Dependency resolution hoàn tất.
- [ ] Compile backend thành công.
- [ ] Test compile thành công.
- [ ] Test suite chạy xong.
- [ ] Không có lỗi file lock trong `target/`.
- [ ] Không có tiến trình Java cũ đang giữ jar.

### Tests

Checklist:

- [ ] Unit tests pass.
- [ ] Integration/WebSocket tests pass hoặc skip có lý do rõ ràng.
- [ ] Testcontainers/Postgres test skip nếu local không hỗ trợ container.
- [ ] Không có flaky test lặp lại.

### Dependency Resolution

Checklist:

- [ ] Maven không bị kẹt khi tải dependency.
- [ ] Không có network/proxy issue.
- [ ] Local Maven cache không corrupt.
- [ ] Java version đúng với project.

### Flyway Startup Validation

Khi chạy backend thật, cần xác minh Flyway migrate đúng database.

Checklist:

- [ ] Datasource trỏ đúng Postgres host/port.
- [ ] Local Docker Postgres đang chạy ở host port `5433`.
- [ ] Flyway migration chạy không lỗi.
- [ ] Không có migration checksum mismatch.
- [ ] App không tự trỏ nhầm sang Windows local PostgreSQL port `5432`.

### Profile/Env Verification

Checklist:

- [ ] Backend chạy với profile phù hợp, ví dụ OAuth profile.
- [ ] Datasource URL đúng.
- [ ] DB username/password đúng.
- [ ] Redis host/port đúng nếu app cần Redis lúc startup.
- [ ] CORS allowed origins có `http://localhost:3000`.

### JWT/OAuth Startup Validation

Checklist:

- [ ] JWT secret/config tồn tại.
- [ ] Google OAuth client id/secret tồn tại nếu OAuth profile bật.
- [ ] Redirect URI khớp local/prod environment.
- [ ] Login options endpoint hoạt động.
- [ ] App startup không fail vì thiếu OAuth env.

Recommended startup command:

```powershell
.\mvnw.cmd spring-boot:run
```

## 4. Frontend Build Verification

### Install

Run from `frontend/`.

```powershell
npm install
```

Checklist:

- [ ] `node_modules` install thành công.
- [ ] `package-lock.json` không bị thay đổi ngoài ý muốn.
- [ ] Không có peer dependency issue nghiêm trọng.
- [ ] Node/npm version phù hợp.

### Typecheck

```powershell
npm run typecheck
```

Checklist:

- [ ] TypeScript pass.
- [ ] Không có type mismatch từ API DTO mới.
- [ ] Không có client/server component import sai.

### ESLint

```powershell
.\node_modules\.bin\node.cmd node_modules\eslint\bin\eslint.js . --max-warnings=0
```

Checklist:

- [ ] ESLint pass.
- [ ] Không có warning vì `--max-warnings=0`.
- [ ] Không có rule fail do generated/build output.

### Production Build

```powershell
npm run build
```

Checklist:

- [ ] Build bắt đầu đúng Next.js project.
- [ ] Build không treo ở compile step.
- [ ] Build không treo ở lint/type step nếu Next tự chạy checks.
- [ ] Build không treo ở static generation.
- [ ] Build không gọi API backend khi không cần thiết.
- [ ] Build không phụ thuộc browser/localStorage trong server context.
- [ ] Build output `.next/` được tạo.
- [ ] Không có route/page nào throw vì thiếu env.

### Next.js Production Output Validation

Checklist:

- [ ] `.next/` được sinh ra đầy đủ.
- [ ] Route manifest có các routes chính.
- [ ] Không có server/client boundary error.
- [ ] Không có dynamic API usage bất ngờ làm fail build.
- [ ] Không có image/media config issue.

### Environment Variable Verification

Checklist:

- [ ] `NEXT_PUBLIC_API_BASE_URL` đúng cho local/prod.
- [ ] Frontend không cần secret server-only trong client bundle.
- [ ] Không có env variable nào khiến build chờ external service.
- [ ] OAuth URL/login options phù hợp backend.

## 5. Docker Verification

### Compose Startup

```powershell
docker compose up --build
```

Checklist:

- [ ] Docker daemon đang chạy.
- [ ] Compose file parse thành công.
- [ ] Postgres container start thành công.
- [ ] Redis container start thành công.
- [ ] Backend container build/start thành công nếu có trong compose.
- [ ] Frontend container build/start thành công nếu có trong compose.

### Service Health Checks

Checklist:

- [ ] Postgres health check healthy.
- [ ] Redis health check healthy.
- [ ] Backend health endpoint trả OK.
- [ ] Frontend trả trang ở expected port.
- [ ] Logs không spam reconnect/error loop.

### Backend/Frontend Connectivity

Checklist:

- [ ] Frontend gọi đúng backend URL.
- [ ] CORS cho frontend origin hợp lệ.
- [ ] Auth cookie/token flow hoạt động.
- [ ] Login options endpoint gọi được từ frontend.

### Database Connectivity

Checklist:

- [ ] Backend connect đúng Postgres service trong Docker network.
- [ ] Local mode connect đúng `localhost:5433`.
- [ ] Docker mode không dùng nhầm `localhost` nếu phải dùng service name.
- [ ] Flyway chạy thành công trong container.

### Redis Connectivity

Checklist:

- [ ] Backend connect Redis đúng host/port.
- [ ] Docker mode dùng Redis service name.
- [ ] Redis không bắt auth nếu backend chưa cấu hình password.

## 6. Build Hang Investigation

### Identify Possible Infinite Loops

Checklist:

- [ ] Kiểm tra React hooks gần đây: `useEffect`, `useMemo`, `useState`.
- [ ] Kiểm tra components dùng React Query invalidation liên tục.
- [ ] Kiểm tra client components không set state trong render.
- [ ] Family Tree render loop đã từng xảy ra, cần regression check.

### Identify Long-running Frontend Build Steps

Checklist:

- [ ] Xác định build treo ở compile, lint, typecheck hay static generation.
- [ ] Chạy riêng `npm run typecheck`.
- [ ] Chạy riêng ESLint direct.
- [ ] Nếu chỉ `npm run build` treo, ghi timestamp từng stage.
- [ ] Kiểm tra memory/CPU trong Task Manager.

### Identify Unresolved Env Variable Waits

Checklist:

- [ ] Không có code build-time chờ backend API.
- [ ] Không có fetch build-time tới service chưa chạy.
- [ ] Không có OAuth discovery call chạy trong build.
- [ ] Không có Cloudinary call chạy trong build.

### Identify WebSocket Startup Hangs

Checklist:

- [ ] Backend WebSocket config không block app startup.
- [ ] Frontend không mở WebSocket trong server build.
- [ ] Messenger client code chỉ chạy ở browser/client component.
- [ ] WebSocket reconnect không chạy trong build context.

### Identify Blocking External Integrations

Checklist:

- [ ] Google OAuth không gọi external network trong build.
- [ ] Cloudinary không gọi external network trong build.
- [ ] Upload/media code không chạy trong build.
- [ ] Any SDK initialization không block nếu env thiếu.

### Identify Windows-specific Filesystem/Process Lock Issues

Checklist:

- [ ] Không có Java process cũ giữ `target/*.jar`.
- [ ] Không có Node process cũ giữ `.next/`.
- [ ] Antivirus/OneDrive không lock build folders.
- [ ] Terminal không đứng ở interactive prompt.
- [ ] Port conflicts được kiểm tra trước khi chạy app.
- [ ] PostgreSQL local Windows chiếm `5432`, Docker dùng `5433`.

## 7. Safe Investigation Rules

- Tránh đổi architecture khi đang debug build.
- Không trộn feature work với build debugging.
- Verify từng subsystem một: backend, frontend, Docker, auth, realtime.
- Capture logs trước khi đổi config.
- Ưu tiên commands local có thể tái lập.
- Chỉ thay một biến/config tại một thời điểm.
- Ghi lại command, working directory, env profile và kết quả.
- Không commit/generated build artifacts nếu không cần.
- Không thêm migration để xử lý lỗi build trừ khi chứng minh lỗi do schema thật.

## 8. Recommended Command Sequence

### Backend

Run from repo root:

```powershell
.\mvnw.cmd clean test
.\mvnw.cmd spring-boot:run
```

Suggested notes to capture:

- Java version.
- Active Spring profile.
- Datasource URL, without password.
- Postgres port.
- Flyway result.
- Health endpoint result.

### Frontend

Run from `frontend/`:

```powershell
npm install
npm run typecheck
.\node_modules\.bin\node.cmd node_modules\eslint\bin\eslint.js . --max-warnings=0
npm run build
```

Suggested notes to capture:

- Node version.
- npm version.
- `NEXT_PUBLIC_API_BASE_URL`.
- Last log line before any hang.
- CPU/memory behavior if build stalls.

### Docker

Run from repo root:

```powershell
docker compose up --build
```

Suggested notes to capture:

- Compose file used.
- Container names.
- Published ports.
- Health status.
- Backend/frontend logs.
- Postgres/Redis connection logs.

## 9. Known Risk Areas

OAuth configuration:

- Missing Google client id/secret can break OAuth profile startup.
- Redirect URI mismatch can make login work locally but fail in production.

Cloudinary configuration:

- Upload may fail if Cloudinary env is missing.
- Asset deletion policy is not yet implemented.
- Build should not depend on Cloudinary network calls.

Windows port conflicts:

- Local PostgreSQL may occupy `5432`.
- Docker Postgres should use host port `5433`.
- Backend config must match the intended local port.

Docker networking:

- Localhost inside container is not the host machine.
- Backend container should use service names for Postgres/Redis.
- Frontend container needs correct backend URL depending on deployment model.

Next.js production build memory usage:

- Build may appear hung if memory pressure is high.
- Check Task Manager for Node memory/CPU.
- Avoid running multiple build/dev servers simultaneously during investigation.

WebSocket runtime behavior:

- WebSocket should not start reconnect loops during build.
- STOMP authorization should be tested at runtime, not build time.
- Multi-client chat behavior still needs manual verification.

Environment variable mismatches:

- Local `.env` and Docker env may differ.
- Frontend `NEXT_PUBLIC_*` env is baked into build.
- Backend profile/env mismatch can point to wrong database or OAuth config.

## 10. Investigation Log Template

Use this template when running the checks:

```text
Date/time:
Machine:
Branch:
Command:
Working directory:
Environment/profile:
Expected result:
Actual result:
Last log line:
Duration before failure/hang:
CPU/memory observation:
Files changed:
Next action:
```

