/* Common tables */

-- ISO 4217 code
--CREATE TYPE
--Currency AS enum(
--    'EUR',
--    'GBP'
--);
CREATE TABLE IF NOT EXISTS
Currency(
    code varchar(5) NOT NULL PRIMARY KEY
);
INSERT INTO Currency VALUES
    ('EUR'),
    ('GBP')
;

-------

-- An account. Not bound to any currency, to potentially allow multi-currency support.
CREATE TABLE IF NOT EXISTS
Account(
    accountId uuid NOT NULL PRIMARY KEY,
    createdAt timestamp with time zone DEFAULT now() NOT NULL
);

-- Current account statement (not closed).
-- An account can have at most one (per currency) an open statement. That statement is the latest one.
-- After some time, an open account statement transitions to be closed with finalized closed balance,
-- and a new open statement is created instead.
CREATE TABLE IF NOT EXISTS
AccountOpenStatement(
    accountOpenStatementId uuid NOT NULL PRIMARY KEY,
    accountId uuid NOT NULL REFERENCES Account(accountId),
--    currency Currency NOT NULL,
    currency varchar(5) NOT NULL REFERENCES Currency(code),
    openedAt timestamp with time zone DEFAULT now() NOT NULL,
    openingBalance decimal(18, 4) NOT NULL
);

CREATE UNIQUE INDEX AccountOpenStatement_accountId_currency_UQ
    ON AccountOpenStatement(accountId, currency);

-- Closed account statement.
-- An account may have multiple statements that track account's balance history
CREATE TABLE IF NOT EXISTS
AccountStatement(
    accountStatementId uuid NOT NULL PRIMARY KEY,
    accountId uuid NOT NULL REFERENCES Account(accountId),
--    currency Currency NOT NULL,
    currency varchar(5) NOT NULL REFERENCES Currency(code),
    openedAt timestamp with time zone NOT NULL,
    openingBalance decimal(18, 4) NOT NULL,
    closedAt timestamp with time zone DEFAULT now() NOT NULL,
    closingBalance decimal(18, 4) NOT NULL
);

CREATE UNIQUE INDEX AccountStatement_accountId_currency_UQ
    ON AccountStatement(accountId, currency);

ALTER TABLE AccountStatement ADD
 CONSTRAINT AccountStatement_openedAt_is_before_closedAt_CK
      CHECK (openedAt < closedAt);


--CREATE TYPE
--AccountTransactionType AS enum(
--    'debit',
--    'credit'
--);
CREATE TABLE IF NOT EXISTS
AccountTransactionType(
    code varchar(10) NOT NULL PRIMARY KEY
);
INSERT INTO AccountTransactionType VALUES
    ('debit'),
    ('credit')
;

CREATE TABLE IF NOT EXISTS
AccountTransaction(
    accountTransactionId uuid NOT NULL PRIMARY KEY,
    accountId uuid NOT NULL REFERENCES Account(accountId),
--    currency Currency NOT NULL,
    currency varchar(5) NOT NULL REFERENCES Currency(code),
    amount decimal(18, 4) NOT NULL,
    createdAt timestamp with time zone DEFAULT now() NOT NULL,
--    transactionType AccountTransactionType NOT NULL,
    transactionType varchar(10) NOT NULL REFERENCES AccountTransactionType(code),
    description varchar(100)
);

ALTER TABLE AccountTransaction ADD
 CONSTRAINT AccountTransaction_amount_is_positive_CK
      CHECK (amount > 0.0);

CREATE INDEX AccountTransaction_createdAt_IDX
    ON AccountTransaction (createdAt);
