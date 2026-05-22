create table app_users (
    id uuid primary key,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null,
    name varchar(255) not null,
    email varchar(255) not null unique,
    avatar_url varchar(255),
    birthday date,
    role varchar(50) not null
);

create table family_members (
    id uuid primary key,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null,
    user_id uuid references app_users(id),
    full_name varchar(255) not null,
    birth_date date,
    death_date date,
    role_in_family varchar(255),
    branch varchar(50) not null,
    avatar_url varchar(255),
    biography varchar(10000),
    generation_level integer not null
);

create table family_relationships (
    id uuid primary key,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null,
    source_member_id uuid not null references family_members(id),
    target_member_id uuid not null references family_members(id),
    type varchar(50) not null,
    notes varchar(2000)
);

create table albums (
    id uuid primary key,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null,
    title varchar(255) not null,
    description varchar(3000),
    category varchar(50) not null,
    created_by_id uuid references app_users(id)
);

create table memory_posts (
    id uuid primary key,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null,
    author_id uuid references app_users(id),
    text varchar(8000) not null,
    occurred_at timestamp with time zone not null,
    location_name varchar(255),
    event_type varchar(50) not null
);

create table memory_post_tagged_members (
    post_id uuid not null references memory_posts(id) on delete cascade,
    member_id uuid not null references family_members(id) on delete cascade,
    primary key (post_id, member_id)
);

create table media_assets (
    id uuid primary key,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null,
    url varchar(2000) not null,
    storage_public_id varchar(255),
    media_type varchar(50) not null,
    caption varchar(2000),
    captured_at timestamp with time zone,
    uploaded_by_id uuid references app_users(id),
    album_id uuid references albums(id),
    post_id uuid references memory_posts(id),
    linked_member_id uuid references family_members(id)
);

create table chat_rooms (
    id uuid primary key,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null,
    name varchar(255) not null,
    type varchar(50) not null,
    branch varchar(50)
);

create table chat_room_members (
    room_id uuid not null references chat_rooms(id) on delete cascade,
    user_id uuid not null references app_users(id) on delete cascade,
    primary key (room_id, user_id)
);

create table chat_messages (
    id uuid primary key,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null,
    room_id uuid not null references chat_rooms(id),
    sender_id uuid references app_users(id),
    type varchar(50) not null,
    body varchar(4000) not null,
    media_url varchar(2000),
    delivered_at timestamp with time zone,
    seen_at timestamp with time zone
);

create table in_app_notifications (
    id uuid primary key,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null,
    recipient_id uuid references app_users(id),
    type varchar(50) not null,
    title varchar(255) not null,
    body varchar(3000) not null,
    scheduled_for timestamp with time zone,
    read_at timestamp with time zone
);

create table recipes (
    id uuid primary key,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null,
    title varchar(255) not null,
    description varchar(3000),
    ingredients varchar(10000) not null,
    instructions varchar(10000) not null,
    video_url varchar(2000),
    notes_from_elders varchar(5000),
    created_by_id uuid references app_users(id)
);

create table memorial_tributes (
    id uuid primary key,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null,
    member_id uuid not null references family_members(id),
    author_id uuid references app_users(id),
    title varchar(255) not null,
    story varchar(10000) not null
);

create index idx_app_users_email on app_users(email);
create index idx_app_users_role on app_users(role);
create index idx_family_members_full_name on family_members(full_name);
create index idx_family_members_branch on family_members(branch);
create index idx_family_members_birth_date on family_members(birth_date);
create index idx_family_members_user_id on family_members(user_id);
create index idx_family_relationship_source on family_relationships(source_member_id);
create index idx_family_relationship_target on family_relationships(target_member_id);
create index idx_family_relationship_type on family_relationships(type);
create index idx_albums_category on albums(category);
create index idx_albums_title on albums(title);
create index idx_albums_created_by_id on albums(created_by_id);
create index idx_memory_posts_occurred_at on memory_posts(occurred_at);
create index idx_memory_posts_event_type on memory_posts(event_type);
create index idx_memory_posts_author_id on memory_posts(author_id);
create index idx_media_assets_media_type on media_assets(media_type);
create index idx_media_assets_captured_at on media_assets(captured_at);
create index idx_media_assets_album_id on media_assets(album_id);
create index idx_media_assets_linked_member_id on media_assets(linked_member_id);
create index idx_chat_rooms_type on chat_rooms(type);
create index idx_chat_rooms_branch on chat_rooms(branch);
create index idx_chat_messages_room_created on chat_messages(room_id, created_at);
create index idx_chat_messages_sender on chat_messages(sender_id);
create index idx_notifications_recipient_read on in_app_notifications(recipient_id, read_at);
create index idx_notifications_scheduled on in_app_notifications(scheduled_for);
create index idx_recipes_title on recipes(title);
create index idx_recipes_created_by on recipes(created_by_id);
create index idx_memorial_tributes_member on memorial_tributes(member_id);
create index idx_memorial_tributes_author on memorial_tributes(author_id);
