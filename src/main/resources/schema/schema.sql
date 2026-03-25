create table if not exists users (
    id bigint generated always AS identity primary key,
    email varchar(100) not null unique,
    password varchar(100) not null,
    nickname varchar(100) not null,
    role varchar(100) not null defalult 'USER',
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp,
    deleted_at timestamp
);

