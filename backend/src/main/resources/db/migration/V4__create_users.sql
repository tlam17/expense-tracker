CREATE SEQUENCE users_id_seq;

CREATE TABLE users(
    id BIGINT DEFAULT nextval('users_id_seq') PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
);

ALTER SEQUENCE users_id_seq OWNED BY users.id;