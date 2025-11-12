--liquibase formatted sql
--changeset author:20250220-create-orders-table
--comment: Create orders table
CREATE
    TABLE
        orders(
            id UUID PRIMARY KEY,
            customer_id UUID NOT NULL,
            amount NUMERIC(
                19,
                4
            ) NOT NULL,
            status VARCHAR(50) NOT NULL,
            created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
            updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        );

--changeset author:20250220-create-order-lines-table
--comment: Create order_lines table
CREATE
    TABLE
        order_lines(
            id UUID PRIMARY KEY,
            order_id UUID NOT NULL,
            product_id UUID NOT NULL,
            quantity INTEGER NOT NULL CHECK(
                quantity >= 1
            ),
            price NUMERIC(
                19,
                4
            ) NOT NULL,
            priority VARCHAR(10) NOT NULL CHECK(
                priority IN(
                    'HIGH',
                    'NORMAL',
                    'LOW'
                )
            ),
            created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
            FOREIGN KEY(order_id) REFERENCES orders(id) ON
            DELETE
                CASCADE
        );

--changeset author:20250220-create-orders-indexes
--comment: Create indexes for orders table
CREATE
    INDEX idx_orders_status ON
    orders(status);

CREATE
    INDEX idx_orders_created_at ON
    orders(created_at);

CREATE
    INDEX idx_orders_customer_id ON
    orders(customer_id);

CREATE
    INDEX idx_order_lines_order_id ON
    order_lines(order_id);

CREATE
    INDEX idx_order_lines_product_id ON
    order_lines(product_id);

--rollback-start
--rollback DROP TABLE order_lines;
--rollback DROP TABLE orders;
--rollback-end