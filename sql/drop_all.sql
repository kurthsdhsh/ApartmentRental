-- Run this ONLY when you want to wipe everything and re-run schema.sql.
-- Order matters because of foreign keys (children first).
DROP TABLE MAINTENANCE_REQUESTS;
DROP TABLE PAYMENTS;
DROP TABLE CONTRACTS;
DROP TABLE TENANTS;
DROP TABLE UNITS;
DROP TABLE USERS;
