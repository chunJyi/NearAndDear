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

create table user_location (
  user_id uuid primary key references users(id) on delete cascade,
  latitude double precision not null,
  longitude double precision not null,
  updated_at timestamptz default now()
);

create table friends (
  id uuid primary key default gen_random_uuid(),
  user_id uuid references users(id) on delete cascade,
  friend_id uuid references users(id) on delete cascade,
  status text not null, -- PENDING | FRIEND | BLOCKED
  created_at timestamptz default now(),
  unique (user_id, friend_id)
);

create table subscriptions (
  id uuid primary key default gen_random_uuid(),
  user_id uuid references users(id) on delete cascade,
  plan text not null, -- FREE | PREMIUM
  start_date timestamptz,
  end_date timestamptz,
  is_active boolean default true
);
