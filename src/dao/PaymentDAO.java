package dao;

import database.DatabaseConnection;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.Payment;

public class PaymentDAO {
    // JOIN of 5 tables
    private static final String BASE =
            "SELECT p.payment_id, p.contract_id, p.user_id, t.first_name || ' ' || t.last_name AS tenant, u.unit_no, "
          + "p.billing_month, p.payment_date, p.amount, p.payment_method, p.status, p.remarks, us.full_name AS received_by "
          + "FROM PAYMENTS p "
          + "INNER JOIN CONTRACTS c ON p.contract_id = c.contract_id "
          + "INNER JOIN TENANTS t ON c.tenant_id = t.tenant_id "
          + "INNER JOIN UNITS u ON c.unit_id = u.unit_id "
          + "INNER JOIN USERS us ON p.user_id = us.user_id ";
    private static final String ORDER = " ORDER BY p.payment_date DESC, p.payment_id DESC";

    /** The contract must be Active (re-checked here). userId comes from the Session, set by the frame. */
    public boolean add(Payment p) throws SQLException {
        String chk = "SELECT COUNT(*) FROM CONTRACTS WHERE contract_id = ? AND status = 'Active'";
        String sql = "INSERT INTO PAYMENTS (contract_id, user_id, payment_date, billing_month, amount, "
                + "payment_method, status, remarks) VALUES (?,?,?,?,?,?,?,?)";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement pc = c.prepareStatement(chk); PreparedStatement ps = c.prepareStatement(sql)) {
            pc.setInt(1, p.getContractId());
            try (ResultSet rs = pc.executeQuery()) {
                rs.next();
                if (rs.getInt(1) == 0) throw new SQLException("The contract is not active.");
            }
            ps.setInt(1, p.getContractId());
            ps.setInt(2, p.getUserId());
            ps.setDate(3, java.sql.Date.valueOf(p.getPaymentDate()));
            ps.setString(4, p.getBillingMonth());
            ps.setBigDecimal(5, p.getAmount());
            ps.setString(6, p.getPaymentMethod());
            ps.setString(7, p.getStatus());
            if (p.getRemarks() == null || p.getRemarks().trim().isEmpty()) ps.setNull(8, Types.VARCHAR);
            else ps.setString(8, p.getRemarks().trim());
            return ps.executeUpdate() > 0;
        }
    }

    /** Contract and receiving user never change. */
    public boolean update(Payment p) throws SQLException {
        String sql = "UPDATE PAYMENTS SET billing_month=?, payment_date=?, amount=?, payment_method=?, status=?, "
                + "remarks=? WHERE payment_id=?";
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, p.getBillingMonth());
            ps.setDate(2, java.sql.Date.valueOf(p.getPaymentDate()));
            ps.setBigDecimal(3, p.getAmount());
            ps.setString(4, p.getPaymentMethod());
            ps.setString(5, p.getStatus());
            if (p.getRemarks() == null || p.getRemarks().trim().isEmpty()) ps.setNull(6, Types.VARCHAR);
            else ps.setString(6, p.getRemarks().trim());
            ps.setInt(7, p.getPaymentId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean voidPayment(int id) throws SQLException {
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE PAYMENTS SET status = 'Voided' WHERE payment_id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM PAYMENTS WHERE payment_id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public Payment getById(int id) throws SQLException {
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(BASE + "WHERE p.payment_id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? map(rs) : null; }
        }
    }

    public List<Payment> getAll() throws SQLException {
        List<Payment> list = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(BASE + ORDER); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public List<Payment> search(String keyword) throws SQLException {
        String k = "%" + (keyword == null ? "" : keyword.trim().toLowerCase()) + "%";
        String sql = BASE + "WHERE LOWER(t.first_name || ' ' || t.last_name) LIKE ? OR LOWER(u.unit_no) LIKE ? "
                + "OR p.billing_month LIKE ?" + ORDER;
        List<Payment> list = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, k); ps.setString(2, k); ps.setString(3, k);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(map(rs)); }
        }
        return list;
    }

    /** True when a 'Paid' payment already exists for this contract and billing month. */
    public boolean existsPaid(int contractId, String billingMonth) throws SQLException {
        String sql = "SELECT COUNT(*) FROM PAYMENTS WHERE contract_id = ? AND billing_month = ? AND status = 'Paid'";
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, contractId);
            ps.setString(2, billingMonth);
            try (ResultSet rs = ps.executeQuery()) { rs.next(); return rs.getInt(1) > 0; }
        }
    }

    /** Sum of non-voided payments for a billing month (yyyy-MM). */
    public BigDecimal totalForMonth(String billingMonth) throws SQLException {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM PAYMENTS WHERE billing_month = ? AND status <> 'Voided'";
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, billingMonth);
            try (ResultSet rs = ps.executeQuery()) { rs.next(); return rs.getBigDecimal(1); }
        }
    }
    
    /** Sum of non-voided payments already recorded for one contract and billing month.
     *  excludePaymentId lets Update ignore the payment being edited (use 0 for a new payment). */
    public BigDecimal paidSoFar(int contractId, String billingMonth, int excludePaymentId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM PAYMENTS "
                   + "WHERE contract_id = ? AND billing_month = ? AND status <> 'Voided' AND payment_id <> ?";
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, contractId);
            ps.setString(2, billingMonth);
            ps.setInt(3, excludePaymentId);
            try (ResultSet rs = ps.executeQuery()) { rs.next(); return rs.getBigDecimal(1); }
        }
    }


    /** Non-voided money paid per billing month (yyyy-MM) for one contract. Months with no payment are not in the map. */
    public Map<String, BigDecimal> paidByMonth(int contractId) throws SQLException {
        String sql = "SELECT billing_month, COALESCE(SUM(amount), 0) FROM PAYMENTS "
                   + "WHERE contract_id = ? AND status <> 'Voided' GROUP BY billing_month";
        Map<String, BigDecimal> map = new HashMap<>();
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, contractId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) map.put(rs.getString(1), rs.getBigDecimal(2));
            }
        }
        return map;
    }

    private Payment map(ResultSet rs) throws SQLException {
        Payment p = new Payment();
        p.setPaymentId(rs.getInt("payment_id"));
        p.setContractId(rs.getInt("contract_id"));
        p.setUserId(rs.getInt("user_id"));
        p.setTenantName(rs.getString("tenant"));
        p.setUnitNo(rs.getString("unit_no"));
        p.setBillingMonth(rs.getString("billing_month"));
        p.setPaymentDate(rs.getDate("payment_date").toLocalDate());
        p.setAmount(rs.getBigDecimal("amount"));
        p.setPaymentMethod(rs.getString("payment_method"));
        p.setStatus(rs.getString("status"));
        p.setRemarks(rs.getString("remarks"));
        p.setReceivedBy(rs.getString("received_by"));
        return p;
    }
}
