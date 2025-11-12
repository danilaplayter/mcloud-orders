--liquibase formatted sql
--changeset author:20250220-create-orders-table
--comment: Create orders table
CREATE
    TABLE
        orders(
            order_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
            customer_id UUID NOT NULL,
            region VARCHAR(50) NOT NULL,
            priority VARCHAR(10) NOT NULL CHECK(
                priority IN(
                    'HIGH',
                    'NORMAL',
                    'LOW'
                )
            ),
            amount DECIMAL(
                10,
                2
            ) NOT NULL CHECK(
                amount >= 0.01
            ),
            status VARCHAR(20) NOT NULL DEFAULT 'QUEUED',
            dispatched_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
            created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
            updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
        );

--changeset author:20250220-create-order-lines-table
--comment: Create order_lines table
CREATE
    TABLE
        order_lines(
            line_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
            order_id UUID NOT NULL REFERENCES orders(order_id) ON
            DELETE
                CASCADE,
                product_id UUID NOT NULL,
                quantity INTEGER NOT NULL CHECK(
                    quantity >= 1
                ),
                price DECIMAL(
                    10,
                    2
                ) NOT NULL CHECK(
                    price >= 0
                ),
                created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
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