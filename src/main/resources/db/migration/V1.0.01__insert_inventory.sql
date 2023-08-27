DO $$
    DECLARE
        service text := 'inventory_service_v1';
BEGIN
    BEGIN
        INSERT INTO inventory (created_by, created_date, last_modified_by, last_modified_date, quantity, sku_code)
        VALUES (service, CURRENT_TIMESTAMP(6), service, CURRENT_TIMESTAMP(6), 100, 'iphone_13');

        INSERT INTO inventory (created_by, created_date, last_modified_by, last_modified_date, quantity, sku_code)
        VALUES (service, CURRENT_TIMESTAMP(6), service, CURRENT_TIMESTAMP(6), 0, 'iphone_14');

    EXCEPTION
        WHEN others THEN
            ROLLBACK;
            RAISE;
    END;
END $$;
