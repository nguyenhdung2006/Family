# Bao Cao Y Tuong Du An, Trang Thai Feature, Va Do Kho Khi Build Bang AI

Cap nhat: 2026-05-27.

Tai lieu nay viet bang tieng Viet de giai thich HomeTree theo goc nhin san pham va ky thuat: du an nay la gi, hien dang co gi, dang lam do gi, chua lam gi, va nen giao viec cho AI theo thu tu nao de tranh loi treo, cho lau, deadlock, hoac cac loi kho debug.

## 1. Y Tuong Du An

HomeTree Digital Family Hub la mot ung dung web rieng tu cho gia dinh. No giong mot mang xa hoi nho chi danh cho nguoi trong nha, ket hop cac phan:

- Cay gia pha.
- Bai viet ky uc theo timeline.
- Album anh/video gia dinh.
- Chat/messenger noi bo.
- Trang tuong niem nguoi da mat.
- So tay cong thuc nau an cua gia dinh.
- Thong bao sinh nhat, gio, su kien, tin nhan.
- Dang nhap bang Google OAuth.

Van de du an giai quyet:

- Anh, cong thuc, ky uc, cau chuyen gia dinh thuong nam rai rac o Zalo, Facebook, Google Photos, may ca nhan.
- Cay gia pha va thong tin nguoi than kho luu tru co cau truc.
- Nguoi lon tuoi co nhieu cau chuyen/gia tri gia dinh nhung de bi mat theo thoi gian.
- Gia dinh can mot noi rieng tu de luu giu, chia se, va xem lai lich su cua minh.

Nguoi dung muc tieu:

| Nhom nguoi dung | Muc dich su dung |
| --- | --- |
| Admin gia dinh | Tao/sua cay gia pha, quan ly thanh vien, ve sau quan ly quyen. |
| Thanh vien gia dinh | Dang ky uc, upload anh, chat, viet tribute, them recipe, tao thong bao. |
| Viewer/khach/nguoi lon tuoi | Doc thong tin, xem anh, xem ky uc nhung bi gioi han quyen ghi. |

## 2. Du An Dang Co Gi

### Backend dang co

Backend dung Spring Boot, Java 25, Maven, PostgreSQL, Flyway, Spring Security, OAuth2, JWT cookie, Redis tuy chon, STOMP/SockJS WebSocket.

Nhung module chinh:

| Module | Dang lam gi |
| --- | --- |
| `auth` | Dang nhap Google OAuth, tao JWT, cookie `HOMETREE_TOKEN`, lay user hien tai, logout. |
| `users` | Luu user, email, avatar, role `ADMIN`, `MEMBER`, `VIEWER`. |
| `family` | Tao/sua/list thanh vien gia dinh va quan he. |
| `posts` | Timeline ky uc, event type, ngay xay ra, tag family member. |
| `albums` | Tao/sua album, gan media vao album. |
| `media` | Upload anh/video len Cloudinary. |
| `chat` | Phong chat, tin nhan, REST API, WebSocket broadcast. |
| `memorials` | Lay nguoi da mat va tao/sua tribute. |
| `kitchen` | Cong thuc nau an, tao/sua/list. |
| `notifications` | Tao/list/mark read thong bao. |
| `shared` | API response envelope, error handler, audit log. |
| `config` | Security, cache, WebSocket. |

### Frontend dang co

Frontend dung Next.js App Router, React 19, TypeScript, React Query, Zustand, Tailwind, React Flow, STOMP/SockJS.

Man hinh chinh:

| Man hinh | Dang co |
| --- | --- |
| Home | Dashboard tong quan, quick links, recent memories, birthdays, albums, chat rooms. |
| Login | Nut dang nhap Google. |
| Profile | Xem user hien tai va role. |
| Family Tree | Graph React Flow, tao/sua member, tao/sua relationship. |
| Timeline | Feed ky uc, composer tao bai, filter nam/event. |
| Albums | Tao/sua album, upload media, gallery, slideshow. |
| Messenger | Tao/sua room, list room, chat, optimistic message, WebSocket receive. |
| Memorial | List nguoi da mat, tao/sua tribute. |
| Kitchen | Tao/sua/list recipe. |
| Notifications | Tao thong bao, list, mark read, polling moi 60 giay. |

### Database dang co

Co 2 migration:

- `V1__initial_hometree_schema.sql`: schema chinh.
- `V2__performance_indexes_and_future_ready_schema.sql`: index va bang tuong lai.

Bang chinh:

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

Bang da tao nhung chua dung that:

- `media_links`: dung de tai su dung mot media o nhieu noi.
- `chat_message_reads`: read receipt rieng tung user.
- `family_member_closure`: truy van ancestor/descendant nhanh cho cay gia pha lon.

## 3. Feature Da Hoan Thanh

| Feature | Trang thai | Chat luong hien tai | Ghi chu |
| --- | --- | --- | --- |
| Health check | Hoan thanh | Tot | `GET /api/health`. |
| API envelope | Hoan thanh | Tot | Backend response co format chung. |
| Google OAuth/JWT cookie | Da co nen tang | Kha | Can verify browser that. |
| Logout | Da co | Kha tot | Clear cookie, session, frontend cache. |
| Family member create/update/list | Da co | Kha | Chua co delete va rule phuc tap. |
| Family relationship create/update/list | Da co | Co ban | Chua validate quan he phuc tap. |
| Timeline create/list | Da co | Co ban tot | Chua co edit/delete/media/comment. |
| Album create/update/list | Da co | Kha | Chua co delete/cover/reorder. |
| Media upload Cloudinary | Da co | Kha | Phu thuoc env Cloudinary. |
| Chat REST | Da co | Kha | Chua co participant permission. |
| Chat WebSocket | Da co | Kha manh | Can test browser/nginx. |
| Memorial tribute | Da co | Co ban tot | Chua co delete/moderation/media. |
| Kitchen recipe | Da co | On dinh | Chua co delete/search/filter. |
| Notifications | Da co | Co ban | Dang bi global, chua theo recipient. |
| Docker/nginx | Da co khung | Kha | Chua production hardening. |

## 4. Feature Dang Lam Do

| Feature | Dang co gi | Con thieu | Muc do uu tien |
| --- | --- | --- | --- |
| Auth production | OAuth, JWT cookie, logout | Rotate secret, verify browser, invitation gate, CSRF plan | Rat cao |
| Role security | Backend role matrix | Frontend an/disable UI theo role, resource-level permission | Rat cao |
| User/admin management | Co `AppUser` va role | UI/API doi role, moi user, disable user | Rat cao |
| Family tree nang cao | Graph co ban | Delete, import/export, relationship rules, closure table | Trung binh |
| Timeline nang cao | List/create/filter | Author, edit/delete, media, comments/reactions | Trung binh |
| Media model | Upload/attach album | `media_links`, delete, reuse, metadata, cleanup Cloudinary | Trung binh-cao |
| Chat product | Room/message/realtime | Participant permission, private room, read receipt per-user | Cao |
| Notification | List/create/read | Recipient, unread count, mark all, realtime, scheduler | Cao |
| Deployment | Docker/nginx | TLS, backups, healthcheck, monitoring, secret manager | Cao |
| Testing | JWT/WebSocket test | Controller/service/frontend/E2E tests | Cao |

## 5. Feature Chua Lam

Nhung thu nen co neu muon dung HomeTree cho du lieu gia dinh that:

- Moi thanh vien bang invitation link.
- Duyet thanh vien moi truoc khi vao app.
- Admin dashboard quan ly user/role.
- Tat/vo hieu hoa tai khoan.
- Quyen rieng tu theo branch, album, member, memorial, chat room.
- Profile edit.
- Delete family member/relationship.
- Import/export gia pha bang GEDCOM hoac CSV.
- Timeline edit/delete/comment/reaction.
- Upload media vao timeline/memorial/recipe.
- Album cover, reorder media, delete media.
- Xoa media tren Cloudinary khi delete.
- Chat private room dung nghia.
- Quan ly participant trong chat room.
- Read receipt tung user.
- Message edit/delete.
- Notification realtime.
- Scheduler gui notification theo `scheduledFor`.
- Search/filter recipe.
- OpenAPI/Swagger docs.
- Frontend test va Playwright E2E.
- CI/CD.
- Production TLS, backup DB, monitoring, log tap trung.

## 6. Task Sap Xep Tu De Den Kho

### Level 1 - Rat de, gan nhu khong co rui ro treo/cho lau

Nhung task nay AI lam duoc tot, it cham external service, it gay loi nghiem trong.

| Task | AI build duoc khong | Rui ro treo/cho lau | Ly do |
| --- | --- | --- | --- |
| Cap nhat docs tieng Viet | Rat duoc | Gan nhu khong | Chi sua markdown. |
| Them bang API endpoint vao docs | Rat duoc | Gan nhu khong | Chi doc code va viet docs. |
| Them empty state/loading text nho | Rat duoc | Gan nhu khong | Frontend UI don gian. |
| Sua typo/UI text | Rat duoc | Gan nhu khong | It phu thuoc logic. |
| Them validation frontend don gian | Rat duoc | Thap | Vi du required/min/max. |
| Them nut/filter UI khong doi backend | Duoc | Thap | Chi state local. |
| Them health/version info | Duoc | Thap | Endpoint don gian. |

Nen lam truoc neu muon "warm up" codebase.

### Level 2 - De, rui ro thap

| Task | AI build duoc khong | Rui ro | Ghi chu |
| --- | --- | --- | --- |
| Delete recipe | Duoc | Thap | Backend delete + frontend button. |
| Search/filter recipe local | Duoc | Thap | Co the lam frontend-only truoc. |
| Mark all notifications read | Duoc | Thap-trung binh | Can endpoint moi. |
| Them unread count notifications | Duoc | Thap | Neu tinh tren list hien tai. |
| Them profile edit co ban | Duoc | Trung binh thap | Can endpoint update user. |
| An mutation UI voi `VIEWER` | Duoc | Thap | Dung current user role frontend. |
| Cai thien React Query unauthorized retry | Duoc | Thap | Sua logic dung `ApiError.status`. |
| Them service/controller test nho | Duoc | Thap | Khong can provider ngoai. |

Day la nhom task AI nen lam de tang chat luong nhanh.

### Level 3 - Trung binh, co the loi nhung van AI build tot

| Task | AI build duoc khong | Rui ro | Diem can can than |
| --- | --- | --- | --- |
| Timeline edit/delete | Duoc | Trung binh | Can role policy, cache invalidation. |
| Album delete | Duoc | Trung binh | Can xu ly media lien quan. |
| Memorial tribute delete | Duoc | Trung binh | Can permission va confirm UI. |
| Family member delete | Duoc nhung can than | Trung binh-cao | FK relationships/media/tribute co the chan delete. |
| Family relationship duplicate friendly error | Duoc | Trung binh | Can catch DB conflict hoac query truoc. |
| Populate `author/createdBy/uploadedBy` | Duoc | Trung binh | Can backfill/migration neu data cu. |
| Recipient-aware notifications | Duoc | Trung binh | Can thay API va frontend. |
| Cursor pagination timeline | Duoc | Trung binh | Can giu contract ro rang. |
| Cursor pagination chat | Duoc | Trung binh-cao | Can UI load older messages. |
| Backend tests cho feature services | Duoc | Trung binh | Co the can mock nhieu repository. |

Nhom nay AI build duoc, nhung nen chay test sau moi thay doi.

### Level 4 - Kho, AI build duoc mot phan nhung can spec ro

| Task | AI build duoc khong | Rui ro | Vi sao kho |
| --- | --- | --- | --- |
| Admin user/role management | Duoc neu co rule | Trung binh-cao | Can quy dinh ai la admin dau tien. |
| Invitation/onboarding | Duoc neu co spec | Cao | Can token, expiry, email/no email, approval. |
| Chat participant permission | Duoc neu co policy | Cao | Anh huong security va UI. |
| Private chat room dung nghia | Duoc neu co spec | Cao | Can participant model that. |
| Per-user read receipts | Duoc nhung phuc tap | Cao | Can dung `chat_message_reads`. |
| Media reuse bang `media_links` | Duoc nhung phuc tap | Cao | Doi data model nhieu feature. |
| Family closure table | Duoc nhung phuc tap | Cao | Can maintain ancestor/descendant khi update graph. |
| Search toan cuc | Duoc mot phan | Trung binh-cao | Can query/index va UI. |
| OpenAPI/Swagger | Duoc | Trung binh | Can dependency/config va verify. |

Nhom nay nen co yeu cau nghiep vu ro truoc khi AI code.

### Level 5 - Rat kho, AI kho tu build hoan chinh neu khong co moi truong/provider/log

| Task | AI build duoc khong | Rui ro treo/cho lau | Vi sao |
| --- | --- | --- | --- |
| Verify OAuth Google that | AI huong dan/sua code duoc, khong tu lam het | Cao | Phu thuoc Google Console, redirect URI, credentials, browser. |
| Debug OAuth production | Kho tu lam het | Cao | Can domain HTTPS, cookie flags, logs, Google error page. |
| Verify WebSocket browser/nginx | AI sua duoc neu co log | Cao | Loi co the o cookie, CORS, SockJS, nginx, STOMP. |
| Debug Cloudinary upload production | AI sua duoc neu co response/log | Cao | Phu thuoc preset/account/network. |
| Next.js production build bi treo | AI co the dieu tra, nhung kho ket luan neu khong co stack/log | Cao | Co tien su build chi in banner roi doi lau. |
| Docker production deploy that | AI viet config duoc, khong dam bao hoan tat | Cao | Can server, DNS, TLS, secrets, firewall, logs. |
| Backup/restore production | AI viet script duoc | Cao | Can du lieu that va quyen server. |
| Full privacy/governance cho gia dinh that | AI khong nen tu quyet het | Cao | Day la quyet dinh san pham, niem tin, va du lieu nhay cam. |

Nhom nay khong nen giao AI lam mot minh theo kieu "tu xu het". Can nguoi cung cap credential, log, domain, rule nghiep vu, va xac nhan chinh sach.

## 7. Sap Xep Theo Rui Ro Treo, Cho Lau, Deadlock, Loi Kho Debug

Luu y quan trong: trong code hien tai, kha nang "deadlock dung nghia" khong cao vi app khong dung lock/concurrency phuc tap. Rui ro lon hon la:

- Lenh build/test cho qua lau.
- External provider khong tra ve response nhu mong doi.
- OAuth redirect loop.
- WebSocket reconnect/handshake loi.
- Docker/Testcontainers doi container.
- Network upload/download bi treo.
- Database migration/FK constraint loi.

### Nhom A - Gan nhu khong the treo

Task chi sua markdown, docs, type, copy text:

- Viet/cap nhat docs.
- Liet ke API.
- Cap nhat roadmap.
- Sua UI text.
- Doi label/button text.
- Them notes vao README.

AI nen uu tien nhom nay khi can nhanh, chac, khong gay side effect.

### Nhom B - Rui ro thap

Task frontend/backend nho, khong dung provider ngoai:

- Them filter frontend.
- Them validation form.
- Them empty/loading/error state.
- Them delete recipe.
- Them mark-all-read neu endpoint don gian.
- Them unit test nho.

Co the loi compile/test, nhung hiem khi lam chuong trinh doi lau.

### Nhom C - Rui ro trung binh

Task dung database/API/cache:

- Them endpoint update/delete cho feature co FK.
- Sua service transaction.
- Them migration nho.
- Them author/createdBy.
- Sua notification recipient.
- Cursor pagination.
- Them controller/service test.

Rui ro:

- Migration sai lam app khong start.
- FK constraint loi khi delete.
- Pagination contract lam frontend hien sai.
- Query cham neu index khong dung.

Can chay backend test va test thu API.

### Nhom D - Rui ro cao vi co network/provider/browser

Task cham OAuth, Cloudinary, WebSocket, Docker:

- OAuth Google.
- Cloudinary upload.
- WebSocket browser.
- nginx proxy `/ws`.
- Docker compose prod.
- Next build.

Rui ro:

- Browser redirect loop.
- Cookie khong gui do SameSite/Secure/CORS.
- SockJS connect that bai nhung UI chi im lang.
- Cloudinary request doi lau hoac tra loi mo ho.
- `next build` treo lau.
- Docker keo image/chay container cham.

Can log ro, timeout ro, va khong nen de AI chay lenh dai ma khong theo doi.

### Nhom E - Rui ro cao nhat vi vua ky thuat vua nghiep vu

Task lien quan quyen rieng tu, admin, invitation, governance:

- Ai duoc moi ai?
- Ai duoc xem thong tin nguoi da mat?
- Viewer co duoc xem album khong?
- Member co duoc tao notification toan nha khong?
- Ai duoc xoa bai/tin nhan/tribute?
- Co can phe duyet noi dung tribute khong?
- Du lieu tre em/minor duoc hien thi the nao?

AI co the build code, nhung khong nen tu quyet policy. Neu tu quyet sai, loi khong chi la bug ky thuat ma la loi san pham va privacy.

## 8. Cai Nao AI Build Tot

AI build tot:

- CRUD co ban.
- Form frontend.
- Validation don gian.
- Empty/loading/error state.
- Role-aware UI an/disable button.
- Service/controller test.
- DTO/API client/hook moi.
- Delete/update endpoint don gian.
- Filter/search co ban.
- Docs, roadmap, report.
- Refactor nho trong pham vi mot feature.
- Cursor pagination neu API contract ro.

Dieu kien de AI build tot:

- Co yeu cau ro.
- Khong can provider ngoai.
- Co the chay test local.
- Khong can quyet dinh privacy/governance phuc tap.

## 9. Cai Nao AI Kho Tu Build

AI kho tu build hoan chinh:

- OAuth that voi Google.
- Production deploy tren domain that.
- TLS/certificate.
- Cloudinary production debug neu khong co account/log.
- WebSocket browser/nginx neu khong co network log.
- Chinh sach invitation/privacy.
- Admin governance cho du lieu gia dinh that.
- Data migration tren database production da co du lieu.
- Backup/restore production.

Ly do:

- Can credential.
- Can quyen vao Google Cloud/Cloudinary/server.
- Can domain HTTPS.
- Can log that.
- Can nguoi quyet dinh nghiep vu.
- Loi co the nam ngoai code.

## 10. Thu Tu Lam Khuyen Nghi

Neu muon lam chac, it rui ro, tang chat luong dan dan:

1. Sua docs va cap nhat report tieng Viet.
2. Them role-aware UI de `VIEWER` khong thay nut mutation.
3. Sua React Query unauthorized retry.
4. Them delete/search/filter cho Kitchen.
5. Them mark-all-read va unread count cho Notifications.
6. Sua Notifications theo recipient.
7. Populate `createdBy/author/uploadedBy` cho cac feature.
8. Them delete/edit cho Timeline, Memorial, Albums.
9. Them tests cho service/controller.
10. Verify OAuth local bang credential moi.
11. Verify logout browser.
12. Verify WebSocket browser local.
13. Verify nginx `/ws` va `/auth`.
14. Them admin role management co ban.
15. Them invitation/onboarding.
16. Them participant permission cho chat.
17. Doi timeline/chat/notifications sang cursor pagination.
18. Noi `chat_message_reads`.
19. Noi `media_links`.
20. Thiet ke privacy/governance day du.
21. Production hardening: TLS, backup, secret manager, observability.

## 11. Ket Luan Ngan

HomeTree hien la mot nen tang full-stack kha tot cho app gia dinh rieng tu: co backend Spring Boot, frontend Next.js, auth OAuth/JWT cookie, family tree, timeline, albums, chat realtime, memorial, kitchen, notifications, database migration, Docker va nginx. Phan dang yeu nhat khong phai UI ma la governance/security production: chua co invitation, chua co admin role management, chua enforce participant/recipient/ownership, chua verify OAuth/WebSocket/Cloudinary trong moi truong that. Viec nen lam tiep theo la lam cac task rui ro thap de on dinh app, sau do verify auth/realtime/provider, roi moi di vao admin/privacy/deployment production.

