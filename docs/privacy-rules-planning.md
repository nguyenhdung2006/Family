# Privacy Rules Planning

## 1. Muc dich

Tai lieu nay lap ke hoach rieng tu va visibility cho Digital Family Hub. Day chi la tai lieu thiet ke, chua trien khai privacy settings, chua doi backend/frontend, chua doi database schema, chua them migration va chua doi API/auth logic.

Digital Family Hub chua nhieu du lieu nhay cam cua gia dinh: cay gia pha, anh, ky uc, memorial, chat noi bo, notifications, recipes va profile thanh vien. Vi vay privacy rules can duoc quyet dinh ro truoc khi them tinh nang moi.

## 2. Hien trang chung

Role hien tai:

- `ADMIN`: quan ly rong hon, co the thuc hien nhieu mutation hon tuy module.
- `MEMBER`: tao noi dung va quan ly noi dung cua minh.
- `VIEWER`: chu yeu doc noi dung duoc phep, khong mutation.

Hien app chu yeu dung:

- Ownership-based mutation permissions.
- Role-based UI visibility cho create/edit/delete.
- Broad read visibility o nhieu module.

Long-term privacy rules chua duoc chot.

## 3. Current visibility assumptions by module

### Home/dashboard

| Item | Notes |
| --- | --- |
| Current likely visibility | Tat ca user dang nhap co the xem dashboard/home. |
| Privacy concerns | Dashboard co the tong hop memories, notifications, albums, family events. Neu summary hien du lieu nhay cam, VIEWER co the thay qua nhieu. |
| Sensitive data risk | Trung binh, tuy noi dung dashboard lay tu module nao. |
| Future visibility controls | Dashboard nen ton trong visibility cua tung module nguon. Khong hien summary cua noi dung user khong duoc xem. |

Requires Owner Decision:

- Dashboard co duoc hien tat ca hoat dong gia dinh cho VIEWER khong?
- Co can an memorial/chat/media summary khoi dashboard khong?

### Family Tree

| Item | Notes |
| --- | --- |
| Current likely visibility | Family tree co ve family-wide cho user dang nhap. |
| Privacy concerns | Ngay sinh, quan he gia dinh, thong tin nguoi da mat/song, cau truc gia dinh co the nhay cam. |
| Sensitive data risk | Cao. |
| Future visibility controls | Co the can role-based edit, branch visibility, an truong nhay cam, hoac VIEWER read-only co gioi han. |

Requires Owner Decision:

- Should VIEWER see all family members?
- Co can an ngay sinh/thong tin lien he khoi VIEWER khong?
- Co can branch visibility, vi du chi xem nhanh gia dinh gan minh?
- Ai duoc edit quan he gia pha?

### Timeline

| Item | Notes |
| --- | --- |
| Current likely visibility | Family feed rong cho user dang nhap; mutation theo owner/admin. |
| Privacy concerns | Memories/posts co the chua anh, cam xuc, su kien rieng. |
| Sensitive data risk | Trung binh-Cao. |
| Future visibility controls | Owner-controlled visibility cho post: family-wide, selected members, private draft. |

Requires Owner Decision:

- Timeline post mac dinh co family-wide khong?
- User co duoc tao memory private chi minh xem khong?
- VIEWER co duoc xem tat ca memories khong?

### Albums/media

| Item | Notes |
| --- | --- |
| Current likely visibility | Albums/media likely visible broadly to authenticated family users; mutation theo owner/admin. |
| Privacy concerns | Anh/video that cua gia dinh, tre em, dia diem, su kien rieng. |
| Sensitive data risk | Rat cao. |
| Future visibility controls | Private albums, selected viewers, hide download, per-album visibility, media removal permissions. |

Requires Owner Decision:

- Can private albums exist?
- Album mac dinh la family-wide hay owner-only cho den khi share?
- ADMIN co duoc xem moi private album khong?
- VIEWER co duoc xem/download media khong?

### Messenger

| Item | Notes |
| --- | --- |
| Current likely visibility | Chat rooms/messages duoc protect theo participant. |
| Privacy concerns | Noi dung chat rieng, direct/group rooms. Admin access can duoc quyet dinh ro. |
| Sensitive data risk | Cao. |
| Future visibility controls | Invite-only rooms, direct/private rooms, room owner/admin, participant management. |

Requires Owner Decision:

- Can direct/private chat rooms exist?
- ADMIN co duoc xem room ma minh khong la participant khong?
- VIEWER co duoc tham gia chat hay chi xem mot so noi dung?
- Ai duoc them/xoa participant khoi room?

### Memorial

| Item | Notes |
| --- | --- |
| Current likely visibility | Tributes likely family-wide; mutation theo author/admin. |
| Privacy concerns | Noi dung tuong niem rat nhay cam ve cam xuc, quan he gia dinh, ngay gio. |
| Sensitive data risk | Cao. |
| Future visibility controls | Memorial visibility theo family-wide/restricted, tribute approval/moderation, hidden memorials. |

Requires Owner Decision:

- Should memorials be family-wide or restricted?
- Ai duoc tao/sua/xoa memorial tributes?
- Co can approval truoc khi tribute hien cong khai trong gia dinh khong?
- Co can an memorial voi mot so VIEWER/guest khong?

### Kitchen

| Item | Notes |
| --- | --- |
| Current likely visibility | Recipes likely family-wide read; mutation theo creator/admin. |
| Privacy concerns | Thuong it nhay cam hon, nhung co the chua cau chuyen gia dinh, video, notes from elders. |
| Sensitive data risk | Thap-Trung binh. |
| Future visibility controls | Family-wide default, optional private recipe/draft, hide elder notes neu can. |

Requires Owner Decision:

- Should recipes be public to all family members?
- Notes from elders co can privacy rieng khong?
- VIEWER co duoc xem video/notes khong?

### Alerts/Notifications

| Item | Notes |
| --- | --- |
| Current likely visibility | Notifications da scope theo current user/recipient. |
| Privacy concerns | Notification co the tiet lo sinh nhat, memorial date, message, family event. |
| Sensitive data risk | Trung binh-Cao. |
| Future visibility controls | Per-user notifications nen la default. Family-wide alerts nen tao notification rieng cho tung recipient thay vi broadcast chung. |

Requires Owner Decision:

- Should notifications ever be shared family-wide?
- Ai duoc tao family-wide alert?
- VIEWER co duoc mark read khong?
- Co can notification preferences theo user khong?

### Profile

| Item | Notes |
| --- | --- |
| Current likely visibility | User co profile/current user; muc do xem profile nguoi khac can kiem tra khi mo rong. |
| Privacy concerns | Ten, email, avatar, role, quan he, ngay sinh, lien he co the nhay cam. |
| Sensitive data risk | Trung binh-Cao. |
| Future visibility controls | Hide email/contact, role visibility, profile field visibility, private birthday. |

Requires Owner Decision:

- Can users hide parts of their profile?
- VIEWER co duoc xem email cua thanh vien khac khong?
- Role co nen hien voi moi nguoi khong?
- Birthday/contact info co nen co visibility rieng khong?

## 4. Possible privacy models

### A. Family-wide visibility by default

Tat ca user trong family hub co the doc hau het noi dung, mutation van theo role/owner.

| Criteria | Assessment |
| --- | --- |
| Complexity | Thap. |
| Usability | De hieu. |
| Elderly-family friendliness | Cao, it cau hinh. |
| Backend impact | Nho, giu broad read visibility. |
| Frontend impact | Nho, UI don gian. |
| Migration risk | Thap. |

Pros:

- Phu hop MVP/student project.
- It lam roi nguoi dung.
- Giu code don gian.

Cons:

- Khong du cho albums/chat/memorial nhay cam.
- VIEWER co the thay qua nhieu neu role nay duoc dung cho guest.

### B. Owner-controlled visibility

Moi content co owner chon visibility: private, family-wide, selected users.

| Criteria | Assessment |
| --- | --- |
| Complexity | Cao. |
| Usability | Co the kho voi nguoi lon tuoi. |
| Elderly-family friendliness | Trung binh-Thap neu UI phuc tap. |
| Backend impact | Can schema visibility/ACL, query filtering, tests. |
| Frontend impact | Can controls tren form/list/detail. |
| Migration risk | Cao hon vi phai backfill default visibility. |

Pros:

- Linh hoat.
- Bao ve tot hon cho media/memorial/timeline.

Cons:

- De permission explosion.
- Can rat nhieu test va UX ro rang.

### C. Role-based visibility

Visibility dua tren role: ADMIN/MEMBER/VIEWER.

| Criteria | Assessment |
| --- | --- |
| Complexity | Trung binh. |
| Usability | De giai thich hon ACL. |
| Elderly-family friendliness | Kha tot neu role copy ro. |
| Backend impact | Can query/endpoint checks theo role. |
| Frontend impact | An/hien state theo role. |
| Migration risk | Trung binh. |

Pros:

- Phu hop role model hien co.
- VIEWER co the bi gioi han doc module nhay cam.

Cons:

- Role qua tho, khong xu ly tot truong hop "chi share album nay voi co A".
- Co the lam ADMIN/MEMBER qua rong.

### D. Hybrid visibility

Mac dinh family-wide, them controls rieng cho module nhay cam.

Vi du:

- Messenger: participant-based, nhu hien co.
- Notifications: per-user, nhu hien co.
- Albums: co private album sau.
- Memorial: co restricted/approval sau.
- Kitchen: family-wide.
- Timeline: family-wide truoc, private post sau neu can.

| Criteria | Assessment |
| --- | --- |
| Complexity | Trung binh. |
| Usability | Tot neu giu defaults ro. |
| Elderly-family friendliness | Tot hon owner-controlled day du. |
| Backend impact | Tang theo tung module, khong can rewrite toan bo. |
| Frontend impact | Them controls co chon loc. |
| Migration risk | Thap-Trung binh neu additive/nullable. |

Pros:

- An toan va thuc te nhat cho giai doan hien tai.
- Khong over-engineer.
- Tap trung vao module rui ro cao truoc.

Cons:

- Can document rules ro de khong moi module mot kieu.
- Ve lau dai co the can chuan hoa.

## 5. Recommended safest current-stage approach

Khuyen nghi hien tai: **Hybrid visibility with family-wide default**.

Cu the:

- Giu family-wide read visibility cho module it nhay cam hon nhu Kitchen.
- Giu Messenger participant-based.
- Giu Notifications per-user.
- Chua them granular privacy settings ngay.
- Uu tien sau nay them private controls cho Albums/media va Memorial truoc, vi rui ro cao.
- Neu can gioi han VIEWER, bat dau bang UI/read rules don gian theo role, khong tao ACL phuc tap.

Ly do:

- Phu hop kich thuoc project hien tai.
- Giam rui ro permission explosion.
- De giai thich cho nguoi dung gia dinh.
- Khong can rewrite ownership architecture.
- Cho phep cai thien privacy theo tung module nhay cam.

## 6. Future privacy feature ideas

Chua implement, chi la y tuong:

- Private albums.
- Hidden memorials.
- Profile visibility settings.
- Invite-only chat rooms.
- Family branch visibility.
- Private timeline drafts.
- Selected-viewer memories.
- Hide email/contact info.
- Child profile limited visibility.
- Per-user notification preferences.
- Memorial tribute approval.
- Album download permission.

## 7. Explicit warnings

Can tranh:

- Over-complicated permissions early.
- Permission explosion voi qua nhieu toggle.
- Rewriting ownership architecture khi chua co product rules.
- Mac dinh qua mo cho albums/media/memorial.
- Broadcast family-wide cho notifications rieng tu.
- Cho ADMIN xem private chat/media neu chua co owner decision.
- Dung thuat ngu security phuc tap trong UI cho nguoi dung gia dinh.

Defaults phai de hieu cho non-technical family members:

- "Ca gia dinh co the xem"
- "Chi minh toi"
- "Chi thanh vien duoc moi"
- "Nguoi quan ly"
- "Thanh vien"
- "Chi xem"

## 8. Do Not Yet Implement

Khong nen lam trong giai doan nay:

- Granular ACL system.
- Enterprise RBAC.
- Encrypted private vault.
- Legal/compliance systems.
- Multi-tenant org architecture.
- Per-field permission engine.
- Cross-family sharing.
- Complex consent workflow.
- Data retention/legal hold.
- Admin impersonation.

## 9. Migration safety notes

Neu sau nay them privacy settings:

- Uu tien additive schema changes.
- Dung nullable/default visibility fields.
- Preserve existing records.
- Khong rewrite ownership fields.
- Khong doi auth flow cung luc.
- Backfill visibility theo default ro, vi du `FAMILY`.
- Test read filters theo ADMIN/MEMBER/VIEWER.
- Them privacy theo tung module, khong rollout toan bo mot lan.
- Neu co selected users, can can nhac join table rieng va tests ky.

## 10. Owner decision checklist

Can quyet dinh truoc khi code privacy settings:

- Should VIEWER see all family members?
- Should memorials be family-wide or restricted?
- Can private albums exist?
- Can direct/private chat rooms exist?
- Should recipes be public to all family members?
- Can users hide parts of their profile?
- Should notifications ever be shared family-wide?
- ADMIN co duoc xem private chat/albums/memorial khong?
- Guest/non-family VIEWER co khac family VIEWER khong?
- Default visibility cua timeline post la gi?
- Private content co can approval/moderation khong?
- Co can branch-based family tree visibility khong?

## 11. Ket luan

Digital Family Hub nen giu privacy model de hieu trong giai doan hien tai: family-wide default cho noi dung chung, per-user cho notifications, participant-based cho messenger, va them controls rieng cho module rui ro cao sau. Huong an toan nhat la hybrid visibility, khong lam ACL/RBAC phuc tap cho den khi co du lieu su dung that va quyet dinh owner ro rang.
