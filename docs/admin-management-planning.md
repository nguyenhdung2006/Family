# Admin Management Planning

## 1. Muc dich

Tai lieu nay lap ke hoach an toan cho cac tinh nang Admin Management trong Digital Family Hub. Day chi la tai lieu thiet ke, chua trien khai admin UI, chua doi backend/frontend, chua doi database schema, chua them migration va chua thay doi authentication logic.

Muc tieu tuong lai la giup admin quan ly thanh vien gia dinh, vai tro, loi moi va moderation mot cach ro rang, nhung khong lam yeu privacy hoac ownership rules hien co.

## 2. Role model hien tai

Digital Family Hub hien co 3 role:

### ADMIN

Kha nang hien tai:

- Co quyen quan ly noi dung rong hon trong cac module da co ownership checks.
- Co the edit/delete nhieu loai content cua user khac, tuy module da ho tro.
- Co the thuc hien mutation UI neu frontend hien control.
- Phu hop vai tro nguoi quan ly hub/gia dinh.

Can canh giac:

- ADMIN la role nhay cam nhat.
- Moi tinh nang role/user management phai tranh lockout va privilege escalation.

### MEMBER

Kha nang hien tai:

- Co the tao content o cac module duoc ho tro.
- Co the edit/delete content cua chinh minh khi backend da enforce ownership.
- Co the tham gia chat room neu la participant.
- Khong nen quan ly role nguoi khac.

### VIEWER

Gioi han hien tai:

- Doc noi dung duoc phep xem.
- UI an hoac disable create/edit/delete controls.
- Backend van phai enforce permissions neu user bypass UI.
- Khong nen send WebSocket messages hoac mutation content.

VIEWER phu hop cho thanh vien chi xem, nguoi lon tuoi khong muon thao tac phuc tap, hoac tai khoan khach trong gia dinh.

## 3. Cac vung nhay cam ve bao mat

### Ownership enforcement

Nhieu module da co ownership server-side:

- Timeline posts theo author.
- Albums/media theo creator/uploader.
- Recipes theo `createdBy`.
- Memorial tributes theo author.
- Notifications theo recipient/current user logic.

Admin management khong duoc pha vo rule: user khong duoc sua/xoa content cua nguoi khac neu khong co role ro rang.

### Notifications privacy

Notifications da duoc scope per current user. Admin UI tuong lai khong nen vo tinh hien notification rieng tu cua tat ca thanh vien neu chua co product decision.

### Memorial privacy

Memorial la module nhay cam ve cam xuc va quyen rieng tu. Admin moderation can co xac nhan ro, va khong nen xoa/sua tribute cua nguoi khac mot cach qua de dang.

### Messenger participation

Chat rooms/messages can rieng tu theo participant. Admin UI khong nen mac dinh cho admin doc moi phong chat neu san pham chua quyet dinh ro.

### Media ownership

Albums/media co anh gia dinh that. Admin actions nhu xoa album, xoa media hoac thay owner co rui ro cao, dac biet neu production storage/delete policy chua chot.

### Family tree edits

Family tree co tinh du lieu quan he gia dinh. Edit sai co the lam hong cay gia pha. Admin UI can co lich su/confirmation truoc khi cho phep thao tac lon.

## 4. Cac kha nang Admin Management tuong lai

| Capability | Risk level | Backend impact | Frontend impact | Abuse potential | Rollback difficulty |
| --- | --- | --- | --- | --- | --- |
| User list | Thap-Trung binh | Can endpoint read-only cho danh sach user/family members, co auth ADMIN. | Bang danh sach don gian, search/filter nhe. | Lo thong tin thanh vien neu permission sai. | Thap neu chi read-only. |
| Role visibility | Thap | Co the tai su dung role hien co neu API da tra role. | Hien role badge trong admin panel. | Thap, vi chua cho thay doi. | Thap. |
| Role changes | Cao | Can endpoint update role, validation last-admin/self-demotion. | Form/dropdown role, confirmation dialog. | Privilege escalation, lockout admin, ha quyen sai nguoi. | Trung binh-Cao. |
| Account disable/reactivate | Cao | Can field trang thai account neu chua co, migration co the can. Auth phai check disabled. | Toggle status, confirmation, disabled state. | Khoa nham tai khoan, tu khoa admin, bypass neu auth khong check. | Cao neu anh huong login. |
| Invite management | Trung binh-Cao | Can invite token/table/expiry/status neu chua co. | Man hinh tao loi moi, copy link, revoke. | Moi sai nguoi, token leak, role gan sai. | Trung binh. |
| Audit visibility | Trung binh | Can audit data source neu chua co. Co the bat dau bang read-only activity tu existing timestamps. | Timeline/bang log don gian. | Lo hanh vi rieng tu, qua nhieu thong tin nhay cam. | Thap-Trung binh neu read-only. |
| Moderation actions | Cao | Endpoint admin delete/hide content, reason, maybe audit. | Nut hide/delete/report, warning dialogs. | Lam mat du lieu, tranh chap gia dinh, misuse admin power. | Cao neu hard delete. |
| Transfer ownership | Cao | Can endpoint doi owner, validation module-specific. | Owner picker, confirmation. | Chiem noi dung nguoi khac, audit khong ro. | Cao. |
| Family settings | Trung binh | Can settings model neu chua co. | Settings panel don gian. | Cau hinh sai lam mo/chan access. | Trung binh. |

## 5. Rollout an toan duoc khuyen nghi

### Phase A: Read-only admin dashboard

Muc tieu:

- Hien danh sach thanh vien va role hien tai.
- Hien thong tin tong quan an toan: so bai timeline, so album, so recipe, so tribute neu co endpoint phu hop.
- Khong co nut thay doi role, disable account, delete content.

Ly do:

- Rui ro thap.
- Giup kiem tra du lieu va UX truoc khi them hanh dong nhay cam.
- It anh huong schema neu endpoint co the dung user data hien co.

### Phase B: Role visibility only

Muc tieu:

- Lam ro ADMIN/MEMBER/VIEWER tren UI.
- Them chu thich ngan gon bang ngon ngu san pham, khong dung thuat ngu security nang.
- Cho admin nhin thay ai dang co quyen gi, nhung chua sua.

### Phase C: Controlled role changes

Muc tieu:

- Cho ADMIN doi role user khac voi guardrails.
- Chan self-demotion neu day la admin cuoi.
- Chan xoa/ha quyen last admin.
- Co confirmation ro rang.

Can co backend enforcement truoc khi hien UI.

### Phase D: Moderation/admin actions

Muc tieu:

- Them hanh dong nhay cam sau cung: hide/delete content, disable account, revoke invite.
- Uu tien soft-delete/hide neu co the, nhung can schema decision rieng.
- Them audit/log neu san pham can.

## 6. Protections bat buoc khi trien khai

### Prevent self-demotion lockout

Admin khong duoc tu ha role cua minh neu hanh dong do lam mat quyen truy cap admin can thiet.

### Prevent last-admin removal

He thong phai dam bao luon con it nhat mot ADMIN active.

### Avoid accidental mass privilege changes

Khong nen co bulk role change trong MVP. Neu sau nay co, can confirmation manh va audit.

### Require confirmation for destructive actions

Hanh dong nhu delete content, disable account, revoke invite, transfer ownership can co confirmation dialog ro rang.

### Preserve ownership rules

Admin UI khong duoc thay the ownership enforcement. Backend van phai enforce:

- MEMBER chi sua/xoa noi dung cua minh.
- VIEWER khong mutation.
- ADMIN action can duoc check role server-side.

### Avoid privacy expansion by accident

Admin dashboard khong nen tu dong hien:

- Noi dung chat rieng cua rooms ma admin khong tham gia.
- Notifications rieng tu cua user khac.
- Media/private memorial data neu chua co product decision.

## 7. UI/UX planning notes

### Simple admin panels

- Bat dau bang bang danh sach thanh vien.
- Hien role bang badge de doc nhanh.
- Dung filter/search nhe neu danh sach dai.
- Khong can dashboard phuc tap o giai doan dau.

### Elderly-family usability

- Dung ngon ngu gan gui: "Nguoi quan ly", "Thanh vien", "Chi xem".
- Tranh thuat ngu nhu RBAC, IAM, permission matrix.
- Giai thich ngan gon quyen truoc khi doi role.
- Nut nguy hiem can mau/canh bao ro, khong dat gan nut thao tac thuong.

### Warning dialogs

Dialog nen noi ro:

- Ai bi anh huong.
- Hanh dong se thay doi gi.
- Co the hoan tac khong.
- Neu la delete/disable, can ghi ro hau qua.

### Avoid admin overload

Khong nen dua moi module vao admin page ngay. Uu tien user/role visibility truoc, moderation sau.

## 8. Do Not Yet Implement

Khong nen lam cac phan sau trong giai doan dau:

- Full RBAC system.
- Granular permissions matrix.
- Org/team hierarchy.
- External IAM provider.
- Audit log infrastructure lon.
- Bulk user import/export.
- SSO enterprise management.
- Fine-grained per-module permission editor.
- Admin impersonation.
- Global super-admin neu app chua co multi-tenant model ro.
- Automatic moderation/AI moderation.

## 9. Migration safety notes

Neu sau nay can schema cho admin management:

- Uu tien additive/non-breaking role evolution.
- Tranh rename role hien co neu frontend/backend dang dung `ADMIN`, `MEMBER`, `VIEWER`.
- Tranh rewrite ownership models trong luc rollout admin UI.
- Khong doi auth flow cung luc voi role management UI.
- Neu them account status, field nen co default an toan va migration backfill ro.
- Neu them invites, token phai hash/expire/revoke duoc.
- Neu them audit, bat dau voi append-only records thay vi sua/xoa log.

## 10. API design notes tuong lai

Chi la y tuong, chua implement:

```text
GET /api/admin/users
GET /api/admin/users/{id}
PATCH /api/admin/users/{id}/role
PATCH /api/admin/users/{id}/status
GET /api/admin/invites
POST /api/admin/invites
DELETE /api/admin/invites/{id}
```

Nguyen tac:

- Tat ca endpoint admin phai require `ADMIN`.
- Role changes phai validate last-admin/self-demotion.
- Unauthorized nen tra 403 hoac theo privacy pattern hien co, tuy endpoint.
- Khong tra du lieu rieng tu khong can thiet.
- Khong tron admin endpoints vao module CRUD hien co neu co the tranh.

## 11. Cau hoi can quyet dinh truoc khi code

- ADMIN co duoc xem chat room ma minh khong tham gia khong?
- ADMIN co duoc xem notifications cua nguoi khac khong?
- Admin delete content la hard delete hay hide/soft delete?
- Ai co quyen moi thanh vien moi?
- Invite gan role truoc hay user vao xong admin set role?
- Co can account disabled status khong?
- Co can audit log bat buoc cho role changes khong?
- Memorial content co can rule rieng khac cac module khac khong?

## 12. Ket luan

Huong an toan nhat la rollout admin management theo tung lop. Bat dau bang read-only admin dashboard va role visibility, sau do moi them controlled role changes voi guardrails. Moderation, account disable/reactivate va invite flow nen tach phase rieng vi co rui ro cao ve privacy, lockout va lam mat du lieu.
