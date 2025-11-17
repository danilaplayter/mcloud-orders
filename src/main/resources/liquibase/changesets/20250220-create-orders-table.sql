--liquibase formatted sql
--changeset author:20250220-create-orders-table
--comment: Create orders table
CREATE
    TABLE
        orders(
            order_id UUID PRIMARY KEY,
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
            line_id UUID PRIMARY KEY,
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