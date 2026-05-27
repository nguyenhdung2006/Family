# Realtime Notifications Planning

## 1. Muc dich

Tai lieu nay lap ke hoach an toan cho realtime notifications trong Digital Family Hub. Day chi la tai lieu thiet ke, chua trien khai WebSocket events, chua doi schema, chua doi API contract va chua thay doi frontend/backend code.

Muc tieu tuong lai la giup bang thong bao cap nhat nhanh hon ma van giu rieng tu theo tung nguoi dung.

## 2. He thong notifications hien tai

### REST behavior hien co

Notifications hien dang dung REST endpoint duoi `/api/notifications`:

- `GET /api/notifications`: lay danh sach notifications cua user dang dang nhap.
- `POST /api/notifications`: tao notification.
- `PUT /api/notifications/{id}`: cap nhat notification.
- `DELETE /api/notifications/{id}`: xoa notification.
- `PATCH /api/notifications/{id}/read`: danh dau da doc.

Response hien co gom cac truong chinh:

- `id`
- `createdById`
- `createdByName`
- `type`
- `title`
- `body`
- `scheduledFor`
- `readAt`

Ghi chu hien trang: trong code hien tai, `createdById`/`createdByName` duoc map tu `recipient`. Neu sau nay can phan biet nguoi tao notification va nguoi nhan notification, can co quyet dinh schema san pham rieng truoc khi code.

### Ownership va privacy hien tai

Notifications da duoc scope theo current user:

- `listNotifications(principal)` resolve current user bang `Principal.getName()` va `AppUserRepository.findByEmailIgnoreCase(...)`.
- Danh sach dung `findByRecipientIdOrderByCreatedAtDesc(currentUser.getId())`.
- `markRead` chi cho phep danh dau notification neu `recipient` la current user.
- Mutation update/delete hien cho phep `ADMIN` quan ly, va `MEMBER` quan ly notification gan voi chinh minh theo logic hien co.
- Unauthorized ownership failure tra `404`, giu pattern tranh lo du lieu rieng.

### Read/unread behavior

Unread/read hien duoc xac dinh bang `readAt`:

- `readAt == null`: notification chua doc.
- `readAt != null`: notification da doc.
- `PATCH /read` set `readAt = Instant.now()`.

Chua co endpoint rieng cho unread count. Frontend co the tinh unread count tu danh sach neu can.

### Frontend flow hien tai

Frontend notifications hien dung:

- `useNotifications()` voi React Query.
- `queryKey: queryKeys.notifications`.
- `refetchInterval: 60_000`, tuc polling moi 60 giay.
- Create/update/delete/mark-read deu invalidate `queryKeys.notifications`.
- UI an create/edit/delete cho `VIEWER`.
- UI hien edit/delete cho `ADMIN` hoac owner theo `createdById`.

Hien chua co realtime subscription cho notifications.

## 3. WebSocket/STOMP infrastructure hien tai

### Messenger realtime flow

Project da co STOMP/WebSocket cho Messenger:

- Endpoint WebSocket: `/ws`.
- Application destination prefix: `/app`.
- Simple broker destinations: `/topic`, `/queue`.
- Chat room topics dung pattern `/topic/rooms/{roomId}`.
- Browser client co the reuse token cookie `HOMETREE_TOKEN` qua handshake.
- CONNECT co the authenticate bang cookie/session principal hoac bearer token.

### Participant/security behavior

WebSocket inbound interceptor hien co:

- Authenticate khi `CONNECT`.
- Validate `SUBSCRIBE` vao `/topic/rooms/{roomId}`.
- Chi cho phep subscribe chat room neu user co messaging access va la participant cua room.
- Validate `SEND` vao `/app/**`; `VIEWER` khong duoc send WebSocket messages.

Day la nen tang co the tai su dung, nhung notifications can kenh rieng theo user, khong nen dung broadcast chung.

### Infrastructure co the tai su dung

Co the tai su dung:

- `/ws` endpoint.
- JWT/cookie handshake.
- `SimpMessagingTemplate` neu can push event tu service sau nay.
- Channel interceptor pattern de validate subscription.
- React Query invalidation pattern tren frontend.

Khong nen tai su dung truc tiep:

- `/topic/rooms/{roomId}` cho notifications.
- Chat participant rules cho notifications, vi notifications phai scope theo user recipient, khong theo room.

## 4. Cac realtime event flow co the thiet ke

Day la y tuong tuong lai, chua implement:

### notification.created

Khi tao notification moi:

1. Backend luu notification bang REST/service nhu hien tai.
2. Backend gui event den kenh rieng cua recipient.
3. Frontend nhan event va invalidate/refetch `queryKeys.notifications`, hoac prepend item neu payload day du va an toan.

Payload toi thieu:

```json
{
  "event": "notification.created",
  "notificationId": "...",
  "recipientId": "...",
  "createdAt": "..."
}
```

Khuyen nghi ban dau: chi gui `notificationId`/event metadata va de frontend refetch, tranh stale payload.

### notification.read

Khi notification duoc mark read:

1. REST `PATCH /api/notifications/{id}/read` van la source of truth.
2. Backend co the push event den user hien tai va cac device khac cua cung user.
3. Frontend invalidate danh sach hoac update `readAt` neu payload co gia tri moi.

### notification.deleted

Khi notification bi xoa:

1. Backend delete theo ownership rule hien co.
2. Push event den recipient affected.
3. Frontend remove item khoi cache hoac invalidate/refetch.

### unread-count-updated

Event nhe de cap nhat badge:

```json
{
  "event": "unread-count-updated",
  "unreadCount": 3
}
```

Phu hop rollout dau tien vi payload nho, it rui ro lo noi dung notification.

## 5. Kenh/topic de xuat

Can tranh broadcast-to-all. Mot so lua chon:

### Option A: User queue cua Spring

Pattern:

```text
/user/queue/notifications
```

Uu diem:

- Phu hop per-user delivery.
- Giam nguy co user tu subscribe vao topic cua nguoi khac.
- Khop voi Spring STOMP user destination.

Can kiem tra:

- Principal name phai on dinh, dang la email.
- Multi-device cua cung user co nhan duoc dung khong.

### Option B: Topic theo user id

Pattern:

```text
/topic/users/{userId}/notifications
```

Uu diem:

- De debug.
- Tuong minh.

Rui ro:

- Phai validate subscription rat chat.
- User co the doan UUID va thu subscribe.
- Sai interceptor se gay privacy leak.

Khuyen nghi: bat dau voi **Option A: `/user/queue/notifications`** neu Spring user destination dap ung duoc nhu cau.

## 6. Rollout an toan duoc khuyen nghi

### Phase A: Unread badge refresh only

Muc tieu:

- Khong push full notification body.
- Chi cap nhat unread count hoac trigger invalidate.
- Giu REST list la source of truth.

Can lam sau nay:

- Them frontend subscription nhan event nhe.
- Khi co event, invalidate `queryKeys.notifications`.
- Neu chua co unread count endpoint, frontend co the refetch list va tinh tu `readAt`.

Ly do an toan:

- It rui ro duplicate UI.
- It rui ro lo noi dung.
- De rollback ve polling 60 giay.

### Phase B: Realtime notification push

Muc tieu:

- Push event khi notification created/read/deleted.
- Van uu tien invalidate/refetch thay vi mutate cache phuc tap.

Can lam sau khi Phase A on:

- Backend emit event trong create/update/delete/mark-read.
- Frontend subscription invalidates notifications query.
- Them tests cho per-user channel neu co pattern.

### Phase C: Toast/banner notifications

Muc tieu:

- Hien toast/banner khi notification moi den.
- Chi hien neu user dang online va event hop le.

Can can nhac:

- Khong hien noi dung nhay cam tren shared screen neu gia dinh dung chung thiet bi.
- Can nut dismiss ro rang.
- Khong spam toast khi reconnect refetch.

### Phase D: Multi-device sync

Muc tieu:

- Khi mot device mark read/delete, cac device khac cua cung user cap nhat.

Can can nhac:

- Event ordering.
- Duplicate deliveries.
- Conflict khi hai tab thao tac cung luc.

## 7. Frontend concerns

### Reconnect behavior

- STOMP client can reconnect an toan khi mat mang.
- Sau reconnect nen invalidate notifications mot lan.
- Can tranh reconnect loop gay load cao.

### Duplicate events

- Cung mot notification co the den nhieu lan sau reconnect.
- Neu frontend mutate cache truc tiep, can de-duplicate bang `notificationId`.
- Khuyen nghi dau tien: invalidate/refetch de server list lam source of truth.

### Optimistic UI risks

- Create/update/delete/read hien dang di qua REST mutation.
- Neu vua optimistic update vua nhan realtime event, UI co the flicker hoac duplicate.
- Nen giu optimistic logic hien co toi thieu; realtime event chi invalidate query trong giai doan dau.

### Stale cache handling

- React Query dang la trung tam cache.
- Realtime event nen goi `queryClient.invalidateQueries({ queryKey: queryKeys.notifications })`.
- Neu sau nay co unread badge query rieng, can invalidate badge va list dong bo.

### UI/UX

- Polling 60 giay hien tai nen giu lam fallback.
- Khong nen them toast truoc khi realtime core on dinh.
- VIEWER van duoc nhan/read notifications neu product cho phep, nhung khong co mutation tao/sua/xoa.

## 8. Backend concerns

### Per-user channels/topics

- Notifications phai di den recipient cu the.
- Khong dung `/topic/notifications` global.
- Neu dung `/user/queue/notifications`, can dam bao Principal name match luc send.

### Authorization checks

- REST ownership rules van la source of truth.
- WebSocket SUBSCRIBE phai yeu cau authenticated user.
- Neu dung topic co user id, interceptor phai check user id/email khop current principal.

### Avoiding notification leaks

- Khong broadcast full notification body len topic chung.
- Khong dua recipient id cua nguoi khac vao topic client co the doan.
- Unauthorized subscription nen reject som.

### Avoiding broadcast-to-all mistakes

Can tranh cac pattern:

```text
/topic/notifications
/topic/family/notifications
/topic/users
```

Neu can family-wide announcement, backend van nen tao notification rieng cho tung recipient roi push den tung user queue.

### Event ordering

- WebSocket event co the den truoc khi REST cache invalidate xong.
- Created/read/deleted events co the den khac thu tu tren multi-device.
- Frontend nen refetch va de backend order by `createdAt desc` nhu hien tai.

### Transaction timing

- Khong push event truoc khi transaction commit.
- Neu sau nay emit event trong service, nen can nhac after-commit event listener.
- Tranh gui realtime event cho notification bi rollback.

## 9. Do Not Yet Implement

Chua nen trien khai cac phan sau trong giai doan dau:

- Push notifications.
- Firebase Cloud Messaging.
- APNS.
- Email notification engine.
- SMS notifications.
- Background scheduler.
- Kafka/event bus.
- Distributed realtime infrastructure.
- Notification digest.
- Cross-family notification routing.
- Advanced notification preferences.
- Full audit stream.

## 10. Risk assessment

| Risk | Muc do | Mo ta | Giam thieu |
| --- | --- | --- | --- |
| Privacy leaks | Cao | Gui notification den sai user hoac topic global. | Dung per-user queue, validate subscription, khong broadcast chung. |
| Duplicate deliveries | Trung binh | Reconnect hoac multi-tab co the nhan lai event. | De-duplicate hoac invalidate/refetch thay vi append truc tiep. |
| Reconnect storms | Trung binh | Client mat mang lien tuc gay reconnect nhanh. | Backoff reconnect, giu polling fallback. |
| Stale unread counts | Trung binh | Read/delete tren tab khac lam badge sai. | Invalidate query sau event, refetch sau reconnect. |
| WebSocket auth edge cases | Cao | Cookie/JWT het han nhung socket con mo. | Kiem tra auth khi CONNECT/SUBSCRIBE, dong/reconnect khi auth fail. |
| Event ordering | Thap den trung binh | Created/read/deleted den sai thu tu. | REST list la source of truth, refetch sau event. |
| Over-engineering | Trung binh | Them bus/scheduler/toast qua som. | Rollout theo Phase A-D, khong tron feature work. |

## 11. Kiem thu de xuat khi trien khai sau nay

Manual role/privacy matrix:

- User A nhan notification thi User B khong thay event.
- VIEWER chi nhan/read theo product rule, khong tao/sua/xoa.
- ADMIN mutation khong lam leak notification sang user khac.
- Multi-tab cung user cap nhat unread state.
- Reconnect sau khi mat mang khong duplicate danh sach.
- REST polling fallback van hoat dong neu WebSocket fail.

Backend tests nen co:

- Event chi gui den recipient.
- Unauthorized subscribe bi reject.
- No global broadcast.
- Event khong gui khi transaction fail.

Frontend tests/manual checks nen co:

- Invalidate notifications query khi nhan event.
- Clear duplicate UI after reconnect.
- Error state khi socket fail khong lam hong list REST.

## 12. Ket luan

Huong an toan nhat la giu REST notifications lam source of truth va them realtime theo tung buoc. Bat dau bang **Phase A: unread badge refresh only** hoac event nhe chi de invalidate React Query. Sau khi privacy va reconnect on dinh, moi tien toi push full notification events, toast UI va multi-device sync.
