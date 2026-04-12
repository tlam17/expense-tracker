CREATE SEQUENCE categories_id_seq;

CREATE TABLE categories (
    id BIGINT PRIMARY KEY DEFAULT nextval('categories_id_seq'),
    name VARCHAR(100) NOT NULL,
    default_budget NUMERIC(19, 2),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ
);

ALTER SEQUENCE categories_id_seq OWNED BY categories.id;