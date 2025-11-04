CREATE TABLE catogory
(
    id             BIGINT AUTO_INCREMENT NOT NULL,
    created_at     datetime NULL,
    is_deleted     BIT(1)       NOT NULL,
    updated_at     datetime NULL,
    `description`  VARCHAR(255) NULL,
    category_name  VARCHAR(255) NOT NULL,
    subcategory_id BIGINT NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (id)
);

CREATE TABLE catogory_future_products
(
    catogory_id        BIGINT NOT NULL,
    future_products_id BIGINT NOT NULL
);

CREATE TABLE catogory_seq
(
    next_val BIGINT NULL
);


CREATE TABLE product
(
    id            BIGINT AUTO_INCREMENT NOT NULL,
    created_at    datetime NULL,
    is_deleted    BIT(1) NOT NULL,
    updated_at    datetime NULL,
    `description` VARCHAR(255) NULL,
    image         VARCHAR(255) NULL,
    price DOUBLE NULL,
    title         VARCHAR(255) NULL,
    category_id   BIGINT NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (id)
);

CREATE TABLE product_seq
(
    next_val BIGINT NULL
);

CREATE TABLE subcategory
(
    id         BIGINT AUTO_INCREMENT NOT NULL,
    created_at datetime NULL,
    is_deleted BIT(1) NOT NULL,
    updated_at datetime NULL,
    surname    VARCHAR(255) NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (id)
);

CREATE TABLE subcategory_seq
(
    next_val BIGINT NULL
);

ALTER TABLE catogory
    ADD CONSTRAINT UKcrejt7wm27rou9earmpt8vbn4 UNIQUE (category_name);

ALTER TABLE catogory_future_products
    ADD CONSTRAINT UKn0cj4voobns3eiggmpsrkt27y UNIQUE (future_products_id);

ALTER TABLE catogory_future_products
    ADD CONSTRAINT FKa40ew9q6b22ko6nws1gfosrnv FOREIGN KEY (future_products_id) REFERENCES product (id) ON DELETE NO ACTION;

ALTER TABLE catogory
    ADD CONSTRAINT FKbgh1otfjf62v6tvbteyi244va FOREIGN KEY (subcategory_id) REFERENCES subcategory (id) ON DELETE NO ACTION;

CREATE INDEX FKbgh1otfjf62v6tvbteyi244va ON catogory (subcategory_id);

ALTER TABLE product
    ADD CONSTRAINT FKjyk1alubes3jiqgdfmmus5lc1 FOREIGN KEY (category_id) REFERENCES catogory (id) ON DELETE NO ACTION;

CREATE INDEX FKjyk1alubes3jiqgdfmmus5lc1 ON product (category_id);

ALTER TABLE catogory_future_products
    ADD CONSTRAINT FKrxfb00aon9vrkhwb0wxrcabdi FOREIGN KEY (catogory_id) REFERENCES catogory (id) ON DELETE NO ACTION;

CREATE INDEX FKrxfb00aon9vrkhwb0wxrcabdi ON catogory_future_products (catogory_id);