begin;

create table if not exists product
(
    id                 bigserial primary key,
    name               varchar(255)   not null,
    description        text,
    price              numeric(19, 4) not null,
    created_by         varchar(255),
    created_date       timestamp(6),
    last_modified_by   varchar(255),
    last_modified_date timestamp(6)
);

create table if not exists category
(
    id                 bigserial primary key,
    name               varchar(255) not null,
    created_by         varchar(255),
    created_date       timestamp(6),
    last_modified_by   varchar(255),
    last_modified_date timestamp(6)
);

create table if not exists product_category
(
    id                 bigserial primary key,
    product_id         bigint references product (id),
    category_id        bigint references category (id),
    created_by         varchar(255),
    created_date       timestamp(6),
    last_modified_by   varchar(255),
    last_modified_date timestamp(6)
);

create table if not exists inventory
(
    id                 bigserial
        primary key,
    product_id         bigint references product (id),
    quantity           int,
    created_by         varchar(255),
    created_date       timestamp(6),
    last_modified_by   varchar(255),
    last_modified_date timestamp(6)
);

commit;
