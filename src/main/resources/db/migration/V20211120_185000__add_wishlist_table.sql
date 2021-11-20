create table if not exists wishlist
(
    id    uuid    not null
        constraint wishlist_pk primary key
        default uuid_generate_v4(),
    owner uuid    not null,
    name  varchar not null
);