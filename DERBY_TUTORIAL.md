# How to use the Derby database

## A. What Derby is
Apache Derby (Java DB) is a database written in Java. In this project it runs as a **server** on your PC
(port 1527). Your Java app connects to it with JDBC using `jdbc:derby://localhost:1527/apt_db` (user `app`, password `app`).
A Derby database is just a **folder** on your computer, so it is NOT shared through Git. Only the `.sql` scripts are shared.

## B. Daily routine (NetBeans)
1. NetBeans > **Services** tab > Databases > Java DB > right-click > **Start Server**.
2. Right-click `jdbc:derby://localhost:1527/apt_db` > **Connect**.
3. Run your app. When finished you may Stop Server.

## C. Look at your data
Services > Databases > apt_db connection > Schemas > **APP** > Tables > right-click a table > **View Data**.

## D. Run SQL yourself
Right-click the connection > **Execute Command**. Type a query and press Ctrl+Shift+E:

    SELECT * FROM UNITS;
    SELECT * FROM UNITS WHERE status = 'Available' ORDER BY unit_no;
    INSERT INTO TENANTS (first_name, last_name, contact_no) VALUES ('Ana','Lopez','09171112222');
    UPDATE UNITS SET monthly_rent = 7000 WHERE unit_no = '101';
    DELETE FROM TENANTS WHERE last_name = 'Lopez';
    SELECT * FROM UNITS FETCH FIRST 3 ROWS ONLY;   -- Derby has no LIMIT

Rules: do not put identity columns (unit_id, tenant_id, ...) in an INSERT. Text uses single quotes. Dates look like '2026-10-07'.

## E. Useful queries to show during the defense
    -- JOIN: units with their active tenant
    SELECT u.unit_no, u.status, t.first_name || ' ' || t.last_name AS tenant
    FROM UNITS u
    LEFT JOIN CONTRACTS c ON c.unit_id = u.unit_id AND c.status = 'Active'
    LEFT JOIN TENANTS t ON t.tenant_id = c.tenant_id;

    -- Count requests per status
    SELECT status, COUNT(*) FROM MAINTENANCE_REQUESTS GROUP BY status;

## F. Reset / start fresh
Run `sql/drop_all.sql`, then `sql/schema.sql`, then `sql/sample_data.sql`.

## G. From Java (what the DAOs do)
    String sql = "SELECT * FROM UNITS WHERE status = ?";
    try (Connection c = DatabaseConnection.getConnection();
         PreparedStatement ps = c.prepareStatement(sql)) {
        ps.setString(1, "Available");
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) System.out.println(rs.getString("unit_no"));
        }
    }
Always use PreparedStatement with `?` and try-with-resources.

## H. Command line alternative (ij tool)
    cd C:\Derby\bin
    startNetworkServer                      (starts the server)
    ij                                      (opens the SQL prompt)
    ij> connect 'jdbc:derby://localhost:1527/apt_db;user=app;password=app';
    ij> run 'C:\path\to\sql\schema.sql';
    ij> SELECT * FROM UNITS;
    ij> exit;
    stopNetworkServer

## I. Common errors
| SQLState | Meaning |
|---|---|
| 08001 | Server not running |
| 23505 | Duplicate value in a UNIQUE column |
| 23503 | Foreign key violation (deleting a parent that has children, or a bad ID) |
| 23513 | CHECK constraint failed (bad status, rent <= 0, ...) |
| 42X05 | Table does not exist (schema.sql not run, or wrong user) |
| 42X01 | Syntax error |
