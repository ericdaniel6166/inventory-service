BEGIN;

CREATE TABLE IF NOT EXISTS product
(
    id                 BIGSERIAL PRIMARY KEY,
    name               VARCHAR(255)   NOT NULL,
    description        TEXT,
    price              NUMERIC(19, 4) NOT NULL,
    created_by         VARCHAR(255),
    created_date       TIMESTAMP(6),
    last_modified_by   VARCHAR(255),
    last_modified_date TIMESTAMP(6)
);

CREATE TABLE IF NOT EXISTS category
(
    id                 BIGSERIAL PRIMARY KEY,
    name               VARCHAR(255) NOT NULL,
    created_by         VARCHAR(255),
    created_date       TIMESTAMP(6),
    last_modified_by   VARCHAR(255),
    last_modified_date TIMESTAMP(6)
);

CREATE TABLE IF NOT EXISTS product_category
(
    id                 BIGSERIAL PRIMARY KEY,
    product_id         BIGINT REFERENCES product (id),
    category_id        BIGINT REFERENCES category (id),
    created_by         VARCHAR(255),
    created_date       TIMESTAMP(6),
    last_modified_by   VARCHAR(255),
    last_modified_date TIMESTAMP(6)
);

CREATE TABLE IF NOT EXISTS inventory
(
    id                 BIGSERIAL
        PRIMARY KEY,
    product_id         BIGINT,
    quantity           INT,
    created_by         VARCHAR(255),
    created_date       TIMESTAMP(6),
    last_modified_by   VARCHAR(255),
    last_modified_date TIMESTAMP(6)
);

COMMIT;
