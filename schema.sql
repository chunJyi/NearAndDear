create table users (
  id uuid primary key references auth.users(id) on delete cascade,
  name text not null,
  email text unique not null,
  avatar_url text,
  phone text,
  role text default 'USER', -- USER | ADMIN
  created_at timestamptz default now(),
  updated_at timestamptz default now()
);

-- Function to handle updated_at
create or replace function handle_updated_at()
returns trigger as $$
begin
  new.updated_at = now();
  return new;
end;
$$ language plpgsql;

-- Trigger for users
create trigger set_updated_at
before update on users
for each row
execute function handle_updated_at();

create table user_location (
  user_id uuid primary key references users(id) on delete cascade,
  latitude double precision not null,
  longitude double precision not null,
  updated_at timestamptz default now()
);

-- Trigger for user_location
create trigger set_updated_at
before update on user_location
for each row
execute function handle_updated_at();

-- Realtime: push location updates to friends watching the map
alter publication supabase_realtime add table user_location;


create table friends (
  id uuid primary key default gen_random_uuid(),
  user_id uuid references users(id) on delete cascade,
  friend_id uuid references users(id) on delete cascade,
  status text not null, -- PENDING | FRIEND | BLOCKED
  -- user_id favorited friend_id (shown in user_id's friends / favorites list)
  is_favorite boolean default false,
  -- friend_id favorited user_id (shown in friend_id's friends / favorites list)
  friend_is_favorite boolean default false,
  created_at timestamptz default now(),
  unique (user_id, friend_id)
);

-- Existing DB migration:
-- alter table friends add column if not exists friend_is_favorite boolean default false;

create table subscriptions (
  id uuid primary key default gen_random_uuid(),
  user_id uuid references users(id) on delete cascade,
  plan text not null, -- FREE | PREMIUM
  start_date timestamptz,
  end_date timestamptz,
  is_active boolean default true
);

-- RLS (Row Level Security)
alter table users enable row level security;
alter table user_location enable row level security;
alter table friends enable row level security;
alter table subscriptions enable row level security;

-- Policies for users
create policy "Public profiles are viewable by everyone" on users
  for select using (true);

create policy "Users can update own profile" on users
  for update using (auth.uid() = id);

-- Policies for user_location
create policy "Users can update own location" on user_location
  for all using (auth.uid() = user_id);

create policy "Friends can see each other's location" on user_location
  for select using (
    auth.uid() = user_id or
    exists (
      select 1 from friends
      where status = 'FRIEND' and (
        (user_id = auth.uid() and friend_id = user_location.user_id) or
        (friend_id = auth.uid() and user_id = user_location.user_id)
      )
    )
  );

-- Policies for friends
create policy "Users can see their own relationships" on friends
  for select using (auth.uid() = user_id or auth.uid() = friend_id);

create policy "Users can insert friend requests" on friends
  for insert with check (auth.uid() = user_id);

create policy "Users can update their own relationships" on friends
  for update using (auth.uid() = user_id or auth.uid() = friend_id);

create policy "Users can delete their own relationships" on friends
  for delete using (auth.uid() = user_id or auth.uid() = friend_id);

