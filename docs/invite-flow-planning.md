# Invite Flow Planning

## 1. Muc dich

Tai lieu nay lap ke hoach an toan cho Invite Flow trong Digital Family Hub. Day chi la tai lieu thiet ke, chua trien khai invite flow, chua doi backend/frontend, chua doi database schema, chua them migration, chua doi authentication logic va chua tich hop email/SMTP provider.

Invite flow la tinh nang nhay cam vi no quyet dinh ai duoc vao khong gian rieng cua gia dinh.

## 2. Vi sao invite flow nhay cam

### Account takeover risk

Neu invite token bi lo, nguoi khac co the co gang dung token de vao hub gia dinh. Neu flow lien ket sai voi OAuth account, user khong dung co the chiem quyen truy cap.

### Accidental access to private family data

Digital Family Hub co anh, ky uc, chat, memorial, family tree va notifications. Moi sai nguoi hoac sai role co the lam lo du lieu ca gia dinh.

### Role escalation risk

Neu invite cho phep gan role `ADMIN` hoac `MEMBER` khong duoc kiem soat, user moi co the co nhieu quyen hon y dinh. Role escalation dac biet nguy hiem neu invite link bi forward.

### Token leakage risk

Invite link co the bi copy, chup man hinh, gui nham chat, nam trong browser history hoac log. Token can co expiration, single-use va khong nen luu raw token neu co the tranh.

## 3. Requires owner decision

Cac quyet dinh sau can project owner/user xac nhan truoc khi code:

| Decision | Options | Khuyen nghi ban dau |
| --- | --- | --- |
| Who can invite users? | ADMIN only, ADMIN + selected MEMBER, anyone can request invite | ADMIN only cho MVP. |
| Default role for invited users | VIEWER, MEMBER, custom per invite | VIEWER hoac MEMBER, tuy muc dich hub. Neu chua chac, chon VIEWER. |
| Invitee email must match token email? | Required, optional, no email binding | Required neu invite gan email. |
| Invite token expiration | 24h, 7 days, 30 days, no expiration | 7 days cho MVP. |
| Invites can be revoked? | Yes, no | Yes. |
| Invite links are single-use? | Yes, no | Yes. |
| Google OAuth email must match invite email? | Required, optional, no check | Required neu invite co email. |
| Non-family guests allowed? | Yes with VIEWER, no, later | Owner decision. |
| Email sending required immediately? | Manual copy link, email provider, both | Manual copy link first. |
| Can invite assign ADMIN? | Yes, no, only existing ADMIN can promote later | No for MVP; promote later via admin management. |
| Should invite create account before OAuth? | Pre-register invite only, create after OAuth | Create/link after OAuth succeeds. |

## 4. Possible rollout approaches

### A. Manual invite code only

Admin creates a short invite code or link manually, then copies it to the invitee.

| Criteria | Assessment |
| --- | --- |
| Complexity | Low-Medium. |
| Security risk | Medium. Token leakage still possible, but scope can be limited with expiration/single-use. |
| Backend impact | Need invite table, token hashing, accept endpoint, validation. |
| Frontend impact | Invite accept page and simple admin create/revoke UI later. |
| Migration impact | Requires additive invite table if implemented. |
| Rollback difficulty | Low-Medium if invite table is isolated. |

Pros:

- No SMTP/email dependency.
- Good for student/MVP stage.
- Easy to manually test.

Cons:

- Admin must copy/share links carefully.
- No delivery tracking.

### B. Admin-created email invite

Admin enters invitee email and system sends an invite email.

| Criteria | Assessment |
| --- | --- |
| Complexity | Medium-High. |
| Security risk | Medium. Better email binding, but adds email deliverability and provider secrets. |
| Backend impact | Invite table, token hashing, email service, provider config, retry/error handling. |
| Frontend impact | Admin invite form with email/status, accept page. |
| Migration impact | Invite table plus maybe delivery status fields. |
| Rollback difficulty | Medium due to provider/config coupling. |

Pros:

- Better UX.
- Easier to bind invite to email.

Cons:

- SMTP/provider setup is not finalized.
- More production/security surface.

### C. Domain/email allowlist

Admins configure allowed emails or domains. Users with matching OAuth email can join.

| Criteria | Assessment |
| --- | --- |
| Complexity | Medium. |
| Security risk | Medium-High, depending on domain policy. |
| Backend impact | Allowlist table/config, auth join logic changes. |
| Frontend impact | Admin allowlist UI, join/error screens. |
| Migration impact | Requires schema if dynamic allowlist. |
| Rollback difficulty | Medium. |

Pros:

- Useful for organizations or school/team setups.
- Reduces per-user invite work.

Cons:

- Risky for family app. A shared domain does not prove family membership.
- Domain-wide auto-join is too broad for private family data.

### D. Full invite + email provider integration

Complete production-grade invite system with email templates, provider integration, delivery status, resend, revoke and audit.

| Criteria | Assessment |
| --- | --- |
| Complexity | High. |
| Security risk | Medium if done well, high if rushed. |
| Backend impact | Invite model, email service, provider config, audit, rate limits. |
| Frontend impact | Admin invite management, accept flow, resend/revoke UI, error states. |
| Migration impact | Multiple additive tables/fields likely. |
| Rollback difficulty | High. |

Pros:

- Best long-term UX.
- Clear admin operations.

Cons:

- Too much for the current phase.
- Requires secrets, provider decisions and production deployment planning.

## 5. Recommended rollout for current project stage

### Phase A: Docs and decision checklist only

Current phase. Finalize owner decisions before code:

- Who can invite.
- Default role.
- Token expiration.
- Email matching policy.
- Single-use/revoke policy.
- Manual link vs email.

### Phase B: Manual single-use invite codes

Safest first implementation:

- ADMIN creates invite.
- Invite has hashed token, expiration, status and assigned role.
- Invite is single-use.
- Invite acceptance requires OAuth login.
- If invite has email, OAuth email must match.
- No email provider yet; admin manually copies link.

### Phase C: Admin invite management UI

Add simple UI:

- List pending/accepted/revoked/expired invites.
- Create invite.
- Copy invite link.
- Revoke invite.
- Show clear status.

### Phase D: Email provider integration

Only after production secrets/deployment policy is clear:

- SMTP/provider config.
- Email templates.
- Resend invite.
- Delivery failure handling.
- Rate limiting and monitoring.

## 6. Backend planning notes

### Invite token table

Future table could contain:

- `id`
- `tokenHash`
- `email`
- `role`
- `createdBy`
- `createdAt`
- `expiresAt`
- `acceptedAt`
- `revokedAt`
- `acceptedBy`

Do not store raw token if avoidable.

### Token hashing

- Generate random high-entropy token.
- Store only hash in DB.
- Send raw token only in invite link once.
- Compare hash on accept.

### Expiration

- Every invite should expire.
- Expired invites should not reveal whether email/user exists.
- UI should show friendly expired invite page.

### Single-use semantics

- Once accepted, invite cannot be reused.
- Acceptance should be transactional.
- Concurrent accept attempts should not create duplicate access.

### Role assignment

- Default role should be conservative.
- Avoid assigning `ADMIN` through invite for MVP.
- Role should be validated server-side, not trusted from frontend.

### Audit trail later

Future audit fields/actions:

- Who created invite.
- Who revoked invite.
- Who accepted invite.
- When role was assigned.

Full audit infrastructure is not needed for first manual invite code phase, but schema should leave room for it.

### Last-admin safety

Invite flow should not be used to bypass admin safety rules. If invite management later includes role changes, it must preserve last-admin protection.

## 7. Frontend planning notes

### Invite accept page

Possible route:

```text
/invite/{token}
```

Flow:

1. User opens invite link.
2. Page validates invite status or waits until OAuth login.
3. User signs in with Google OAuth if not authenticated.
4. Backend accepts invite if token/email/policy match.
5. User lands in app with assigned role.

### Expired invite page

Needs friendly copy:

- "This invite has expired."
- "Ask the family admin for a new invite."

### Invalid invite page

Avoid revealing details:

- Do not say whether token, email or account exists.
- Use generic message like "This invite link is invalid or unavailable."

### Role explanation copy

Use simple language:

- ADMIN: "Can manage family hub settings and members."
- MEMBER: "Can add and manage their own family content."
- VIEWER: "Can view allowed family content."

### Simple admin invite creation UI later

Initial UI should include:

- Email field if email-bound invites are chosen.
- Role dropdown with conservative default.
- Expiration display.
- Create invite button.
- Copy link button.
- Revoke action with confirmation.

## 8. Security protections

Required protections for future implementation:

- Never store raw tokens if avoidable.
- Do not expose whether arbitrary emails exist.
- Rate limit invite acceptance later.
- Require OAuth email match when policy is decided.
- Prevent role escalation.
- Prevent reusable leaked links.
- Use single-use tokens.
- Use expiration.
- Allow revoke before accept.
- Do not allow invite to create ADMIN unless explicitly approved with guardrails.
- Do not log raw invite tokens.
- Keep invite acceptance server-authoritative.

## 9. Do Not Yet Implement

Do not implement these in the first invite phase:

- SMTP/email provider.
- Public registration.
- Domain-wide auto-join.
- Complex org hierarchy.
- Full audit log system.
- External identity provider beyond current Google OAuth/JWT.
- Invite analytics.
- Bulk invites.
- Guest expiration automation.
- QR-code invites.
- Admin impersonation.

## 10. Migration safety notes

If invite flow is implemented later:

- Use additive schema changes only.
- Keep invite tables isolated from existing ownership models.
- Do not rewrite auth/user tables during invite rollout unless absolutely necessary.
- Preserve existing OAuth/JWT login behavior.
- Add nullable fields where possible.
- Avoid mandatory backfills for existing users.
- Make token/status fields explicit.
- Prefer reversible rollout: disable invite acceptance without breaking existing users.

## 11. API planning ideas

Only ideas, not implemented:

```text
POST /api/admin/invites
GET /api/admin/invites
DELETE /api/admin/invites/{inviteId}
POST /api/invites/{token}/accept
GET /api/invites/{token}/status
```

Principles:

- Create/revoke/list invites should require `ADMIN`.
- Accept invite should validate token, expiration, single-use status and email policy.
- Responses should avoid leaking whether an email has an existing account.
- Token should stay in path or body only long enough to accept; never echo raw token after creation except the one-time copy link.

## 12. Owner decision checklist

Before implementation, owner should answer:

- Who can invite users?
- What is the default role?
- Can invite assign `ADMIN`?
- Must invitee email match token email?
- Must Google OAuth email match invite email?
- How long should invite tokens last?
- Can invites be revoked?
- Are invite links single-use?
- Are non-family guests allowed?
- Is manual copy link enough for MVP?
- When should email sending be added?
- What should happen if someone signs in without an invite?

## 13. Ket luan

Safest path is to avoid email/provider work first and implement manual single-use invite codes only after product decisions are made. Invite flow should be conservative by default: ADMIN-only creation, short expiration, single-use tokens, OAuth email matching when email-bound and no ADMIN assignment through invites in MVP.
