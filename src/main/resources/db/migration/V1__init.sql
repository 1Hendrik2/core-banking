CREATE TABLE accounts (
                          id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                          customer_id  VARCHAR(64) NOT NULL,
                          country      VARCHAR(2)  NOT NULL,
                          created_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE balances (
                          id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                          account_id   UUID NOT NULL REFERENCES accounts(id),
                          currency     VARCHAR(3) NOT NULL,
                          amount       NUMERIC(19,4) NOT NULL DEFAULT 0,
                          updated_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
                          UNIQUE (account_id, currency),
                          CHECK (amount >= 0)
);

CREATE TABLE transactions (
                              id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                              account_id    UUID NOT NULL REFERENCES accounts(id),
                              amount        NUMERIC(19,4) NOT NULL CHECK (amount > 0),
                              currency      VARCHAR(3) NOT NULL,
                              direction     VARCHAR(3) NOT NULL CHECK (direction IN ('IN','OUT')),
                              description   VARCHAR(255) NOT NULL,
                              balance_after NUMERIC(19,4) NOT NULL,
                              created_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_transactions_account ON transactions(account_id);