-- =====================================================================
-- Apartment Rental Management System  |  Apache Derby  |  schema v1
-- Run on a FRESH database (ApartmentDB, user app / password app).
-- If tables already exist, run sql/drop_all.sql first.
-- Derby identity columns auto-generate IDs: never put the ID in an INSERT.
-- =====================================================================

CREATE TABLE USERS (
    user_id   INTEGER      NOT NULL GENERATED ALWAYS AS IDENTITY,
    username  VARCHAR(50)  NOT NULL,
    password  VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    role      VARCHAR(30)  DEFAULT 'Staff'  NOT NULL,
    status    VARCHAR(20)  DEFAULT 'Active' NOT NULL,
    CONSTRAINT pk_users PRIMARY KEY (user_id),
    CONSTRAINT uq_users_username UNIQUE (username),
    CONSTRAINT ck_users_role CHECK (role IN ('Admin','Staff')),
    CONSTRAINT ck_users_status CHECK (status IN ('Active','Inactive'))
);

CREATE TABLE UNITS (
    unit_id      INTEGER       NOT NULL GENERATED ALWAYS AS IDENTITY,
    unit_no      VARCHAR(20)   NOT NULL,
    unit_type    VARCHAR(30)   NOT NULL,
    floor_no     INTEGER       DEFAULT 1 NOT NULL,
    monthly_rent DECIMAL(10,2) NOT NULL,
    status       VARCHAR(20)   DEFAULT 'Available' NOT NULL,
    CONSTRAINT pk_units PRIMARY KEY (unit_id),
    CONSTRAINT uq_units_no UNIQUE (unit_no),
    CONSTRAINT ck_units_rent CHECK (monthly_rent > 0),
    CONSTRAINT ck_units_status CHECK (status IN ('Available','Occupied','Maintenance'))
);

CREATE TABLE TENANTS (
    tenant_id       INTEGER      NOT NULL GENERATED ALWAYS AS IDENTITY,
    first_name      VARCHAR(50)  NOT NULL,
    last_name       VARCHAR(50)  NOT NULL,
    contact_no      VARCHAR(30)  NOT NULL,
    email           VARCHAR(100),
    address         VARCHAR(255),
    date_registered DATE         DEFAULT CURRENT_DATE NOT NULL,
    CONSTRAINT pk_tenants PRIMARY KEY (tenant_id)
);

CREATE TABLE CONTRACTS (
    contract_id  INTEGER       NOT NULL GENERATED ALWAYS AS IDENTITY,
    tenant_id    INTEGER       NOT NULL,
    unit_id      INTEGER       NOT NULL,
    start_date   DATE          NOT NULL,
    end_date     DATE          NOT NULL,
    monthly_rent DECIMAL(10,2) NOT NULL,
    deposit      DECIMAL(10,2) DEFAULT 0 NOT NULL,
    status       VARCHAR(20)   DEFAULT 'Active' NOT NULL,
    CONSTRAINT pk_contracts PRIMARY KEY (contract_id),
    CONSTRAINT fk_contracts_tenant FOREIGN KEY (tenant_id) REFERENCES TENANTS(tenant_id),
    CONSTRAINT fk_contracts_unit   FOREIGN KEY (unit_id)   REFERENCES UNITS(unit_id),
    CONSTRAINT ck_contracts_dates  CHECK (end_date > start_date),
    CONSTRAINT ck_contracts_rent   CHECK (monthly_rent > 0),
    CONSTRAINT ck_contracts_status CHECK (status IN ('Active','Ended','Terminated'))
);

CREATE TABLE PAYMENTS (
    payment_id     INTEGER       NOT NULL GENERATED ALWAYS AS IDENTITY,
    contract_id    INTEGER       NOT NULL,
    user_id        INTEGER       NOT NULL,
    payment_date   DATE          NOT NULL,
    billing_month  VARCHAR(7)    NOT NULL,   -- format yyyy-MM, example 2026-10
    amount         DECIMAL(10,2) NOT NULL,
    payment_method VARCHAR(20)   DEFAULT 'Cash' NOT NULL,
    status         VARCHAR(20)   DEFAULT 'Paid' NOT NULL,
    remarks        VARCHAR(255),
    CONSTRAINT pk_payments PRIMARY KEY (payment_id),
    CONSTRAINT fk_payments_contract FOREIGN KEY (contract_id) REFERENCES CONTRACTS(contract_id),
    CONSTRAINT fk_payments_user     FOREIGN KEY (user_id)     REFERENCES USERS(user_id),
    CONSTRAINT ck_payments_amount   CHECK (amount > 0),
    CONSTRAINT ck_payments_method   CHECK (payment_method IN ('Cash','GCash','Bank Transfer')),
    CONSTRAINT ck_payments_status   CHECK (status IN ('Paid','Partial','Voided'))
);

CREATE TABLE MAINTENANCE_REQUESTS (
    request_id    INTEGER       NOT NULL GENERATED ALWAYS AS IDENTITY,
    unit_id       INTEGER       NOT NULL,
    tenant_id     INTEGER,                    -- nullable: management may report it
    description   VARCHAR(500)  NOT NULL,
    priority      VARCHAR(20)   DEFAULT 'Medium' NOT NULL,
    date_reported DATE          DEFAULT CURRENT_DATE NOT NULL,
    status        VARCHAR(20)   DEFAULT 'Open' NOT NULL,
    date_resolved DATE,
    repair_cost   DECIMAL(10,2) DEFAULT 0 NOT NULL,
    CONSTRAINT pk_maint PRIMARY KEY (request_id),
    CONSTRAINT fk_maint_unit   FOREIGN KEY (unit_id)   REFERENCES UNITS(unit_id),
    CONSTRAINT fk_maint_tenant FOREIGN KEY (tenant_id) REFERENCES TENANTS(tenant_id),
    CONSTRAINT ck_maint_priority CHECK (priority IN ('Low','Medium','High','Urgent')),
    CONSTRAINT ck_maint_status   CHECK (status IN ('Open','In Progress','Resolved','Cancelled'))
);
