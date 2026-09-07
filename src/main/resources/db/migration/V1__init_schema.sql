-- 1. Tabla synchronization
CREATE TABLE synchronization
(
    id_synchronization BIGSERIAL NOT NULL PRIMARY KEY,
    created_time       TIMESTAMP NULL
);

-- 2. Tabla recipient
CREATE TABLE recipient
(
    recipient_generated_id BIGSERIAL NOT NULL PRIMARY KEY,
    id                     VARCHAR(255) NULL,
    name                   VARCHAR(255) NULL,
    supports               VARCHAR(255) NULL,
    synchronization_id     BIGINT       NOT NULL,
    top                    BOOLEAN DEFAULT FALSE,
    status                 INTEGER      DEFAULT 0,
    CONSTRAINT recipient_fk FOREIGN KEY (synchronization_id)
        REFERENCES synchronization (id_synchronization)
);

-- 3. Tabla service
CREATE TABLE service
(
    service_generated_id BIGSERIAL NOT NULL PRIMARY KEY,
    id                   VARCHAR(255) NULL,
    name                 VARCHAR(255) NULL,
    type                 VARCHAR(255) NULL,
    label                VARCHAR(255) NULL,
    data_type            VARCHAR(255) NULL,
    length               INTEGER      NULL,
    recipient_id         BIGINT       NOT NULL,
    synchronization_id   BIGINT       NOT NULL,
    status               INTEGER      DEFAULT 0,
    CONSTRAINT service_synchronization_fk FOREIGN KEY (synchronization_id)
        REFERENCES synchronization (id_synchronization),
    CONSTRAINT service_recipient_fk FOREIGN KEY (recipient_id)
        REFERENCES recipient (recipient_generated_id)
);
