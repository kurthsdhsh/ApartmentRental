package dao;

import database.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.Contract;

public class ContractDAO {
    // JOIN of 3 tables
    private static final String BASE =
            "SELECT c.contract_id, c.tenant_id, c.unit_id, t.first_name || ' ' || t.last_name AS tenant, u.unit_no, "
          + "c.start_date, c.end_date, c.monthly_rent, c.status "
          + "FROM CONTRACTS c "
          + "INNER JOIN TENANTS t ON c.tenant_id = t.tenant_id "
          + "INNER JOIN UNITS u ON c.unit_id = u.unit_id ";

    /** Inserts the contract AND sets the unit to Occupied in ONE transaction. */
    public boolean createContract(Contract k) throws SQLException {
        String ins = "INSERT INTO CONTRACTS (tenant_id, unit_id, start_date, end_date, monthly_rent, status) "
                + "VALUES (?, ?, ?, ?, ?, 'Active')";
        String occ = "UPDATE UNITS SET status = 'Occupied' WHERE unit_id = ? AND status = 'Available'";
        try (Connection c = DatabaseConnection.getConnection()) {
            c.setAutoCommit(false);
            try (PreparedStatement p1 = c.prepareStatement(ins); PreparedStatement p2 = c.prepareStatement(occ)) {
                p1.setInt(1, k.getTenantId());
                p1.setInt(2, k.getUnitId());
                p1.setDate(3, java.sql.Date.valueOf(k.getStartDate()));
                p1.setDate(4, java.sql.Date.valueOf(k.getEndDate()));
                p1.setBigDecimal(5, k.getMonthlyRent());
                p1.executeUpdate();
                p2.setInt(1, k.getUnitId());
                if (p2.executeUpdate() == 0) throw new SQLException("This unit is no longer available.");
                c.commit();
                return true;
            } catch (SQLException e) {
                c.rollback();
                throw e;
            } finally {
                c.setAutoCommit(true);
            }
        }
    }

    public boolean add(Contract k) throws SQLException { return createContract(k); }

    /** Only end date and monthly rent may change, and only on an Active contract. */
    public boolean updateContract(Contract k) throws SQLException {
        String sql = "UPDATE CONTRACTS SET end_date=?, monthly_rent=? WHERE contract_id=? AND status='Active'";
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(k.getEndDate()));
            ps.setBigDecimal(2, k.getMonthlyRent());
            ps.setInt(3, k.getContractId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean update(Contract k) throws SQLException { return updateContract(k); }

    /** newStatus = Ended or Terminated. Sets the contract status and frees the unit in ONE transaction. */
    public boolean endContract(int contractId, String newStatus) throws SQLException {
        String sel = "SELECT unit_id FROM CONTRACTS WHERE contract_id = ? AND status = 'Active'";
        String upC = "UPDATE CONTRACTS SET status = ? WHERE contract_id = ?";
        String upU = "UPDATE UNITS SET status = 'Available' WHERE unit_id = ? AND status = 'Occupied'";
        try (Connection c = DatabaseConnection.getConnection()) {
            c.setAutoCommit(false);
            try (PreparedStatement p0 = c.prepareStatement(sel);
                 PreparedStatement p1 = c.prepareStatement(upC);
                 PreparedStatement p2 = c.prepareStatement(upU)) {
                p0.setInt(1, contractId);
                int unitId;
                try (ResultSet rs = p0.executeQuery()) {
                    if (!rs.next()) throw new SQLException("This contract is not active.");
                    unitId = rs.getInt(1);
                }
                p1.setString(1, newStatus);
                p1.setInt(2, contractId);
                p1.executeUpdate();
                p2.setInt(1, unitId);
                p2.executeUpdate();
                c.commit();
                return true;
            } catch (SQLException e) {
                c.rollback();
                throw e;
            } finally {
                c.setAutoCommit(true);
            }
        }
    }

    public int countPayments(int contractId) throws SQLException {
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM PAYMENTS WHERE contract_id = ?")) {
            ps.setInt(1, contractId);
            try (ResultSet rs = ps.executeQuery()) { rs.next(); return rs.getInt(1); }
        }
    }

    /** Allowed only when there are no payments. If the contract was Active the unit is freed too. */
    public boolean delete(int contractId) throws SQLException {
        try (Connection c = DatabaseConnection.getConnection()) {
            c.setAutoCommit(false);
            try (PreparedStatement p0 = c.prepareStatement("SELECT unit_id, status FROM CONTRACTS WHERE contract_id = ?");
                 PreparedStatement p1 = c.prepareStatement("DELETE FROM CONTRACTS WHERE contract_id = ?");
                 PreparedStatement p2 = c.prepareStatement(
                         "UPDATE UNITS SET status = 'Available' WHERE unit_id = ? AND status = 'Occupied'")) {
                p0.setInt(1, contractId);
                int unitId; String status;
                try (ResultSet rs = p0.executeQuery()) {
                    if (!rs.next()) return false;
                    unitId = rs.getInt(1);
                    status = rs.getString(2);
                }
                p1.setInt(1, contractId);
                p1.executeUpdate();
                if ("Active".equals(status)) {
                    p2.setInt(1, unitId);
                    p2.executeUpdate();
                }
                c.commit();
                return true;
            } catch (SQLException e) {
                c.rollback();
                throw e;
            } finally {
                c.setAutoCommit(true);
            }
        }
    }

    public Contract getById(int id) throws SQLException {
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(BASE + "WHERE c.contract_id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? map(rs) : null; }
        }
    }

    public List<Contract> getAll() throws SQLException {
        return query(BASE + "ORDER BY c.contract_id DESC");
    }

    public List<Contract> getActiveContracts() throws SQLException {
        return query(BASE + "WHERE c.status = 'Active' ORDER BY u.unit_no");
    }

    public List<Contract> search(String keyword) throws SQLException { return search(keyword, "All"); }

    public List<Contract> search(String keyword, String statusFilter) throws SQLException {
        String k = "%" + (keyword == null ? "" : keyword.trim().toLowerCase()) + "%";
        boolean filter = statusFilter != null && !"All".equals(statusFilter);
        String sql = BASE + "WHERE (LOWER(t.first_name || ' ' || t.last_name) LIKE ? OR LOWER(u.unit_no) LIKE ? "
                + "OR LOWER(c.status) LIKE ?)" + (filter ? " AND c.status = ?" : "") + " ORDER BY c.contract_id DESC";
        List<Contract> list = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, k); ps.setString(2, k); ps.setString(3, k);
            if (filter) ps.setString(4, statusFilter);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(map(rs)); }
        }
        return list;
    }

    private List<Contract> query(String sql) throws SQLException {
        List<Contract> list = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    private Contract map(ResultSet rs) throws SQLException {
        Contract k = new Contract();
        k.setContractId(rs.getInt("contract_id"));
        k.setTenantId(rs.getInt("tenant_id"));
        k.setUnitId(rs.getInt("unit_id"));
        k.setTenantName(rs.getString("tenant"));
        k.setUnitNo(rs.getString("unit_no"));
        k.setStartDate(rs.getDate("start_date").toLocalDate());
        k.setEndDate(rs.getDate("end_date").toLocalDate());
        k.setMonthlyRent(rs.getBigDecimal("monthly_rent"));
        k.setStatus(rs.getString("status"));
        return k;
    }
}
