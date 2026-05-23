# HomeTree Database Review

The existing schema is a good relational foundation. `V2__performance_indexes_and_future_ready_schema.sql` adds safe performance indexes and future-ready tables without replacing backend logic.

## Improvements Added

- Prevent self-referential family relationships.
- Deduplicate relationship edges by `(source_member_id, target_member_id, type)`.
- Add compound indexes for family tree branch/generation views.
- Add timeline cursor indexes for feed pagination.
- Add tagged-member reverse lookup index.
- Add album/media chronological indexes.
- Add chat cursor and sender indexes.
- Add notification unread partial indexes.
- Add future `media_links` table for reusing media across albums, posts, members, tributes, and recipes.
- Add future `chat_message_reads` table for proper group-chat read receipts.
- Add future `family_member_closure` table for fast ancestor/descendant graph queries.

## Cursor Pagination Direction

Offset pagination is acceptable for early pages, but timeline and chat should move to cursor pagination:

- Timeline cursor: `(occurred_at, id)`.
- Chat cursor: `(created_at, id)`.
- Notifications cursor: `(created_at, id)`.

This avoids slow deep offsets as memories and messages grow.

## Media Direction

The current `media_assets` table supports direct links to album/post/member. Long term, keep the asset once and connect it through `media_links`. This avoids duplicate uploads when one photo belongs to an album, a timeline post, and a memorial tribute.
