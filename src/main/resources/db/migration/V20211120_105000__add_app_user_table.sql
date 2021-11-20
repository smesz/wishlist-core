create table if not exists app_user
(
    id            uuid    not null
        constraint app_user_pk primary key
        default uuid_generate_v4(),
    first_name    varchar not null,
    last_name     varchar not null,
    email         varchar not null,
    password      varchar not null,
    birthday      timestamp,
    registered_at timestamp,
    updated_at    timestamp,
    roles         varchar,
    user_hash     varchar
);