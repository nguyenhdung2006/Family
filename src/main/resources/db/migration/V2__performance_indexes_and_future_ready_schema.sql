do $$
begin
    if not exists (
        select 1 from pg_constraint
        where conname = 'chk_family_relationships_not_self'
    ) then
        alter table family_relationships
            add constraint chk_family_relationships_not_self
            check (source_member_id <> target_member_id);
    end if;
end $$;

create unique index if not exists uq_family_relationship_edge_type
    on family_relationships(source_member_id, target_member_id, type);

create index if not exists idx_family_members_branch_generation
    on family_members(branch, generation_level, full_name);

create index if not exists idx_family_relationship_source_type
    on family_relationships(source_member_id, type);

create index if not exists idx_family_relationship_target_type
    on family_relationships(target_member_id, type);

create index if not exists idx_memory_posts_feed_cursor
    on memory_posts(occurred_at desc, id desc);

create index if not exists idx_memory_posts_event_feed
    on memory_posts(event_type, occurred_at desc, id desc);

create index if not exists idx_memory_posts_author_feed
    on memory_posts(author_id, occurred_at desc, id desc);

create index if not exists idx_memory_post_tagged_members_member_post
    on memory_post_tagged_members(member_id, post_id);

create index if not exists idx_media_assets_album_captured
    on media_assets(album_id, captured_at desc, id desc);

create index if not exists idx_media_assets_post
    on media_assets(post_id, id);

create index if not exists idx_chat_messages_room_cursor
    on chat_messages(room_id, created_at desc, id desc);

create index if not exists idx_chat_messages_sender_created
    on chat_messages(sender_id, created_at desc, id desc);

create index if not exists idx_notifications_recipient_created
    on in_app_notifications(recipient_id, created_at desc, id desc);

create index if not exists idx_notifications_unread
    on in_app_notifications(recipient_id, created_at desc, id desc)
    where read_at is null;

create index if not exists idx_notifications_scheduled_unread
    on in_app_notifications(scheduled_for)
    where read_at is null;

alter table media_assets
    add column if not exists provider varchar(50),
    add column if not exists width integer,
    add column if not exists height integer,
    add column if not exists duration_seconds integer,
    add column if not exists file_size_bytes bigint,
    add column if not exists checksum varchar(128),
    add column if not exists metadata_json jsonb;

create table if not exists media_links (
    id uuid primary key,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null,
    media_asset_id uuid not null references media_assets(id) on delete cascade,
    owner_type varchar(50) not null,
    owner_id uuid not null,
    sort_order integer not null default 0,
    caption varchar(2000)
);

create index if not exists idx_media_links_owner
    on media_links(owner_type, owner_id, sort_order, id);

create index if not exists idx_media_links_asset
    on media_links(media_asset_id);

create table if not exists chat_message_reads (
    message_id uuid not null references chat_messages(id) on delete cascade,
    user_id uuid not null references app_users(id) on delete cascade,
    read_at timestamp with time zone not null,
    primary key (message_id, user_id)
);

create index if not exists idx_chat_message_reads_user_read
    on chat_message_reads(user_id, read_at desc);

create table if not exists family_member_closure (
    ancestor_id uuid not null references family_members(id) on delete cascade,
    descendant_id uuid not null references family_members(id) on delete cascade,
    depth integer not null,
    primary key (ancestor_id, descendant_id)
);

create index if not exists idx_family_member_closure_descendant
    on family_member_closure(descendant_id, ancestor_id, depth);
