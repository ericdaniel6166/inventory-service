BEGIN;

INSERT INTO category (name, created_by, created_date, last_modified_by, last_modified_date)
VALUES ('Electronics', 'inventory-service-v1.0', now() - INTERVAL '8 year' + random() * INTERVAL '1 year',
        'inventory-service-v1.0', now() - INTERVAL '6 year' + random() * INTERVAL '1 year'),
       ('Clothing', 'inventory-service-v1.0', now() - INTERVAL '8 year' + random() * INTERVAL '1 year',
        'inventory-service-v1.0', now() - INTERVAL '6 year' + random() * INTERVAL '1 year'),
       ('Home & Garden', 'inventory-service-v1.0', now() - INTERVAL '8 year' + random() * INTERVAL '1 year',
        'inventory-service-v1.0', now() - INTERVAL '6 year' + random() * INTERVAL '1 year'),
       ('Toys & Games', 'inventory-service-v1.0', now() - INTERVAL '8 year' + random() * INTERVAL '1 year',
        'inventory-service-v1.0', now() - INTERVAL '6 year' + random() * INTERVAL '1 year'),
       ('Sports & Outdoors', 'inventory-service-v1.0', now() - INTERVAL '8 year' + random() * INTERVAL '1 year',
        'inventory-service-v1.0', now() - INTERVAL '6 year' + random() * INTERVAL '1 year'),
       ('Books', 'inventory-service-v1.0', now() - INTERVAL '8 year' + random() * INTERVAL '1 year',
        'inventory-service-v1.0', now() - INTERVAL '6 year' + random() * INTERVAL '1 year'),
       ('Furniture', 'inventory-service-v1.0', now() - INTERVAL '8 year' + random() * INTERVAL '1 year',
        'inventory-service-v1.0', now() - INTERVAL '6 year' + random() * INTERVAL '1 year'),
       ('Jewelry', 'inventory-service-v1.0', now() - INTERVAL '8 year' + random() * INTERVAL '1 year',
        'inventory-service-v1.0', now() - INTERVAL '6 year' + random() * INTERVAL '1 year'),
       ('Health & Beauty', 'inventory-service-v1.0', now() - INTERVAL '8 year' + random() * INTERVAL '1 year',
        'inventory-service-v1.0', now() - INTERVAL '6 year' + random() * INTERVAL '1 year'),
       ('Food', 'inventory-service-v1.0', now() - INTERVAL '8 year' + random() * INTERVAL '1 year',
        'inventory-service-v1.0', now() - INTERVAL '6 year' + random() * INTERVAL '1 year');


CREATE TEMP TABLE IF NOT EXISTS category_assignments AS
SELECT p.product_id,
       CASE
           WHEN p.product_id % 10 = 1 THEN 1
           WHEN p.product_id % 10 = 2 THEN 2
           WHEN p.product_id % 10 = 3 THEN 3
           WHEN p.product_id % 10 = 4 THEN 4
           WHEN p.product_id % 10 = 5 THEN 5
           WHEN p.product_id % 10 = 6 THEN 6
           WHEN p.product_id % 10 = 7 THEN 7
           WHEN p.product_id % 10 = 8 THEN 8
           WHEN p.product_id % 10 = 9 THEN 9
           WHEN p.product_id % 10 = 0 THEN 10
           END                                              AS category_id,
       CASE
           WHEN p.product_id % 10 = 1 THEN 'iPhone'
           WHEN p.product_id % 10 = 2 THEN 'Designer Jeans'
           WHEN p.product_id % 10 = 3 THEN 'LED TV'
           WHEN p.product_id % 10 = 4 THEN 'Toy Train Set'
           WHEN p.product_id % 10 = 5 THEN 'Football'
           WHEN p.product_id % 10 = 6 THEN 'Mystery Novel'
           WHEN p.product_id % 10 = 7 THEN 'Coffee Table'
           WHEN p.product_id % 10 = 8 THEN 'Diamond Necklace'
           WHEN p.product_id % 10 = 9 THEN 'Supplement'
           WHEN p.product_id % 10 = 0 THEN 'Beef'
           END || ' ' || (floor(random() * 10000)::INTEGER) AS product_name
FROM generate_series(1, 150) AS p(product_id);


INSERT INTO product (name, description, price, created_by, created_date, last_modified_by, last_modified_date)
SELECT ca.product_name,
       'Description for ' || ca.product_name,
       (random() * 10000)::NUMERIC(19, 4),
       'inventory-service-v1.0',
       now() - INTERVAL '4 year' + random() * INTERVAL '1 year',
       'inventory-service-v1.0',
       now() - INTERVAL '2 year' + random() * INTERVAL '1 year'
FROM category_assignments ca;


INSERT INTO product_category (product_id, category_id, created_by, created_date, last_modified_by, last_modified_date)
SELECT ca.product_id,
       ca.category_id,
       'inventory-service-v1.0',
       now() - INTERVAL '8 year' + random() * INTERVAL '1 year',
       'inventory-service-v1.0',
       now() - INTERVAL '6 year' + random() * INTERVAL '1 year'
FROM category_assignments ca;


DROP TABLE IF EXISTS category_assignments;

CREATE TEMP TABLE IF NOT EXISTS product_ids AS
SELECT id AS product_id
FROM product;


INSERT INTO inventory (product_id, quantity, created_by, created_date, last_modified_by, last_modified_date)
SELECT pi.product_id,
       (floor(random() * 10000) + 1)::INTEGER                   AS quantity,
       'inventory-service-v1.0'                                 AS created_by,
       now() - INTERVAL '4 year' + random() * INTERVAL '1 year' AS created_date,
       'inventory-service-v1.0'                                 AS last_modified_by,
       now() - INTERVAL '2 year' + random() * INTERVAL '1 year' AS last_modified_date
FROM product_ids pi;

DROP TABLE IF EXISTS product_ids;

COMMIT;
