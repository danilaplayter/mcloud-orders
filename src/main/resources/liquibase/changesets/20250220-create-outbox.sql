--liquibase formatted sql
--changeset create-outbox-table:1
--comment Создание таблицы outbox_event для паттерна Transactional Outbox

CREATE TABLE IF NOT EXISTS outbox_event (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    aggregate_id UUID NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    payload TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'NEW',
    attempts INTEGER NOT NULL DEFAULT 0,
    next_retry_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_error TEXT,
    UNIQUE (aggregate_id, event_type, created_at)
);

--changeset create-outbox-indexes:2
--comment Создание индексов для оптимизации выборки событий

CREATE INDEX IF NOT EXISTS idx_outbox_status ON outbox_event(status);
CREATE INDEX IF NOT EXISTS idx_outbox_next_retry ON outbox_event(next_retry_at) WHERE status = 'FAILED';
CREATE INDEX IF NOT EXISTS idx_outbox_created_at ON outbox_event(created_at) WHERE status = 'SENT';

--rollback DROP TABLE IF EXISTS outbox_event;