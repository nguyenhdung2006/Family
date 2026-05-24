# Báo Cáo Trạng Thái Project Và Khả Năng AI Tự Build

Tài liệu này sắp xếp các phần của HomeTree từ dễ nhất đến khó nhất để tiếp tục làm. Mỗi phần ghi rõ project đã có gì, phần nào còn dở hoặc có thể bị tắc, và AI có thể tự build tốt đến đâu.

## 1. Health Check, Cấu Trúc Repo, Quy Ước API

Độ khó: dễ nhất.

Đã có và làm tốt:
- Backend Spring Boot đã có cấu trúc module rõ: `auth`, `users`, `family`, `posts`, `albums`, `media`, `chat`, `memorials`, `kitchen`, `notifications`, `shared`, `config`.
- API dùng envelope chung qua `ApiResponse`, lỗi tập trung qua `GlobalExceptionHandler`.
- Có endpoint health: `GET /api/health`.
- Frontend tách rõ `app`, `components`, `features`, `lib`, `stores`.
- Frontend có `apiFetch`, query keys, hooks React Query, Zustand cho UI state.

Đang dở hoặc có thể hoàn thành:
- Có thể bổ sung tài liệu OpenAPI/Swagger hoặc bảng API đầy đủ.
- Có thể thêm smoke test cho health/API envelope.

Tắc nghẽn:
- Không có tắc kỹ thuật lớn.

AI có thể tự build:
- AI có thể tự làm tốt phần này: thêm docs, thêm endpoint nhỏ, thêm test cơ bản, chuẩn hóa naming.

## 2. UI Shell, Navigation, Profile/Auth State

Độ khó: dễ.

Đã có và làm tốt:
- App shell có sidebar desktop, bottom nav mobile, header, profile avatar.
- Nếu chưa đăng nhập, frontend hiện màn hình Google login.
- Có route chính: home, family tree, timeline, albums, messenger, memorial, kitchen, notifications, profile.
- `GET /api/auth/me` đã là nguồn xác định user hiện tại.

Đang dở hoặc có thể hoàn thành:
- Logout UI/end-point đã có: backend clear cookie `HOMETREE_TOKEN`, frontend clear local auth state, disconnect STOMP clients, redirect `/login`.
- Profile hiện chủ yếu dựa vào current user, chưa thấy luồng chỉnh hồ sơ đầy đủ.
- Chưa có màn hình lỗi/empty state hoàn thiện cho mọi trường hợp auth.

Tắc nghẽn:
- Không tắc lớn; logout cần verify bằng browser thật sau OAuth.

AI có thể tự build:
- AI có thể tự build tốt các bước tiếp theo quanh auth UX như profile edit, session-expired screen, hoặc test frontend.
- AI cũng có thể thêm profile edit cơ bản nếu backend cho phép update user.

## 3. Kitchen Recipes

Độ khó: dễ.

Đã có và làm tốt:
- Backend có recipe entity/repository/service/controller.
- API hỗ trợ list, create, update recipe.
- Frontend có trang kitchen, form tạo/sửa recipe, fields như title, video URL, description, ingredients, instructions, notes from elders.
- React Query hooks đã có.

Đang dở hoặc có thể hoàn thành:
- Có thể thêm delete recipe nếu sản phẩm cần.
- Có thể thêm search/filter theo người nấu, dịp lễ, tag.
- Có thể cải thiện validation frontend/backend.

Tắc nghẽn:
- Không có tắc lớn.

AI có thể tự build:
- AI tự build tốt CRUD mở rộng, form validation, filter/search, test service/controller.

## 4. Notifications

Độ khó: dễ đến trung bình.

Đã có và làm tốt:
- Backend có notification entity/repository/service/controller.
- API có list, create notification, mark read.
- Frontend có trang alerts/notifications, form tạo thông báo, nút đánh dấu đã đọc.
- Database có index cho unread notifications.

Đang dở hoặc có thể hoàn thành:
- Chưa có realtime notification.
- Chưa có push/email.
- Pagination hiện vẫn kiểu offset/page-size.

Tắc nghẽn:
- Realtime/push cần quyết định sản phẩm: chỉ in-app, WebSocket, email, hay mobile push.

AI có thể tự build:
- AI tự build tốt phần in-app nâng cấp: filter unread, mark all read, badge count, pagination.
- AI không nên tự quyết định push/email production nếu chưa có provider và policy.

## 5. Timeline / Memory Posts

Độ khó: trung bình thấp.

Đã có và làm tốt:
- Backend có memory post entity/repository/service/controller.
- API có list và create posts.
- Frontend có feed, memory card, composer, filter năm.
- Schema có index hướng tới cursor pagination.

Đang dở hoặc có thể hoàn thành:
- API hiện vẫn dùng offset pagination.
- Chưa thấy update/delete post.
- Media gắn với post chưa hoàn chỉnh theo hướng `media_links`.
- Tag family members mới có nền schema/index, cần kiểm tra mức độ nối vào UI.

Tắc nghẽn:
- Không tắc lớn nếu chỉ thêm CRUD.
- Nếu làm media/tag sâu sẽ chạm media model và cần migration/service cẩn thận.

AI có thể tự build:
- AI tự build tốt filter, edit/delete, validation, test controller/service.
- AI build được cursor pagination nếu giữ hợp đồng API rõ.
- AI cần thận trọng khi nối media/tag nhiều nơi vì dễ tạo duplication giữa timeline, albums, memorial.

## 6. Family Tree

Độ khó: trung bình.

Đã có và làm tốt:
- Backend có family members, branches, relationships.
- Role policy khá rõ: viewer đọc được, chỉ admin tạo/sửa family.
- Frontend có React Flow canvas, node, drawer, search, create/edit member, create/edit relationship.
- Có hỗ trợ branch paternal/maternal, generation level, avatar URL, biography.
- Database có constraint chống self-reference và dedupe relationship.

Đang dở hoặc có thể hoàn thành:
- Chưa có delete member/relationship.
- Layout graph hiện dựa vào logic frontend, chưa chắc đủ tốt khi cây lớn/phức tạp.
- `family_member_closure` đã có schema tương lai nhưng chưa nối service.
- Chưa có import/export GEDCOM hoặc import CSV.

Tắc nghẽn:
- Cây gia phả thật có nhiều case khó: cha mẹ nuôi, nhiều hôn nhân, ly hôn, anh em cùng cha khác mẹ, vòng quan hệ sai dữ liệu.
- Auto-layout/debug visual graph có thể mất thời gian khi dữ liệu lớn.

AI có thể tự build:
- AI tự build tốt CRUD, form, validation, delete an toàn, branch filter, search.
- AI build được closure table nếu yêu cầu rõ ancestor/descendant query.
- AI không nên tự thiết kế lại toàn bộ mô hình gia phả phức tạp nếu chưa có rule nghiệp vụ gia đình cụ thể.

## 7. Albums Và Media Upload

Độ khó: trung bình.

Đã có và làm tốt:
- Backend có albums, media asset, media controller.
- Cloudinary upload service đã có kiểm tra content type, size, upload preset, cloud name.
- Nếu Cloudinary chưa config, service trả `503 Service Unavailable` thay vì lỗi mơ hồ.
- Frontend có tạo/sửa album, chọn album, upload image/video, progress state, attach media, preview gallery, slideshow modal.
- Docker/env docs đã nhắc Cloudinary variables.

Đang dở hoặc có thể hoàn thành:
- Cần cấu hình `CLOUDINARY_CLOUD_NAME` và `CLOUDINARY_UPLOAD_PRESET`.
- `media_links` đã có schema tương lai nhưng chưa dùng.
- Chưa có delete media, reorder media, album cover, reuse một media ở nhiều nơi.
- Cần kiểm thử upload thật với Cloudinary.

Tắc nghẽn:
- Upload thật phụ thuộc network, Cloudinary account, unsigned/signed upload preset.
- Debug lỗi upload có thể cần xem response thật từ Cloudinary.

AI có thể tự build:
- AI tự build tốt UI upload, API attach, delete/reorder metadata, test mock service.
- AI chỉ build được phần Cloudinary production khi có env/provider đúng.
- AI khó tự debug 100% lỗi Cloudinary nếu thiếu credential, preset, network hoặc log response thật.

## 8. Memorials / Tributes

Độ khó: trung bình.

Đã có và làm tốt:
- Backend có memorial listing từ deceased family members và tribute repository/service/controller.
- API có list memorials, list tributes, create tribute, update tribute.
- Frontend có memorial view và form tribute.
- Role policy cho phép viewer đọc, member/admin tạo/sửa.

Đang dở hoặc có thể hoàn thành:
- Chưa thấy delete tribute.
- Chưa nối media vào tribute theo `media_links`.
- Chưa có workflow duyệt tribute nếu family muốn moderation.
- Memorial phụ thuộc dữ liệu family member có `deathDate`.

Tắc nghẽn:
- Không tắc kỹ thuật lớn.
- Tắc sản phẩm nếu cần moderation/quyền riêng tư chi tiết theo từng memorial.

AI có thể tự build:
- AI tự build tốt delete, validation, media attach cơ bản, filter/sort.
- AI cần yêu cầu rõ nếu có moderation hoặc privacy nâng cao.

## 9. Chat / Messenger REST

Độ khó: trung bình đến khó.

Đã có và làm tốt:
- Backend có chat rooms, messages, create/update room, list room, list messages, send message, mark seen.
- Frontend có messenger UI khá đầy đủ: room list, create/edit room, active room, composer, optimistic message, seen icon.
- React Query cache và Zustand draft/selection đã tách hợp lý.
- Role policy chặn viewer với `/api/messages/**`.

Đang dở hoặc có thể hoàn thành:
- Read receipt hiện chỉ là `seenAt` trên message, chưa phải group read receipts.
- `chat_message_reads` đã có schema tương lai nhưng chưa nối service.
- API vẫn offset pagination, chưa cursor.
- Chưa có room membership/participant model rõ cho private room.
- Chưa có delete message, edit message, attachments trong chat.

Tắc nghẽn:
- Group chat thật cần mô hình participants, quyền xem phòng, read receipts per-user.
- Nếu không định nghĩa rõ private/group room rules thì AI dễ build sai nghiệp vụ.

AI có thể tự build:
- AI tự build tốt phần REST CRUD nhỏ, cursor pagination, UI polish, optimistic handling.
- AI cần spec rõ để build participant permissions và read receipts đúng.

## 10. WebSocket / Realtime Chat

Độ khó: khó.

Đã có và làm tốt:
- Backend có STOMP endpoint `/ws`, SockJS fallback.
- Broker dùng `/topic` và `/queue`, app prefix `/app`.
- Chat broadcast tới `/topic/rooms/{roomId}`.
- WebSocket auth đã được harden theo hướng cookie-first: lấy `HOMETREE_TOKEN` HttpOnly cookie trong handshake, bind `StompPrincipal`, fallback Bearer token khi client gửi header.
- Backend validate subscribe `/topic/rooms/*` và send `/app/**`, viewer không được dùng chat socket.
- Backend có STOMP send endpoint `/app/rooms/{roomId}` gọi lại `ChatService.sendMessage(...)`, trong khi REST send cũ vẫn giữ nguyên.
- Frontend có `stomp-client.ts` và `use-room-socket.ts`, merge message vào React Query cache, lọc optimistic duplicate.
- Frontend logout chủ động disconnect các STOMP client đang theo dõi.
- nginx dùng một route `/ws` có Upgrade headers để bao cả endpoint gốc và SockJS subpaths.
- Đã có Spring STOMP integration tests cho authenticated connect, subscribe, send success, và viewer rejection.

Đang dở hoặc có thể hoàn thành:
- Cần verify end-to-end sau OAuth ở hai mode: local split-origin `localhost:3000` + `localhost:8080`, và production same-origin qua nginx.
- Chưa có reconnect UX rõ khi socket mất kết nối.

Tắc nghẽn:
- WebSocket + SockJS + cookie + CORS + OAuth là vùng debug khó vì lỗi có thể nằm ở browser cookie policy, SameSite, CORS, nginx proxy, STOMP CONNECT, hoặc session reconnect.
- Cần browser thật và log backend/frontend khi kiểm thử.

AI có thể tự build:
- AI có thể tự thêm test, log, trạng thái kết nối, retry UI.
- AI có thể tự sửa lỗi rõ khi có log cụ thể.
- AI khó tự debug hoàn toàn nếu chỉ biết "socket không chạy" mà không có browser/network/backend logs.

## 11. Google OAuth, JWT Cookie, Role-Based Security

Độ khó: khó.

Đã có và làm tốt:
- OAuth được tách vào Spring profile `oauth`, không làm hỏng dev mode khi thiếu credentials.
- Login bắt đầu ở `/oauth2/authorization/google`.
- OAuth success handler upsert `AppUser`, audit login, issue HS256 JWT, set HttpOnly cookie `HOMETREE_TOKEN`, redirect về frontend.
- JWT filter đọc được cả Bearer token và cookie.
- SecurityConfig có role policy tập trung cho API.
- User role có `ADMIN`, `MEMBER`, `VIEWER`.
- Production profile bật secure cookie.

Đang dở hoặc có thể hoàn thành:
- Logout endpoint clear cookie đã có ở `POST /auth/logout`, kèm route tương thích `POST /api/auth/logout`.
- Chưa có admin role management.
- OAuth-created user mặc định là `MEMBER`, chưa có invitation/onboarding gate.
- Cần rotate Google client secret đã từng bị paste vào chat.
- Cần verify OAuth local thật với credentials mới.

Tắc nghẽn:
- OAuth phụ thuộc Google Cloud Console: redirect URI, consent screen, client secret, local/prod origin.
- Bug OAuth thường cần browser redirect trace và Google error page cụ thể.

AI có thể tự build:
- AI tự build tốt role endpoint cơ bản, invitation model nếu spec rõ, và test mở rộng cho auth/session lifecycle.
- AI không thể tự hoàn tất Google Console setup/secret rotation nếu không có quyền tài khoản.
- AI khó debug OAuth production nếu thiếu exact redirect URI, HTTPS domain, cookie flags và Google error details.

## 12. Database, Flyway, Future Tables

Độ khó: khó.

Đã có và làm tốt:
- PostgreSQL là source of truth.
- Flyway có `V1__initial_hometree_schema.sql`.
- `V2__performance_indexes_and_future_ready_schema.sql` thêm indexes và bảng tương lai: `media_links`, `chat_message_reads`, `family_member_closure`.
- Schema đã nghĩ trước cho cursor pagination, media reuse, group read receipts, family graph query.

Đang dở hoặc có thể hoàn thành:
- Các bảng tương lai chưa nối vào service.
- APIs vẫn dùng offset pagination ở nhiều nơi.
- Chưa có seed data chính thức cho local/dev.
- Testcontainers PostgreSQL test đang disabled khi Docker chưa available.

Tắc nghẽn:
- Khi đã có data thật, migration cần cực kỳ cẩn thận để không mất dữ liệu.
- Không được sửa migration đã apply; chỉ append migration mới.

AI có thể tự build:
- AI tự build tốt migration additive, repository/service query, cursor pagination.
- AI cần backup/sample data/log migration nếu debug lỗi production DB.
- AI không nên tự sửa migration cũ hoặc làm data migration phá vỡ dữ liệu thật nếu chưa có xác nhận.

## 13. Testing Và Build Verification

Độ khó: khó do môi trường.

Đã có và làm tốt:
- Backend có `JwtServiceTest`.
- Có Spring context test với repository mocks.
- Có Testcontainers PostgreSQL test nhưng disabled.
- Frontend có scripts: `lint`, `typecheck`, `build`.
- Tài liệu ghi TypeScript và ESLint đã pass trong một mốc trước.

Đang dở hoặc có thể hoàn thành:
- Cần thêm backend tests cho controllers/services auth-protected.
- Cần frontend component/integration tests.
- Cần Playwright/E2E cho OAuth callback, navigation, chat, upload.
- Production `next build` từng bị stall sau banner trong environment này, cần verify lại clean shell/CI.

Tắc nghẽn:
- Build frontend trên Windows/Node version nhạy: repo yêu cầu Node `>=22 <24`; Node 24 đã từng gây treo.
- E2E OAuth cần account thật hoặc test strategy riêng.

AI có thể tự build:
- AI tự build tốt unit/integration tests khi dependency chạy ổn.
- AI có thể set up Playwright cho non-OAuth flows.
- AI khó tự kết luận lỗi `next build` stall nếu môi trường không trả log hoặc process treo không có stack trace.

## 14. Docker, Nginx, Production Deployment

Độ khó: rất khó.

Đã có và làm tốt:
- Backend Dockerfile dùng Eclipse Temurin Java 25.
- Frontend Dockerfile build Next standalone bằng Node 22.
- Local compose có PostgreSQL và Redis.
- Production compose có API, web, PostgreSQL, Redis, nginx.
- nginx route `/`, `/api/`, `/auth/`, `/oauth2/`, `/login/oauth2/`, `/ws` đúng hướng split frontend/backend; `/ws` giữ websocket Upgrade headers.
- Production profile dùng Redis cache và secure cookies.

Đang dở hoặc có thể hoàn thành:
- Chưa có TLS/cert automation.
- Chưa có secret manager.
- Chưa có backup/restore PostgreSQL.
- Chưa có health checks đầy đủ cho compose.
- Chưa có observability: logs tập trung, metrics, tracing, alerting.
- Cần verify OAuth/logout/WebSocket qua nginx thật.

Tắc nghẽn:
- Production debug phụ thuộc domain, HTTPS, reverse proxy headers, cookie secure/SameSite, firewall, container logs.
- Nếu không có quyền server/domain/cloud, AI chỉ có thể chuẩn bị config chứ không thể chứng minh production chạy.

AI có thể tự build:
- AI tự build tốt Docker/nginx config, healthcheck, docs, env templates.
- AI không thể tự đảm bảo production deploy thành công nếu thiếu server access, DNS, TLS, secrets, logs.

## 15. Admin, Invitation, Privacy, Real Family Governance

Độ khó: rất khó nhất về nghiệp vụ.

Đã có và làm tốt:
- Role nền tảng đã có `ADMIN`, `MEMBER`, `VIEWER`.
- SecurityConfig đã chặn một số hành động nhạy cảm theo role.
- OAuth upsert user giúp đăng nhập dễ.

Đang dở hoặc có thể hoàn thành:
- Chưa có admin screen quản lý user/role.
- Chưa có invitation/onboarding gate.
- Chưa có approval flow cho thành viên mới.
- Chưa có privacy model theo branch, album, memorial, chat room, từng người thân.
- Chưa có audit UI dù backend có audit service hook.

Tắc nghẽn:
- Đây là phần dễ sai nhất nếu không có rule gia đình rõ: ai được mời ai, ai được xem thông tin người đã mất, ai được sửa cây, viewer có được xem ảnh không, trẻ em/minor data xử lý thế nào.
- Debug không chỉ là code mà là policy và trust model.

AI có thể tự build:
- AI có thể tự build bản admin cơ bản: list users, đổi role, disable user, invitation token.
- AI cần bạn quyết định policy trước khi build privacy sâu.
- AI không nên tự quyết định governance/privacy mặc định cho dữ liệu gia đình thật.

## Thứ Tự Ưu Tiên Gợi Ý

Nếu muốn đi từ dễ đến khó và tạo giá trị nhanh:

1. Verify OAuth local với Google credentials mới.
2. Verify logout và WebSocket sau OAuth ở local split-origin.
3. Verify production compose/nginx/OAuth/logout/WebSocket trên HTTPS thật.
4. Hoàn thiện Kitchen/Notifications nhỏ: delete, filter, mark all read.
5. Thêm edit/delete cho Timeline, Memorial, Albums.
6. Cấu hình và test Cloudinary upload thật.
7. Thêm admin user/role management cơ bản.
8. Đổi timeline/chat/notifications sang cursor pagination.
9. Nối `chat_message_reads` cho group read receipts.
10. Nối `media_links` để reuse media giữa albums, timeline, memorial.
11. Thiết kế invitation/privacy/governance trước khi dùng cho gia đình thật.

## Tóm Tắt Ngắn

Project đã có nền full-stack tốt: backend Spring Boot, frontend Next.js, OAuth/JWT cookie, role security, family tree, timeline, albums/media, chat realtime, memorials, kitchen, notifications, Docker/nginx.

Phần AI tự build tốt nhất là CRUD, UI, tests, docs, pagination, logout, admin cơ bản. Phần AI dễ bị tắc nhất là OAuth/WebSocket/Cloudinary/production deploy vì phụ thuộc external provider, cookie/browser policy, network, secret, domain, logs thật. Phần không nên để AI tự quyết một mình là privacy, invitation, role governance và mô hình gia phả phức tạp vì đó là quyết định sản phẩm và dữ liệu gia đình thật.
