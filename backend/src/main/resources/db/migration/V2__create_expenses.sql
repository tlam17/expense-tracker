CREATE SEQUENCE expenses_id_seq;

CREATE TABLE expenses (
    id BIGINT PRIMARY KEY DEFAULT nextval('expenses_id_seq'),
    amount NUMERIC(19, 2) NOT NULL,
    date DATE NOT NULL,
    description VARCHAR(255),
    category_id BIGINT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ,
    CONSTRAINT fk_category FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE RESTRICT
);

ALTER SEQUENCE expenses_id_seq OWNED BY expenses.id;
CREATE INDEX idx_expenses_category_id ON expenses(category_id);