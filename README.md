# Apartment Rental Management System (Java Swing + JDBC + Apache Derby)

## 1. One-time setup (every member)
1. Install the same JDK (e.g. 17). Check: `java -version`.
2. Download Apache Derby (binary). Java 8 -> 10.14.x, Java 11 -> 10.15.x, Java 17 -> 10.16.x. Extract to `C:\Derby`.
3. NetBeans: **Tools > Options > Java > Java DB**. Set *Java DB Installation* = `C:\Derby`, set a *Database Location* folder.
4. **Services tab > Databases > Java DB > right-click > Start Server** (do this EVERY time before running the app).
5. Right-click Java DB > **Create Database**: name `ApartmentDB`, user `app`, password `app`.
6. Right-click `jdbc:derby://localhost:1527/ApartmentDB` > **Connect**.
7. Right-click the connection > **Execute Command**, paste `sql/schema.sql`, click **Run SQL** (Ctrl+Shift+E).
   Then do the same with `sql/sample_data.sql`.
8. Add `derbyclient.jar` (and `derbyshared.jar` for Derby 10.15+) to the project: Project > Properties > Libraries > Add JAR/Folder.
   Copy the jars into `lib/` so teammates do not hunt for them.

## 2. Create the NetBeans project
1. New Project > Java with Ant > Java Application, name `ApartmentRental`, **untick "Create Main class"**.
2. Copy the contents of this repo's `src/` into the project's `src/` (packages `model dao database util ui` + `Main.java`).
3. Run `database/ConnectionTest.java` (Run File). Expected output: `Connected to jdbc:derby://localhost:1527/ApartmentDB` and 0-row counts.
4. Run `Main.java`. Use the stub login buttons (Admin or Staff) to enter the stub dashboard.

## 3. Re-creating the database
Run `sql/drop_all.sql`, then `sql/schema.sql`, then `sql/sample_data.sql`. Only Member 1 edits `sql/`.

## 4. Default login
`admin / admin123` is created automatically on first run by `DatabaseInitializer` (once Member 2's real login exists).

## 5. Troubleshooting
| Symptom | Fix |
|---|---|
| Connection refused / SQLState 08001 | Start the Derby server (step 4 above) |
| ClassNotFoundException ClientDriver / No suitable driver | Add `derbyclient.jar` to project libraries |
| Table 'USERS' does not exist | Run `schema.sql` while connected as `app/app` |
| XSDB6 another instance has booted | Use the network URL only; stop server, close NetBeans connections, restart |
| ERROR 42X01 syntax error | Reserved word or missing comma; keep column names as in schema.sql |
