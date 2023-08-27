create table if not exists inventory
(
    id                 bigserial
        primary key,
    created_by         varchar(255),
    created_date       timestamp(6),
    last_modified_by   varchar(255),
    last_modified_date timestamp(6),
    quantity           integer,
    sku_code           varchar(255)
);