create table if not exists easyshop.users(id bigserial primary key,email text unique,password_hash text not null,role text not null default 'USER',created_at timestamptz not null default now());
