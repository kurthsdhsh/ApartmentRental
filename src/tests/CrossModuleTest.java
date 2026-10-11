package tests;

import dao.*;
import database.DatabaseInitializer;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import javax.swing.table.DefaultTableModel;
import model.*;

/**
 * Cross-module test (guide section 9.4) run at DAO level, with no windows.
 * Run it on a FRESH database (drop_all.sql, schema.sql; do NOT run sample_data.sql) with the Derby server started.
 * In NetBeans: right-click this file, Run File. It prints PASS or FAIL for every step.
 */
public class CrossModuleTest {
    static int pass = 0, fail = 0;

    static void check(String step, boolean ok) {
        System.out.println((ok ? "PASS  " : "FAIL  ") + step);
        if (ok) pass++; else fail++;
    }

    static boolean has(DefaultTableModel m, String text) {
        for (int r = 0; r < m.getRowCount(); r++)
            for (int c = 0; c < m.getColumnCount(); c++)
                if (m.getValueAt(r, c) != null && m.getValueAt(r, c).toString().contains(text)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        UserDAO users = new UserDAO(); UnitDAO units = new UnitDAO(); TenantDAO tenants = new TenantDAO();
        ContractDAO contracts = new ContractDAO(); PaymentDAO payments = new PaymentDAO();
        MaintenanceDAO maint = new MaintenanceDAO(); DashboardDAO dash = new DashboardDAO(); ReportDAO reports = new ReportDAO();
        String month = YearMonth.now().toString();

        // 1. login as admin / admin123
        DatabaseInitializer.ensureDefaultAdmin();
        User admin = users.authenticate("admin", "admin123");
        check("1  Login admin / admin123 returns an Admin user", admin != null && "Admin".equals(admin.getRole()));
        check("1b Wrong password is rejected", users.authenticate("admin", "wrong") == null);

        // 2. add staff1
        User s = new User(); s.setUsername("staff1"); s.setPassword("staff123"); s.setFullName("Staff One");
        s.setRole("Staff"); s.setStatus("Active");
        users.add(s);
        check("2  Staff user saved and appears in the table", users.search("staff1").size() == 1);
        boolean dupBlocked = false;
        try { users.add(s); } catch (SQLException e) { dupBlocked = "23505".equals(e.getSQLState()); }
        check("2b Duplicate username blocked (23505)", dupBlocked);

        // 3. add unit 401
        Unit u = new Unit(0, "401", "Studio", 4, new BigDecimal("7000"), "Available");
        units.add(u);
        Unit u401 = units.search("401").get(0);
        check("3  Unit 401 saved with status Available", "Available".equals(u401.getStatus()));
        boolean dupUnit = false;
        try { units.add(u); } catch (SQLException e) { dupUnit = "23505".equals(e.getSQLState()); }
        check("3b Duplicate unit number blocked (23505)", dupUnit);

        // 4. add tenant Ana Lopez
        Tenant t = new Tenant(0, "Ana", "Lopez", "09171112222", "ana@email.com", "Quezon City");
        tenants.add(t);
        Tenant ana = tenants.search("ana").get(0);
        check("4  Tenant Ana Lopez saved", "Ana Lopez".equals(ana.getFullName()) && "-".equals(ana.getCurrentUnit()));
        check("4b Duplicate tenant detected", tenants.isDuplicate("Ana", "Lopez", "09171112222", 0));

        // 5. create contract -> unit Occupied
        Contract k = new Contract();
        k.setTenantId(ana.getTenantId()); k.setUnitId(u401.getUnitId());
        k.setStartDate(LocalDate.now()); k.setEndDate(LocalDate.now().plusDays(20));
        k.setMonthlyRent(new BigDecimal("7000")); k.setDeposit(new BigDecimal("7000"));
        contracts.createContract(k);
        check("5  Contract created and unit 401 is now Occupied", "Occupied".equals(units.getById(u401.getUnitId()).getStatus()));
        check("5b Tenant list shows the current unit 401", "401".equals(tenants.getById(ana.getTenantId()).getCurrentUnit()));

        // 5c. transaction failure path: leasing the same (now Occupied) unit must fail and leave NO half-created contract
        int before = contracts.getAll().size();
        boolean failed = false;
        try { contracts.createContract(k); } catch (SQLException e) { failed = true; }
        check("5c Leasing an Occupied unit fails and rolls back (no extra contract)",
                failed && contracts.getAll().size() == before);

        // 6. unit combo no longer lists 401
        boolean listed = false;
        for (Unit a : units.getAvailableUnits()) if ("401".equals(a.getUnitNo())) listed = true;
        check("6  Available-units list no longer contains 401", !listed);

        // 7. payment for the current month, received by the logged-in user
        Contract active = contracts.getActiveContracts().get(0);
        Payment p = new Payment();
        p.setContractId(active.getContractId()); p.setUserId(admin.getUserId());
        p.setBillingMonth(month); p.setPaymentDate(LocalDate.now()); p.setAmount(new BigDecimal("7000"));
        payments.add(p);
        Payment saved = payments.getAll().get(0);
        check("7  Payment saved, received by = logged-in user", "System Administrator".equals(saved.getReceivedBy()));
        check("7b Duplicate 'Paid' month is detected", payments.existsPaid(active.getContractId(), month));

        // 8. dashboard counts
        check("8  Dashboard counts changed (occupied 1, active contracts 1, rent this month 7000)",
                dash.occupiedUnits() == 1 && dash.activeContracts() == 1
                && dash.rentCollected(month).compareTo(new BigDecimal("7000")) == 0);

        // 9 and 10. delete protection
        check("9  Unit 401 has contract history (delete blocked)", units.hasHistory(u401.getUnitId()));
        check("10 Tenant Ana has a contract (delete blocked)", tenants.hasHistory(ana.getTenantId()));
        boolean fk = false;
        try { units.delete(u401.getUnitId()); } catch (SQLException e) { fk = "23503".equals(e.getSQLState()); }
        check("10b Database also blocks the delete (23503)", fk);

        // 11. maintenance for unit 401
        MaintenanceRequest m = new MaintenanceRequest();
        m.setUnitId(u401.getUnitId()); m.setTenantId(ana.getTenantId()); m.setDescription("Leaking faucet");
        maint.add(m);
        MaintenanceRequest mr = maint.getAll().get(0);
        check("11 Maintenance saved and shows unit and tenant by name", "401".equals(mr.getUnitNo()) && "Ana Lopez".equals(mr.getTenantName()));
        check("11b Maintenance did not change the unit status", "Occupied".equals(units.getById(u401.getUnitId()).getStatus()));

        // 12. reports
        check("12 Occupancy report shows 401 with Ana", has(reports.unitOccupancy(), "Ana Lopez"));
        check("12 Monthly collection report shows the month", has(reports.monthlyCollection(), month));
        check("12 Overdue report is empty for the paid month", reports.overdueRent(month).getRowCount() == 0);
        check("12 Overdue report lists Ana for an unpaid month", has(reports.overdueRent("2099-01"), "Ana Lopez"));
        check("12 Open maintenance report shows the request", has(reports.openMaintenance(), "Leaking faucet"));
        check("12 Expiring-soon report shows the contract", has(reports.expiringSoon(), "Ana Lopez"));
        check("12 Tenant list report works", has(reports.tenantList(), "Ana Lopez"));

        // contract rules
        check("12b Contract cannot be deleted while it has payments", contracts.countPayments(active.getContractId()) > 0);
        Contract edit = contracts.getById(active.getContractId());
        edit.setDeposit(new BigDecimal("5000"));
        contracts.updateContract(edit);
        check("12c Active contract deposit can be updated", contracts.getById(active.getContractId()).getDeposit().compareTo(new BigDecimal("5000")) == 0);

        // void instead of delete
        Payment p2 = new Payment();
        p2.setContractId(active.getContractId()); p2.setUserId(admin.getUserId());
        p2.setBillingMonth(month); p2.setPaymentDate(LocalDate.now()); p2.setAmount(new BigDecimal("100")); p2.setStatus("Partial");
        payments.add(p2);
        BigDecimal withPartial = payments.totalForMonth(month);
        payments.voidPayment(payments.getAll().get(0).getPaymentId());
        check("12d Voided payment no longer counts in the total",
                withPartial.compareTo(new BigDecimal("7100")) == 0 && payments.totalForMonth(month).compareTo(new BigDecimal("7000")) == 0);

        // 13. end contract
        contracts.endContract(active.getContractId(), "Ended");
        check("13 Contract Ended and unit 401 is Available again",
                "Ended".equals(contracts.getById(active.getContractId()).getStatus())
                && "Available".equals(units.getById(u401.getUnitId()).getStatus()));
        boolean noPay = false;
        Payment p3 = new Payment();
        p3.setContractId(active.getContractId()); p3.setUserId(admin.getUserId());
        p3.setBillingMonth(month); p3.setPaymentDate(LocalDate.now()); p3.setAmount(new BigDecimal("1"));
        try { payments.add(p3); } catch (SQLException e) { noPay = true; }
        check("13b Payments are refused for an Ended contract", noPay);

        // 14. staff1 login and role
        User staff = users.authenticate("staff1", "staff123");
        check("14 staff1 logs in with role Staff (Users menu and Delete buttons are disabled for this role)",
                staff != null && "Staff".equals(staff.getRole()));
        staff.setPassword(""); staff.setStatus("Inactive");
        users.update(staff);
        check("14b Inactive account cannot log in", users.authenticate("staff1", "staff123") == null);
        check("14c Admin count protects the last Admin", users.countAdmins() == 1);

        System.out.println("\nResult: " + pass + " passed, " + fail + " failed.");
        System.out.println("Step 15 (stop the Derby server and click Save) must be checked by hand in the app.");
        System.exit(fail == 0 ? 0 : 1);
    }
}
