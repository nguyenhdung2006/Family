# Manual Role Matrix Test Checklist

## 1. Purpose

Checklist này dùng để kiểm thử thủ công quyền theo vai trò và ownership trong Digital Family Hub.

Mục tiêu:

- Xác nhận `ADMIN`, `MEMBER`, `VIEWER` nhìn thấy đúng UI.
- Xác nhận `MEMBER` chỉ sửa/xóa nội dung do chính mình tạo.
- Xác nhận `ADMIN` quản lý được nội dung ở các module đã hỗ trợ.
- Xác nhận backend vẫn chặn quyền khi gọi API trực tiếp, kể cả khi UI bị bypass.
- Xác nhận read/list behavior hiện có không bị regression.

## 2. Test Accounts Needed

Cần ít nhất 4 tài khoản test:

- `ADMIN`: một tài khoản quản trị.
- `MEMBER_A`: một tài khoản member dùng để tạo nội dung owner.
- `MEMBER_B`: một tài khoản member khác để test non-owner access.
- `VIEWER`: một tài khoản chỉ xem.

Khuyến nghị seed hoặc tạo trước dữ liệu:

- Timeline post do `MEMBER_A` tạo.
- Album do `MEMBER_A` tạo, có ít nhất một media item.
- Recipe do `MEMBER_A` tạo.
- Memorial tribute do `MEMBER_A` tạo.
- Notification/alert do `MEMBER_A` tạo.
- Chat room có `MEMBER_A` là participant.
- Một chat room khác không có `MEMBER_B` là participant.

## 3. Global Expected Rules

- `ADMIN` generally can manage all owned content where supported.
- `MEMBER` can create supported content and edit/delete only their own content.
- `VIEWER` can view allowed content but must not create/edit/delete.
- Unauthorized ownership access should not expose private records.
- UI should hide mutation controls where the user lacks permission.
- Backend should still enforce permissions even if UI is bypassed.
- Ownership failures should return `404` where the privacy pattern applies.
- Authentication failures should return `401`.
- Role failures should return `403` or equivalent Spring Security denial, depending on route.

## 4. Module-by-module Checklist

### Timeline Posts

ADMIN behavior:

- Log in as `ADMIN`.
- Open Timeline.
- Verify all visible posts can show edit/delete controls.
- Edit a post created by `MEMBER_A`.
- Delete a test post created by `MEMBER_A`.
- Verify list refreshes after edit/delete.

MEMBER owner behavior:

- Log in as `MEMBER_A`.
- Create a new timeline post.
- Verify the post appears in the list.
- Verify edit/delete controls appear for that post.
- Edit the post.
- Delete the post.

MEMBER non-owner behavior:

- Log in as `MEMBER_B`.
- Open Timeline and find a post created by `MEMBER_A`.
- Verify edit/delete controls do not appear.
- Attempt direct API call to update `MEMBER_A` post.
- Expected: backend returns `404`.
- Attempt direct API call to delete `MEMBER_A` post.
- Expected: backend returns `404`.

VIEWER behavior:

- Log in as `VIEWER`.
- Open Timeline.
- Verify posts can be read.
- Verify composer is hidden.
- Verify edit/delete controls are hidden.
- Attempt direct create/update/delete API call.
- Expected: backend denies mutation.

UI controls visibility:

- Composer visible only to users allowed to create.
- Edit/delete visible only to `ADMIN` or owner.

Backend/API enforcement:

- `PUT /api/timeline/posts/{postId}` rejects non-owner `MEMBER`.
- `DELETE /api/timeline/posts/{postId}` rejects non-owner `MEMBER`.

### Albums and Media Removal

ADMIN behavior:

- Log in as `ADMIN`.
- Open Albums.
- Verify edit/delete controls appear for albums.
- Edit an album created by `MEMBER_A`.
- Remove media from an album created by `MEMBER_A`.
- Delete a test album created by `MEMBER_A`.

MEMBER owner behavior:

- Log in as `MEMBER_A`.
- Create an album.
- Upload/attach media if upload is configured.
- Verify edit/delete album controls appear for own album.
- Verify remove media control appears for media in own album.
- Edit own album.
- Remove media from own album.
- Delete own test album.

MEMBER non-owner behavior:

- Log in as `MEMBER_B`.
- Open an album created by `MEMBER_A`.
- Verify edit/delete album controls are hidden.
- Verify remove media controls are hidden.
- Attempt direct API update/delete on `MEMBER_A` album.
- Expected: backend returns `404`.
- Attempt direct API media removal from `MEMBER_A` album.
- Expected: backend returns `404`.

VIEWER behavior:

- Log in as `VIEWER`.
- Verify albums/media can be viewed.
- Verify create/edit/delete/upload/remove controls are hidden.
- Attempt direct mutation API calls.
- Expected: backend denies mutation.

UI controls visibility:

- Album edit/delete visible only to `ADMIN` or album owner.
- Upload/remove media visible only to `ADMIN` or album owner.

Backend/API enforcement:

- `PUT /api/albums/{albumId}` rejects non-owner `MEMBER`.
- `DELETE /api/albums/{albumId}` rejects non-owner `MEMBER`.
- `DELETE /api/albums/{albumId}/media/{mediaId}` rejects non-owner `MEMBER`.

### Kitchen Recipes

ADMIN behavior:

- Log in as `ADMIN`.
- Open Kitchen.
- Verify edit/delete controls appear on recipes.
- Edit a recipe created by `MEMBER_A`.
- Delete a test recipe created by `MEMBER_A`.

MEMBER owner behavior:

- Log in as `MEMBER_A`.
- Create a recipe.
- Verify edit/delete controls appear for own recipe.
- Edit own recipe.
- Delete own test recipe.

MEMBER non-owner behavior:

- Log in as `MEMBER_B`.
- Open recipe created by `MEMBER_A`.
- Verify edit/delete controls are hidden.
- Attempt direct update/delete API call on `MEMBER_A` recipe.
- Expected: backend returns `404`.

VIEWER behavior:

- Log in as `VIEWER`.
- Verify recipes can be viewed.
- Verify add/edit/delete controls are hidden.
- Attempt direct create/update/delete API call.
- Expected: backend denies mutation.

UI controls visibility:

- Add form hidden for `VIEWER`.
- Edit/delete visible only to `ADMIN` or recipe owner.

Backend/API enforcement:

- `PUT /api/kitchen/recipes/{recipeId}` rejects non-owner `MEMBER`.
- `DELETE /api/kitchen/recipes/{recipeId}` rejects non-owner `MEMBER`.

### Memorial Tributes

ADMIN behavior:

- Log in as `ADMIN`.
- Open Memorial.
- Select a memorial member.
- Verify edit/delete controls appear on tributes.
- Edit a tribute created by `MEMBER_A`.
- Delete a test tribute created by `MEMBER_A`.

MEMBER owner behavior:

- Log in as `MEMBER_A`.
- Create a tribute for a deceased member.
- Verify edit/delete controls appear for own tribute.
- Edit own tribute.
- Delete own test tribute.

MEMBER non-owner behavior:

- Log in as `MEMBER_B`.
- View tribute created by `MEMBER_A`.
- Verify edit/delete controls are hidden.
- Attempt direct update/delete API call on `MEMBER_A` tribute.
- Expected: backend returns `404`.

VIEWER behavior:

- Log in as `VIEWER`.
- Verify memorial members and tributes can be viewed.
- Verify add/edit/delete controls are hidden.
- Attempt direct create/update/delete API call.
- Expected: backend denies mutation.

UI controls visibility:

- Tribute form hidden for `VIEWER`.
- Edit/delete visible only to `ADMIN` or tribute author.

Backend/API enforcement:

- `PUT /api/memorials/{memberId}/tributes/{tributeId}` rejects non-author `MEMBER`.
- `DELETE /api/memorials/{memberId}/tributes/{tributeId}` rejects non-author `MEMBER`.
- Wrong `memberId` path should not expose tribute.

### Alerts / Notifications

ADMIN behavior:

- Log in as `ADMIN`.
- Open Alerts/Notifications.
- Verify notifications list loads.
- Verify edit/delete controls appear for manageable notifications.
- Edit a test notification.
- Delete a test notification.

MEMBER owner behavior:

- Log in as `MEMBER_A`.
- Create a notification.
- Verify it appears in the notification list.
- Verify edit/delete controls appear for own notification.
- Edit own notification.
- Mark it read.
- Delete own test notification.

MEMBER non-owner behavior:

- Log in as `MEMBER_B`.
- Verify `MEMBER_A` private notifications do not appear in list.
- Attempt direct update/delete API call using a known `MEMBER_A` notification id.
- Expected: backend returns `404`.

VIEWER behavior:

- Log in as `VIEWER`.
- Verify own notifications can be listed.
- Verify create/edit/delete controls are hidden.
- Attempt direct create/update/delete/mark-read mutation if not allowed by policy.
- Expected: backend denies mutation.

UI controls visibility:

- Create form hidden for `VIEWER`.
- Edit/delete visible only to `ADMIN` or notification owner.
- Mark read should only appear where current policy allows.

Backend/API enforcement:

- `GET /api/notifications` returns current user's notifications only.
- `PUT /api/notifications/{notificationId}` rejects non-owner `MEMBER`.
- `DELETE /api/notifications/{notificationId}` rejects non-owner `MEMBER`.
- `PATCH /api/notifications/{notificationId}/read` must not mark another user's notification.

### Family Tree

ADMIN behavior:

- Log in as `ADMIN`.
- Open Family Tree.
- Verify member/relationship mutation controls appear.
- Create/edit a test family member.
- Create/edit a test relationship if supported.

MEMBER owner behavior:

- Log in as `MEMBER_A`.
- Open Family Tree.
- Verify read behavior works.
- Verify mutation controls follow current policy.

MEMBER non-owner behavior:

- Log in as `MEMBER_B`.
- Verify family tree can be viewed.
- Attempt direct mutation APIs if not allowed.
- Expected: backend denies mutation according to current family rules.

VIEWER behavior:

- Log in as `VIEWER`.
- Verify Family Tree renders.
- Verify create/edit controls are hidden.
- Attempt direct mutation APIs.
- Expected: backend denies mutation.

UI controls visibility:

- Family mutation controls should not appear for `VIEWER`.
- Controls should match backend policy for `ADMIN`/`MEMBER`.

Backend/API enforcement:

- `POST /api/family/**` should be restricted to allowed role.
- `PUT /api/family/**` should be restricted to allowed role.

### Messenger Rooms/Messages

ADMIN behavior:

- Log in as `ADMIN`.
- Verify accessible rooms load.
- Create/update a room if UI supports it.
- Send message to a room where user is participant.

MEMBER owner/participant behavior:

- Log in as `MEMBER_A`.
- Open Messenger.
- Verify only participant rooms load.
- Create a room and confirm creator is participant.
- Send message in participant room.
- Verify chronological message order in UI.

MEMBER non-owner/non-participant behavior:

- Log in as `MEMBER_B`.
- Try to list/read/send messages in a room where `MEMBER_B` is not participant using direct API.
- Expected: backend returns `404`.
- Try to subscribe to restricted room topic if testing WebSocket manually.
- Expected: subscription is rejected/blocked.

VIEWER behavior:

- Log in as `VIEWER`.
- Verify viewer can only access allowed read surfaces.
- Verify message-producing controls are hidden or disabled.
- Attempt direct send API/WebSocket `/app/**`.
- Expected: backend/WebSocket rules deny mutation.

UI controls visibility:

- Room creation/update/send controls hidden for `VIEWER`.
- Room/message UI should not expose non-participant rooms.

Backend/API enforcement:

- List rooms returns only participant rooms.
- List messages requires participant.
- Send message requires participant.
- Update room requires participant or authorized role according to policy.

### Profile

ADMIN behavior:

- Log in as `ADMIN`.
- Open Profile.
- Verify profile displays current admin user details and role.

MEMBER behavior:

- Log in as `MEMBER_A`.
- Open Profile.
- Verify profile displays current member details and role.

VIEWER behavior:

- Log in as `VIEWER`.
- Open Profile.
- Verify profile displays current viewer details and role.

UI controls visibility:

- Any future edit controls should follow account ownership/security policy.

Backend/API enforcement:

- Current user endpoint should return only authenticated user's profile.
- Direct attempts to fetch another user's private profile should not be possible unless explicitly supported.

### Home / Dashboard

ADMIN behavior:

- Log in as `ADMIN`.
- Open Home.
- Verify dashboard loads without unauthorized errors.
- Verify links to all rooms work.

MEMBER behavior:

- Log in as `MEMBER_A`.
- Open Home.
- Verify dashboard loads.
- Verify recent content widgets do not show private content from other users unexpectedly.

VIEWER behavior:

- Log in as `VIEWER`.
- Open Home.
- Verify dashboard loads.
- Verify mutation CTAs are hidden or route to read-only pages.

UI controls visibility:

- Navigation should show allowed rooms.
- Dashboard should not show create/edit/delete controls for `VIEWER`.

Backend/API enforcement:

- Dashboard data requests should use existing module-level permissions.

## 5. Regression Checklist

Read/list behavior:

- Timeline list still loads for allowed users.
- Albums list still loads for allowed users.
- Album media list still loads for allowed users.
- Kitchen recipes list still loads for allowed users.
- Memorial members list still loads for allowed users.
- Memorial tributes list still loads for allowed users.
- Notifications list returns only current user's notifications.
- Messenger room list returns only rooms where current user is participant.
- Family Tree still renders and does not enter maximum update depth loop.
- Home/dashboard still loads after login.
- Profile still displays current authenticated user.

Auth behavior:

- Google OAuth login still works.
- Refresh/retry behavior does not loop on `401`.
- Logout still clears session/token state.

Role behavior:

- `VIEWER` can view allowed read pages.
- `VIEWER` cannot create/edit/delete via UI.
- Direct `VIEWER` mutation API calls are denied.
- Non-owner `MEMBER` mutation API calls return `404` for modules using privacy pattern.

Frontend behavior:

- No mutation buttons appear for unauthorized users.
- Empty/loading/error states still render.
- Forms reset after successful create/edit where expected.
- Lists invalidate/refetch after create/edit/delete.

## 6. Verification Commands

Backend:

```powershell
.\mvnw.cmd test
```

Frontend:

```powershell
npm run typecheck
.\node_modules\.bin\node.cmd node_modules\eslint\bin\eslint.js . --max-warnings=0
```

