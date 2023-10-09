begin;

insert into category (name, created_by, created_date, last_modified_by, last_modified_date)
values ('Electronics', 'inventory-service-v1.0', now() - interval '8 year' + random() * interval '1 year',
        'inventory-service-v1.0', now() - interval '6 year' + random() * interval '1 year'),
       ('Clothing', 'inventory-service-v1.0', now() - interval '8 year' + random() * interval '1 year',
        'inventory-service-v1.0', now() - interval '6 year' + random() * interval '1 year'),
       ('Home & Garden', 'inventory-service-v1.0', now() - interval '8 year' + random() * interval '1 year',
        'inventory-service-v1.0', now() - interval '6 year' + random() * interval '1 year'),
       ('Toys & Games', 'inventory-service-v1.0', now() - interval '8 year' + random() * interval '1 year',
        'inventory-service-v1.0', now() - interval '6 year' + random() * interval '1 year'),
       ('Sports & Outdoors', 'inventory-service-v1.0', now() - interval '8 year' + random() * interval '1 year',
        'inventory-service-v1.0', now() - interval '6 year' + random() * interval '1 year'),
       ('Books', 'inventory-service-v1.0', now() - interval '8 year' + random() * interval '1 year',
        'inventory-service-v1.0', now() - interval '6 year' + random() * interval '1 year'),
       ('Furniture', 'inventory-service-v1.0', now() - interval '8 year' + random() * interval '1 year',
        'inventory-service-v1.0', now() - interval '6 year' + random() * interval '1 year'),
       ('Jewelry', 'inventory-service-v1.0', now() - interval '8 year' + random() * interval '1 year',
        'inventory-service-v1.0', now() - interval '6 year' + random() * interval '1 year'),
       ('Health & Beauty', 'inventory-service-v1.0', now() - interval '8 year' + random() * interval '1 year',
        'inventory-service-v1.0', now() - interval '6 year' + random() * interval '1 year'),
       ('Food', 'inventory-service-v1.0', now() - interval '8 year' + random() * interval '1 year',
        'inventory-service-v1.0', now() - interval '6 year' + random() * interval '1 year');


create temp table if not exists category_assignments as
select p.product_id,
       case
           when p.product_id % 10 = 1 then 1
           when p.product_id % 10 = 2 then 2
           when p.product_id % 10 = 3 then 3
           when p.product_id % 10 = 4 then 4
           when p.product_id % 10 = 5 then 5
           when p.product_id % 10 = 6 then 6
           when p.product_id % 10 = 7 then 7
           when p.product_id % 10 = 8 then 8
           when p.product_id % 10 = 9 then 9
           when p.product_id % 10 = 0 then 10
           end                                              as category_id,
       case
           when p.product_id % 10 = 1 then 'iPhone'
           when p.product_id % 10 = 2 then 'Designer Jeans'
           when p.product_id % 10 = 3 then 'LED TV'
           when p.product_id % 10 = 4 then 'Toy Train Set'
           when p.product_id % 10 = 5 then 'Football'
           when p.product_id % 10 = 6 then 'Mystery Novel'
           when p.product_id % 10 = 7 then 'Coffee Table'
           when p.product_id % 10 = 8 then 'Diamond Necklace'
           when p.product_id % 10 = 9 then 'Supplement'
           when p.product_id % 10 = 0 then 'Beef'
           end || ' ' || (floor(random() * 10000)::integer) as product_name
from generate_series(1, 150) as p(product_id);


insert into product (name, description, price, created_by, created_date, last_modified_by, last_modified_date)
select ca.product_name,
       'Description for ' || ca.product_name,
       (random() * 10000)::numeric(19, 4),
       'inventory-service-v1.0',
       now() - interval '4 year' + random() * interval '1 year',
       'inventory-service-v1.0',
       now() - interval '2 year' + random() * interval '1 year'
from category_assignments ca;


insert into product_category (product_id, category_id, created_by, created_date, last_modified_by, last_modified_date)
select ca.product_id,
       ca.category_id,
       'inventory-service-v1.0',
       now() - interval '8 year' + random() * interval '1 year',
       'inventory-service-v1.0',
       now() - interval '6 year' + random() * interval '1 year'
from category_assignments ca;


drop table if exists category_assignments;

create temp table if not exists product_ids as
select id as product_id
from product;


insert into inventory (product_id, quantity, created_by, created_date, last_modified_by, last_modified_date)
select pi.product_id,
       (floor(random() * 10000) + 1)::integer                   as quantity,
       'inventory-service-v1.0'                                   as created_by,
       now() - interval '4 year' + random() * interval '1 year' as created_date,
       'inventory-service-v1.0'                                   as last_modified_by,
       now() - interval '2 year' + random() * interval '1 year' as last_modified_date
from product_ids pi;

drop table if exists product_ids;

commit;
