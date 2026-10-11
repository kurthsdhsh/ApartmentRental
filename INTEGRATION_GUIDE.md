# Integration Guide (Phase 3 and Phase 4, Member 1)

This build contains a working version of **every** module, so the project already runs end to end.
When a teammate pushes their own version of a module, theirs replaces the matching files below (see section 2).

## 1. What is in the build

| Owner | Files in this build |
|---|---|
| Member 1 | `sql/*`, `database/*`, `util/*` (incl. new `FormUtil`), `model/ComboItem`, `Main`, Maintenance (`MaintenanceRequest`, `MaintenanceDAO`, `MaintenanceFrame`), `tests/*` |
| Member 2 | `User`, `UserDAO`, `DashboardDAO`, `ReportDAO`, `LoginFrame`, `DashboardFrame`, `UserFrame`, `ReportsFrame` |
| Member 3 | `Unit`, `Tenant`, `UnitDAO`, `TenantDAO`, `UnitFrame`, `TenantFrame` |
| Member 4 | `Contract`, `Payment`, `ContractDAO`, `PaymentDAO`, `ContractFrame`, `PaymentFrame` |

The non-Member-1 files are integration versions written to the guide's conventions. They are placeholders for the owners' own work.

## 2. Merge order (Section 9.2 of the team guide)

Run these from your project folder. Replace `m3-units-tenants` etc. with the real branch names.

```
git checkout main
git pull origin main

git merge origin/m3-units-tenants        # 1. Member 3: Units and Tenants
# run the tests (section 3), click every menu item
git merge origin/m4-contracts-payments   # 2. Member 4: Contracts and Payments
# run the tests again
# 3. Member 1: Maintenance is already on main
git merge origin/m2-login-users          # 4. Member 2: Login, Dashboard, Users (wired to the real frames)
# 5. Member 2: Reports (same branch or a later push)

git push origin main
```

Conflict rules:
- Two people must never edit the same file. Whoever is NOT the owner of a conflicting file takes the owner's version:
  `git checkout --theirs path/to/File.java` (owner's branch) and commit.
- For `.form` files the owner redoes any lost change. Never merge a `.form` by hand.
- If a teammate's class has the same name as one in this build, **theirs wins**. Check that their DAO methods still have the
  names the other modules call: `UnitDAO.getAvailableUnits()`, `TenantDAO.getAll()`, `ContractDAO.getActiveContracts()`,
  `PaymentDAO.totalForMonth(String)`, `DashboardDAO` counts, `ReportDAO` report methods. If one differs, ask the owner to add it.

## 3. After EVERY merge (5 minutes)

1. Start the Derby server. Make sure the database has `schema.sql` loaded.
2. Run `database/ConnectionTest.java` and expect `Connected to ...`.
3. Run `tests/CrossModuleTest.java` and expect `0 failed`. It automates cross-module test steps 1 to 14.
   Use a fresh database: run `drop_all.sql`, then `schema.sql` (the sample data is optional). The test leaves its own data behind, so reset again before re-running.
4. Run `tests/GuiSmokeTest.java`. It opens all screens as Admin and as Staff and checks that Delete is disabled for Staff and the Users menu is disabled.
5. Run `Main.java` and click every menu item once. Report breakage to the file's owner immediately.

## 4. Cross-module test status (Section 9.4)

| Step | Check | How |
|---|---|---|
| 1 | Log in as admin / admin123 | Automated |
| 2 | Add staff1 | Automated |
| 3 | Add unit 401 | Automated |
| 4 | Add tenant Ana Lopez | Automated |
| 5 | Contract makes 401 Occupied | Automated (also checks the failed transaction leaves no half-made contract) |
| 6 | Unit list no longer shows 401 | Automated |
| 7 | Payment received by the logged-in user | Automated |
| 8 | Dashboard counts change | Automated (DAO level). Also look at the dashboard after closing a module window |
| 9, 10 | Delete of unit 401 / tenant Ana blocked | Automated |
| 11 | Maintenance request shows unit and tenant by name | Automated |
| 12 | Occupancy, Monthly collection, Overdue (and 3 more) reports | Automated |
| 13 | End Contract frees the unit | Automated |
| 14 | staff1: Users menu and Delete disabled | Automated (`GuiSmokeTest`) |
| 15 | Stop Derby, click Save: friendly message, no crash | **Manual** |

Step 15 by hand: log in, open Units, right-click Java DB in NetBeans and choose Stop Server, then press Save.
Expected: the message "Cannot connect to the database. Is the Derby server running?" and the app keeps running.

## 4b. Checklist 9.3 (what was verified in this build)

| # | Check | Result |
|---|---|---|
| 1 | Same URL for everyone | `DatabaseConnection` uses `jdbc:derby://localhost:1527/apt_db` with `app`/`app`. The whole team must use the same name |
| 3 | Module frames use DISPOSE_ON_CLOSE | Yes. Only Login and Dashboard exit the app on close |
| 4 | Statuses come from Constants | Yes for units, contracts, payments, roles, maintenance. User status ("Active"/"Inactive") has no constant |
| 5, 6 | No-argument constructors, user from Session | Yes |
| 7 | Delete disabled for Staff | Yes, verified by `GuiSmokeTest` |
| 8 | No SQL in frames, no dialogs in DAOs | Yes (searched the code) |
| 9 | PreparedStatement for user input | Yes. `DashboardDAO` uses a plain Statement for fixed count queries with no user input |
| 10 | Dashboard counts refresh when a window closes | Yes (WindowListener) |
| 11 | Consistent look | Yes, shared `Style` and `FormUtil` |
| 12 | try-with-resources everywhere | Yes |

## 5. Demo rehearsal (Section 9.6)

Rehearse with the whole team using the table in the team guide (A to J). Suggested order and who shows what:
Login (M2) > Database connection in NetBeans Services (M1) > Add unit, tenant, contract, payment (M3/M4) > View and Search in each module
> Update > Delete including a blocked delete > Contract makes unit Occupied and the foreign keys in the database (M4/M1)
> Error handling (empty field, bad number, duplicate, Derby stopped) > Reports (M2).

## 6. Notes and known limits
- Screens are hand-coded Swing (no NetBeans `.form` files), so they will not open in the NetBeans designer.
- A Derby database is a folder on each computer. Everyone creates `apt_db` and runs `schema.sql` (and `sample_data.sql`) themselves.
- Login locks for 5 seconds after 3 failed attempts. Passwords are SHA-256 hashes without salt (fine for the course, see the recommendations).
- The test classes under `src/tests` use `-Dapt.db.url=...` to switch to an embedded database on a build server. In NetBeans you do not need it.
- Keep a shared bug list (GitHub Issues) with the owner's name next to each bug.
