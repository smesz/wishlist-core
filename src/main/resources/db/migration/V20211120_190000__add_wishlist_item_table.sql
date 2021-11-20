create table if not exists wishlist_item
(
    id          uuid    not null
        constraint wishlist_item_pk primary key
        default uuid_generate_v4(),
    wishlist_id uuid    not null,
    name        varchar not null,
    description varchar
);