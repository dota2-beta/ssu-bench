--liquibase formatted sql

--changeset dota2-beta:1
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    points BIGINT DEFAULT 0,
    role VARCHAR(50) NOT NULL,
    is_blocked BOOLEAN DEFAULT FALSE
);

--changeset dota2-beta:2
CREATE TABLE tasks (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    executor_id BIGINT,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    cost BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL,
    CONSTRAINT fk_tasks_customer FOREIGN KEY (customer_id) REFERENCES users(id),
    CONSTRAINT fk_tasks_executor FOREIGN KEY (executor_id) REFERENCES users(id)
);

--changeset dota2-beta:3
CREATE TABLE bids (
    id BIGSERIAL PRIMARY KEY,
    executor_id BIGINT NOT NULL,
    task_id BIGINT NOT NULL,
    CONSTRAINT fk_bids_executor FOREIGN KEY (executor_id) REFERENCES users(id),
    CONSTRAINT fk_bids_task FOREIGN KEY (task_id) REFERENCES tasks(id)
);

--changeset dota2-beta:4
CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    payer_id BIGINT NOT NULL,
    receiver_id BIGINT NOT NULL,
    task_id BIGINT NOT NULL,
    points BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_payments_payer FOREIGN KEY (payer_id) REFERENCES users(id),
    CONSTRAINT fk_payments_receiver FOREIGN KEY (receiver_id) REFERENCES users(id),
    CONSTRAINT fk_payments_task FOREIGN KEY (task_id) REFERENCES tasks(id)
);