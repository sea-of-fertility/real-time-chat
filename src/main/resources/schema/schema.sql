create table if not exists users (
    id bigint generated always AS identity primary key,
    email varchar(100) not null unique,
    password varchar(100) not null,
    nickname varchar(100) not null,
    role varchar(100) not null default 'USER',
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp,
    deleted_at timestamp
);

create table if not exists friendship (
    id bigint generated always AS identity primary key,
    friendship_A varchar(100) not null,
    friendship_B varchar(100) not null,
    created_at timestamp not null default current_timestamp
);

create table if not exists friend_invitation(
    id bigint generated always AS identity primary key,
    from_email varchar(100) not null,
    to_email varchar(100) not null,
    status varchar(20) not null,
    created_at timestamp not null default current_timestamp
);