CREATE SEQUENCE budgets_id_seq;

CREATE TABLE budgets(
    id BIGINT PRIMARY KEY DEFAULT nextval('budgets_id_seq'),
    amount NUMERIC(19, 2) NOT NULL,
    month VARCHAR(7) NOT NULL,
    category_id BIGINT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ,
    CONSTRAINT fk_category FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE RESTRICT
);

ALTER SEQUENCE budgets_id_seq OWNED BY budgets.id;
ALTER TABLE budgets ADD CONSTRAINT uk_budgets_month_category UNIQUE (category_id, month);