-- Run AFTER schema.sql. Do not include ID columns (identity).
INSERT INTO UNITS (unit_no, unit_type, floor_no, monthly_rent, status) VALUES ('101','Studio',1,6500.00,'Available');
INSERT INTO UNITS (unit_no, unit_type, floor_no, monthly_rent, status) VALUES ('102','1-Bedroom',1,9000.00,'Available');
INSERT INTO UNITS (unit_no, unit_type, floor_no, monthly_rent, status) VALUES ('201','2-Bedroom',2,12500.00,'Available');
INSERT INTO UNITS (unit_no, unit_type, floor_no, monthly_rent, status) VALUES ('202','2-Bedroom',2,13000.00,'Maintenance');
INSERT INTO UNITS (unit_no, unit_type, floor_no, monthly_rent, status) VALUES ('301','3-Bedroom',3,18000.00,'Available');

INSERT INTO TENANTS (first_name, last_name, contact_no, email, address)
    VALUES ('Juan','Dela Cruz','09171234567','juan@email.com','Taguig City');
INSERT INTO TENANTS (first_name, last_name, contact_no, email, address)
    VALUES ('Maria','Santos','09181234567','maria@email.com','Pasig City');
INSERT INTO TENANTS (first_name, last_name, contact_no, email, address)
    VALUES ('Pedro','Reyes','09191234567',NULL,'Makati City');

-- Users: NOT inserted here. The app creates admin / admin123 on first run (DatabaseInitializer).
-- Contracts and payments: create them through the app so unit status logic is exercised.
