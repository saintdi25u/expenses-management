-- V1.1 : création des tables initiales (user, category, transaction)

-- ---------------------------------------------------------------------------
-- Séquences (stratégie GenerationType.SEQUENCE)
-- ---------------------------------------------------------------------------
CREATE SEQUENCE user_id_seq START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE category_id_seq START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE transaction_id_seq START WITH 1 INCREMENT BY 50;

-- ---------------------------------------------------------------------------
-- Table : user  ("user" est un mot réservé PostgreSQL, d'où les guillemets)
-- ---------------------------------------------------------------------------
CREATE TABLE "user" (
    id            INT8       NOT NULL DEFAULT nextval('user_id_seq'),
    first_name    TEXT NOT NULL,
    last_name     TEXT NOT NULL,
    email         TEXT NOT NULL,
    password_hash TEXT NOT NULL,
    created_at    TIMESTAMP    NOT NULL,
    updated_at    TIMESTAMP    NOT NULL,
    CONSTRAINT pk_user PRIMARY KEY (id),
    CONSTRAINT uq_user_email UNIQUE (email)
);

-- ---------------------------------------------------------------------------
-- Table : category
-- ---------------------------------------------------------------------------
CREATE TABLE category (
    id         INT8       NOT NULL DEFAULT nextval('category_id_seq'),
    name       TEXT NOT NULL,
    owner_id   INT8       NOT NULL,
    created_at TIMESTAMP    NOT NULL,
    updated_at TIMESTAMP    NOT NULL,
    CONSTRAINT pk_category PRIMARY KEY (id),
    CONSTRAINT fk_category_owner FOREIGN KEY (owner_id) REFERENCES "user" (id)
);

-- ---------------------------------------------------------------------------
-- Table : transaction
-- ---------------------------------------------------------------------------
CREATE TABLE transaction (
    id               INT8         NOT NULL DEFAULT nextval('transaction_id_seq'),
    date             DATE           NOT NULL,
    amount           NUMERIC(19, 2) NOT NULL,
    name             TEXT           NOT NULL,
    description      TEXT,
    category_id      INT8,
    user_id          INT8         NOT NULL,
    transaction_type TEXT           NOT NULL,
    created_at       TIMESTAMP      NOT NULL,
    updated_at       TIMESTAMP      NOT NULL,
    CONSTRAINT pk_transaction PRIMARY KEY (id),
    CONSTRAINT fk_transaction_user FOREIGN KEY (user_id) REFERENCES "user" (id),
    CONSTRAINT fk_transaction_category FOREIGN KEY (category_id) REFERENCES category (id),
    CONSTRAINT ck_transaction_type CHECK (transaction_type IN ('DEBIT', 'CREDIT'))
);
