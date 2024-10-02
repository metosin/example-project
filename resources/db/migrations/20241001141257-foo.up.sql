CREATE TABLE todo (
  id serial,
  text text,
  created_at timestamp with time zone default now(),
  status text default 'unresolved'
);
