# PROJECT STATUS - Digital Family Hub

## 1. Tổng quan ngắn

Digital Family Hub là một ứng dụng web dành cho gia đình, đóng vai trò như một "ngôi nhà số" để lưu giữ thông tin thành viên, cây gia phả, ký ức, ảnh, trò chuyện, công thức nấu ăn, tưởng niệm và thông báo nội bộ.

Người dùng chính là các thành viên trong gia đình với ba nhóm quyền hiện có:

- `ADMIN`: quản trị và chỉnh sửa dữ liệu quan trọng.
- `MEMBER`: tạo và quản lý nội dung của chính mình.
- `VIEWER`: xem nội dung được phép xem, không tạo/sửa/xóa.

App đang giải quyết nhu cầu gom các hoạt động gia đình vào một nơi: xem cây gia đình, lưu kỷ niệm, chia sẻ album, trò chuyện riêng tư, ghi lại công thức, tưởng nhớ người đã mất và nhận thông báo.

## 2. Bản đồ căn nhà

### Home = sảnh chính

Mục đích: trang tổng quan để người dùng bước vào hệ thống và thấy nhanh các khu vực chính.

Hiện đã làm được:

- Có dashboard frontend.
- Hiển thị các lối vào như Timeline, Family Tree, Albums, Messenger, Kitchen, Memorial.
- Có dùng dữ liệu người dùng hiện tại qua auth hook.

Có chạy được không: Có, theo trạng thái local hiện tại.

Quyền:

- `ADMIN`, `MEMBER`, `VIEWER` đều có thể vào sau khi đăng nhập.
- Hành động mutation ở các phòng con được kiểm soát riêng.

Còn thiếu:

- Dashboard còn đơn giản.
- Chưa có cá nhân hóa sâu theo vai trò hoặc hoạt động gần đây.

Mức độ: Partial.

### Family Tree = phòng gia phả

Mục đích: quản lý thành viên gia đình và quan hệ gia phả.

Hiện đã làm được:

- Backend có family member và relationship API.
- Frontend có canvas cây gia đình.
- Vòng lặp render vô hạn ở `/family-tree` đã được sửa.
- UI mutation đã ẩn với `VIEWER`.

Có chạy được không: Có, render loop đã được xử lý.

Quyền:

- `ADMIN`: tạo/sửa thành viên và quan hệ.
- `MEMBER`: hiện tại chủ yếu xem, tùy API cụ thể vẫn cần kiểm thử ma trận quyền.
- `VIEWER`: chỉ xem, không hiện UI tạo/sửa.

Còn thiếu:

- Quy tắc gia phả nâng cao còn thiếu.
- Chưa có kiểm tra dữ liệu phả hệ phức tạp như vòng quan hệ, quan hệ mâu thuẫn, thế hệ tự động.
- UX kéo/thả và trình bày cây còn cần polish.

Mức độ: Partial.

### Timeline = phòng ký ức

Mục đích: lưu và chia sẻ các bài viết/ký ức gia đình theo dòng thời gian.

Hiện đã làm được:

- List/create timeline posts.
- Ownership server-side được set từ user đăng nhập.
- Người tạo hoặc `ADMIN` có thể sửa/xóa bài viết.
- Frontend chỉ hiện edit/delete cho chủ bài hoặc `ADMIN`.
- `VIEWER` không thấy UI tạo/sửa/xóa.

Có chạy được không: Có.

Quyền:

- `ADMIN`: sửa/xóa mọi post.
- `MEMBER`: tạo post, sửa/xóa post của mình.
- `VIEWER`: xem, không mutation.

Còn thiếu:

- Comment/love đang giống UI prototype, chưa chắc đã có backend đầy đủ.
- Chưa có media gắn trực tiếp với post theo trải nghiệm hoàn chỉnh.
- Chưa có kiểm thử thủ công đa người dùng đầy đủ.

Mức độ: Partial.

### Albums = kho ảnh gia đình

Mục đích: lưu album ảnh/video gia đình và quản lý media theo album.

Hiện đã làm được:

- List/create/update/delete album.
- Attach media vào album.
- Remove media khỏi album.
- Ownership album được set server-side từ user đăng nhập.
- `ADMIN` quản lý mọi album/media.
- `MEMBER` quản lý album/media của mình.
- Frontend ẩn edit/delete/remove nếu không phải owner hoặc `ADMIN`.

Có chạy được không: Có ở mức logic app. Upload thật phụ thuộc cấu hình media/Cloudinary.

Quyền:

- `ADMIN`: sửa/xóa mọi album, remove mọi media.
- `MEMBER`: tạo album, sửa/xóa album của mình, thêm/xóa media trong album của mình.
- `VIEWER`: xem, không mutation.

Còn thiếu:

- Cloudinary chưa được xác nhận cấu hình production.
- Chưa implement xóa asset thật trên Cloudinary.
- Chưa có fallback local upload rõ ràng nếu không dùng Cloudinary.
- Slideshow/gallery còn đơn giản.

Mức độ: Partial.

### Messenger = phòng trò chuyện

Mục đích: chat giữa các thành viên gia đình.

Hiện đã làm được:

- Backend có chat rooms, messages, room participants.
- WebSocket/STOMP đã có cấu hình.
- Message ordering ở frontend đã được normalize theo thứ tự thời gian.
- Backend đã enforce participant permission cho list/read/send/update room.
- Khi tạo room, creator được thêm làm participant.

Có chạy được không: Có ở mức backend/test; realtime cần kiểm thử thủ công thêm.

Quyền:

- `ADMIN`/`MEMBER`: tham gia phòng và gửi tin nhắn nếu là participant.
- `VIEWER`: bị chặn gửi message/restricted WebSocket flow theo rule hiện tại.

Còn thiếu:

- Read receipts nâng cao.
- Invite/add participant UI rõ ràng.
- Kiểm thử realtime nhiều client.
- UX chat còn prototype.

Mức độ: Partial.

### Memorial = phòng tưởng niệm

Mục đích: nơi lưu các lời tưởng nhớ cho thành viên đã mất.

Hiện đã làm được:

- List memorial members.
- List/create/update/delete tributes.
- Tribute author được set server-side từ user đăng nhập.
- `ADMIN` sửa/xóa mọi tribute.
- `MEMBER` sửa/xóa tribute của mình.
- Frontend chỉ hiện edit/delete cho author hoặc `ADMIN`.

Có chạy được không: Có.

Quyền:

- `ADMIN`: quản lý mọi tribute.
- `MEMBER`: tạo tribute, sửa/xóa tribute của mình.
- `VIEWER`: xem, không mutation.

Còn thiếu:

- Quy tắc ai được xem memorial cần quyết định sản phẩm.
- Chưa có moderation/approval.
- Chưa có media tưởng niệm.

Mức độ: Partial.

### Kitchen = phòng bếp/công thức

Mục đích: lưu công thức nấu ăn gia đình, ghi chú từ người lớn tuổi và link video.

Hiện đã làm được:

- List/create/update/delete recipes.
- `createdBy` được set server-side.
- `ADMIN` sửa/xóa mọi recipe.
- `MEMBER` sửa/xóa recipe của mình.
- Frontend chỉ hiện edit/delete cho owner hoặc `ADMIN`.

Có chạy được không: Có.

Quyền:

- `ADMIN`: quản lý mọi công thức.
- `MEMBER`: tạo công thức, sửa/xóa công thức của mình.
- `VIEWER`: xem, không mutation.

Còn thiếu:

- Search/filter recipe.
- Tag/category recipe.
- Import ảnh/video có kiểm soát.
- UI đọc công thức còn đơn giản.

Mức độ: Partial.

### Alerts = bảng thông báo

Mục đích: thông báo nội bộ cho người dùng, ví dụ sự kiện gia đình, nhắc sinh nhật, tin nhắn, kỷ niệm.

Hiện đã làm được:

- List notifications scoped theo current user.
- Mark read chỉ áp dụng cho notification của current user.
- Create/update/delete notifications.
- Không thêm migration: ownership hiện dùng relationship `recipient` như owner cho self-created alerts.
- Response có `createdById`/`createdByName`, map từ `recipient`.
- Frontend có create/edit/delete controls theo owner hoặc `ADMIN`.

Có chạy được không: Có ở mức hiện tại.

Quyền:

- `ADMIN`: sửa/xóa mọi notification nếu truy cập được record qua backend.
- `MEMBER`: tạo notification cho chính mình, sửa/xóa notification của mình.
- `VIEWER`: xem notification của mình, không mutation.

Còn thiếu:

- Chưa có realtime notifications.
- Chưa có notification gửi cho người khác hoặc nhóm gia đình.
- Nếu muốn "createdBy" và "recipient" tách biệt thật sự, cần thiết kế schema/migration sau.
- Chưa có scheduler xử lý scheduled notifications.

Mức độ: Partial.

### Profile = thẻ thành viên

Mục đích: hiển thị thông tin người dùng hiện tại.

Hiện đã làm được:

- Có trang/profile frontend.
- Có current user hook và auth state.
- Dùng OAuth/JWT để nhận diện user.

Có chạy được không: Có.

Quyền:

- `ADMIN`, `MEMBER`, `VIEWER` đều có profile.

Còn thiếu:

- Chưa có edit profile hoàn chỉnh.
- Chưa có avatar/profile preferences đầy đủ.
- Chưa có quản lý account/security settings.

Mức độ: Prototype.

## 3. Nền móng kỹ thuật

Backend:

- Spring Boot backend.
- REST API cho auth, family, timeline, albums, media, chat, notifications, memorials, kitchen.
- Service layer đã bắt đầu enforce ownership/server-side auth cho nhiều module.
- `ResponseStatusException` dùng cho 401/404/privacy failures.

Frontend:

- Next.js/React frontend.
- React Query cho data fetching/mutations.
- UI component tự xây như `Card`, `Button`, `Input`, `Textarea`, `Badge`, `Avatar`.
- Các trang chính đã có surface sử dụng được, nhưng UI/UX vẫn còn rough.

Database/Flyway:

- PostgreSQL.
- Flyway migrations có schema ban đầu và indexes hiệu năng.
- Không thêm migration trong các phase CRUD gần đây, trừ khi thật sự cần.

OAuth/JWT:

- Google OAuth đã hoạt động trong local flow.
- JWT/auth retry logic frontend đã sửa để dùng `ApiError.status` thay vì string message fragile.

Docker/Postgres/Redis:

- Docker Compose có Postgres/Redis.
- Local runtime hiện dùng Postgres Docker host port `5433` vì Windows local PostgreSQL đang chiếm `5432`.
- Backend nên chạy với `oauth` profile và datasource trỏ port `5433`.
- Frontend chạy ở `http://localhost:3000`.

WebSocket:

- Có STOMP/WebSocket cho chat.
- Có participant checks cho room subscription/send path.
- Realtime behavior vẫn cần manual multi-client testing.

Tests:

- Backend có unit/integration tests cho auth, chat, notifications, timeline, albums, kitchen, memorials, WebSocket.
- Một Postgres Testcontainers test đang skip nếu môi trường không hỗ trợ container.
- Frontend hiện được kiểm bằng `npm run typecheck` và ESLint direct.

Local runtime notes:

- Backend: chạy với OAuth profile, datasource port `5433`.
- Frontend: chạy ở `localhost:3000`.
- Production build trước đây từng inconclusive/hung, chưa coi là verified.

## 4. Những việc đã hoàn thành gần đây

- Local runtime stabilized.
- Google OAuth works.
- Family tree render loop fixed.
- Auth retry logic fixed.
- Viewer mutation UI hidden.
- Notifications scoped to current user.
- Chat participant permissions enforced.
- Content ownership server-side enforced cho timeline posts, albums, media assets, recipes, memorial tributes.
- Timeline edit/delete.
- Album edit/delete/media remove.
- Recipe edit/delete.
- Memorial tribute edit/delete.
- Notification/alert edit/delete with owner/admin controls.

## 5. Những phần còn thiếu / chưa chắc

- UI/UX vẫn còn rough, nhiều màn hình mới ở mức usable hơn là polished.
- Cloudinary chưa được cấu hình/xác nhận đầy đủ.
- Cloudinary asset deletion chưa được implement.
- Production build trước đây inconclusive/hung.
- Production deployment chưa verified.
- Admin management UI còn thiếu.
- Invite flow còn thiếu.
- Advanced privacy rules còn thiếu.
- Family tree advanced genealogy rules còn thiếu.
- Real multi-user permission testing vẫn cần làm.
- Mobile polish/accessibility chưa hoàn chỉnh.
- Notifications chưa có realtime delivery/scheduler/recipient targeting nâng cao.
- Chưa có backup/security hardening cho production.

## 6. AI có thể build tiếp gì?

| Feature | AI can build? | Why | Input needed from me | Risk |
| --- | --- | --- | --- | --- |
| Manual role matrix tests | Yes | Có rule ADMIN/MEMBER/VIEWER rõ ràng và API đã tương đối ổn định | Danh sách account test hoặc seed data mong muốn | Có thể bỏ sót case sản phẩm nếu role policy chưa chốt |
| Admin management UI | Partially | AI có thể tạo CRUD UI theo API hiện có | Bạn cần quyết định admin được quản lý user/role/invite tới đâu | Rủi ro lộ quyền quá rộng |
| Invite flow | Partially | Có thể build email/token flow kỹ thuật | Cần quyết định ai được mời, hạn token, domain/email policy | Rủi ro bảo mật/account takeover |
| Cloudinary configuration | Partially | AI có thể wiring env/config và mock fallback | Cần credentials, policy upload/delete, size/type limits | Rủi ro mất/xóa nhầm asset thật |
| Local upload fallback | Yes | Có thể build storage local/dev-only rõ ràng | Bạn muốn lưu file ở đâu trong dev | Không phù hợp production nếu dùng sai |
| UI/UX polish | Partially | AI có thể cải thiện consistency, spacing, responsive | Cần gu hình ảnh, đối tượng người dùng, mức độ đơn giản cho người lớn tuổi | Dễ đẹp nhưng không đúng thói quen gia đình |
| Family tree advanced validation | Partially | AI có thể thêm validation kỹ thuật | Cần rule gia phả cụ thể | Rủi ro áp đặt sai văn hóa/gia đình |
| Realtime notifications | Yes | Có WebSocket nền cho chat, có thể mở rộng | Cần quyết định notification event nào realtime | Rủi ro spam/ồn nếu không có UX policy |
| Production deployment | Partially | AI có thể chuẩn hóa Docker/Nginx/env docs | Cần target server, domain, secrets, backup policy | Rủi ro production/security nếu tự quyết secrets |
| Privacy settings | No | Đây là quyết định sản phẩm/gia đình nhạy cảm | Bạn cần chốt ai thấy gì | Rủi ro lộ dữ liệu gia đình |

## 7. AI không nên tự quyết

- Privacy policy: ai được xem dữ liệu gia đình, ảnh, memorial, chat.
- Family roles: `ADMIN`, `MEMBER`, `VIEWER` có ý nghĩa xã hội như thế nào trong gia đình.
- Who can see memorials: tưởng niệm là dữ liệu nhạy cảm, cần quyết định rõ.
- Production secrets/deployment: domain, OAuth secrets, DB password, backup keys.
- Cloudinary deletion policy: xóa mềm hay xóa thật, ai được xóa, có restore không.
- UX for elderly family members: chữ to/nhỏ, flow đơn giản, thuật ngữ, ngôn ngữ.
- Final visual identity: tên app, màu sắc, tone cảm xúc, logo, ảnh hero.

## 8. Recommended next roadmap

1. Finish remaining CRUD/alerts.
   - Alerts CRUD đã có basic edit/delete; bước tiếp theo là quyết định recipient targeting, scheduler, realtime.

2. Manual test role matrix.
   - Test thủ công `ADMIN`, `MEMBER`, `VIEWER` cho từng phòng.
   - Kiểm thử owner vs non-owner ở nhiều account thật.

3. Configure Cloudinary or mock local upload fallback.
   - Chốt dev/prod upload strategy.
   - Chưa implement xóa asset thật nếu chưa có policy.

4. Improve UI/UX system.
   - Làm responsive polish.
   - Chuẩn hóa empty/loading/error states.
   - Tăng accessibility, đặc biệt cho người lớn tuổi.

5. Add invite/admin management.
   - Admin mời thành viên.
   - Quản lý role.
   - Có audit trail cơ bản.

6. Production build/deploy.
   - Điều tra lại build hung/inconclusive.
   - Verify Docker/Nginx/env.
   - Chạy thử staging.

7. Backup/security hardening.
   - Backup DB.
   - Secrets management.
   - Rate limits.
   - Privacy review.

## 9. Current verification snapshot

- Backend tests: pass, latest known `49` tests run, `0` failures, `1` skipped.
- Frontend typecheck: pass via `npm run typecheck`.
- ESLint direct: pass via `.\node_modules\.bin\node.cmd node_modules\eslint\bin\eslint.js . --max-warnings=0`.
- Manual runtime: local development flow đã được ổn định/xác nhận với backend OAuth profile + Postgres Docker port `5433`, frontend ở `localhost:3000`.

