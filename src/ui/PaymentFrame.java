package ui;

import dao.ContractDAO;
import dao.PaymentDAO;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import model.ComboItem;
import model.Contract;
import model.Payment;
import model.User;
import util.ErrorHelper;
import util.Session;

import java.time.format.DateTimeParseException;
import util.Constants;
import util.Validator;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class PaymentFrame extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(PaymentFrame.class.getName());
    
    private final PaymentDAO dao = new PaymentDAO();
    private final ContractDAO contractDao = new ContractDAO();
    private boolean loading = false; 
    private Contract current;

    public PaymentFrame() {
        initComponents();
        txtSearch.addActionListener(e -> btnSearch.doClick());
        getContentPane().setBackground(new Color(249, 241, 246));

        PillButtonUI.apply(btnSave, Theme.PRIMARY, Color.WHITE);
        PillButtonUI.apply(btnUpdate, Theme.PRIMARY_DARK, Color.WHITE);
        PillButtonUI.apply(btnVoid, Theme.SIDEBAR, Color.WHITE);
        PillButtonUI.apply(btnDelete, Theme.DANGER, Color.WHITE);
        PillButtonUI.apply(btnClear, new Color(160, 160, 160), Color.WHITE);
        PillButtonUI.apply(btnSearch, Theme.PRIMARY, Color.WHITE);
        PillButtonUI.apply(btnRefresh, Theme.PRIMARY_DARK, Color.WHITE);
        PillButtonUI.apply(btnBack, Theme.SIDEBAR, Color.WHITE);
        TableStyle.apply(tblPayments);
        tblPayments.getColumnModel().getColumn(0).setMaxWidth(60);

        installStatusColors();
        DatePicker.attach(txtPaymentDate, null, null); 
        cmbStatus.setEnabled(false); 
        txtAmount.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                previewStatus();
            }
        });
        setLocationRelativeTo(null);
        txtPaymentId.setEditable(false);       
        btnDelete.setEnabled(Session.isAdmin());
        resetDefaults();
        loadActiveContracts();
        refreshTable();
        updateSummary();
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
private static void addItem(JComboBox combo, ComboItem item) {
    combo.addItem(item);
}

// lists only active contracts
private void loadActiveContracts() {
    loading = true;
    try {
        cmbContract.removeAllItems();
        List<Contract> list = contractDao.getActiveContracts();
        for (Contract k : list) {
            addItem(cmbContract, new ComboItem(k.getContractId(),
                    "C-" + k.getContractId() + " | Unit " + k.getUnitNo() + " | " + k.getTenantName()));
        }
        if (list.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No active contracts. Create a contract first.");
        }
    } catch (SQLException ex) {
        ErrorHelper.show(this, ex);
    } finally {
        loading = false;
    }
    onContractChanged();
}


/** The billing months a contract covers: one per month of the lease, starting at the start month. */
private static List<YearMonth> monthsOf(Contract k) {
    YearMonth first = YearMonth.from(k.getStartDate());
    long n = ChronoUnit.MONTHS.between(k.getStartDate(), k.getEndDate().plusDays(1));
    if (n < 1) n = 1;
    List<YearMonth> list = new ArrayList<>();
    for (int i = 0; i < n; i++) list.add(first.plusMonths(i));
    return list;
}

private String selectedMonth() {
    Object m = cmbBillingMonth.getSelectedItem();
    return m == null ? null : m.toString();
}

private int excludeId() {
    String id = txtPaymentId.getText().trim();
    return id.isEmpty() ? 0 : Integer.parseInt(id);
}

// fills the Billing Month list with ONLY the months of the contract
private void loadMonths(Contract k, String select) {
    loading = true;
    try {
        cmbBillingMonth.removeAllItems();
        boolean found = false;
        for (YearMonth m : monthsOf(k)) {
            cmbBillingMonth.addItem(m.toString());
            if (m.toString().equals(select)) found = true;
        }
        if (select != null) {
            if (!found) cmbBillingMonth.addItem(select); 
            cmbBillingMonth.setSelectedItem(select);
        }
    } finally {
        loading = false;
    }
}

// the first month that is not fully paid yet (or the last month when everything is paid)
private String firstUnpaidMonth(Contract k) throws SQLException {
    Map<String, BigDecimal> paid = dao.paidByMonth(k.getContractId());
    List<YearMonth> months = monthsOf(k);
    for (YearMonth m : months) {
        BigDecimal p = paid.getOrDefault(m.toString(), BigDecimal.ZERO);
        if (p.compareTo(k.getMonthlyRent()) < 0) return m.toString();
    }
    return months.get(months.size() - 1).toString();
}

// called when another contract is chosen (or the list is reloaded)
private void onContractChanged() {
    Object sel = cmbContract.getSelectedItem();
    if (!(sel instanceof ComboItem)) {
        current = null;
        loading = true;
        cmbBillingMonth.removeAllItems();
        loading = false;
        txtAmount.setText("");
        refreshBalance();
        return;
    }
    try {
        current = contractDao.getById(((ComboItem) sel).getId());
        if (current == null) return;
        loadMonths(current, firstUnpaidMonth(current));
        fillAmountForMonth();
        previewStatus();
        refreshBalance();
    } catch (SQLException ex) {
        ErrorHelper.show(this, ex);
    }
}

// suggests the amount: what is still unpaid for the chosen month
private void fillAmountForMonth() {
    String month = selectedMonth();
    if (current == null || month == null) {
        txtAmount.setText("");
        return;
    }
    try {
        BigDecimal balance = current.getMonthlyRent()
                .subtract(dao.paidSoFar(current.getContractId(), month, 0));
        txtAmount.setText(balance.signum() > 0 ? balance.toPlainString() : "");
    } catch (SQLException ex) {
        ErrorHelper.show(this, ex);
    }
}

// the Status box only shows the result: enough money for the month = Paid, otherwise Partial
private void previewStatus() {
    if (current == null) return;
    if (!txtPaymentId.getText().isEmpty() && Constants.PAY_VOIDED.equals(String.valueOf(cmbStatus.getSelectedItem()))) {
        return;                                     // a voided record keeps showing Voided
    }
    String month = selectedMonth();
    if (month == null) return;
    BigDecimal amount;
    try {
        amount = Validator.parsePositive(txtAmount.getText());
    } catch (NumberFormatException ex) {
        return;
    }
    try {
        BigDecimal total = dao.paidSoFar(current.getContractId(), month, excludeId()).add(amount);
        cmbStatus.setSelectedItem(total.compareTo(current.getMonthlyRent()) >= 0
                ? Constants.PAY_PAID : Constants.PAY_PARTIAL);
    } catch (SQLException ex) {
        // preview only; the real check happens on save
    }
}

private void showReceivedBy() {
    User u = Session.getCurrentUser();
    lblReceivedBy.setText(u == null ? "-" : u.getFullName());
}

private void resetDefaults() {
    txtPaymentId.setText("");
    txtPaymentDate.setText(LocalDate.now().toString());  
    cmbMethod.setSelectedIndex(0);
    cmbStatus.setSelectedItem("Paid");
    txtRemarks.setText("");
    showReceivedBy();
}

private void refreshTable() {
    try {
        fillTable(dao.getAll());
    } catch (SQLException ex) {
        ErrorHelper.show(this, ex);
    }
}

private void fillTable(List<Payment> list) {
    DefaultTableModel m = (DefaultTableModel) tblPayments.getModel();
    m.setRowCount(0);
    for (Payment p : list) {
        m.addRow(new Object[]{
            p.getPaymentId(),
            p.getTenantName(),
            p.getUnitNo(),
            p.getBillingMonth(),
            p.getPaymentDate(),
            p.getAmount(),
            p.getPaymentMethod(),
            p.getStatus(),
            p.getReceivedBy()
        });
    }
    lblInfo.setText(list.isEmpty() ? "No records found." : list.size() + " payment(s)");
}

private void installStatusColors() {
    tblPayments.getColumnModel().getColumn(7).setCellRenderer(new DefaultTableCellRenderer() {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (!isSelected) {
                String s = value == null ? "" : value.toString();
                if (Constants.PAY_PAID.equals(s)) {
                    c.setBackground(new Color(214, 240, 220));
                    c.setForeground(new Color(20, 100, 50));
                } else if (Constants.PAY_PARTIAL.equals(s)) {
                    c.setBackground(new Color(255, 236, 200));
                    c.setForeground(new Color(150, 90, 0));
                } else {
                    c.setBackground(Color.WHITE);
                    c.setForeground(Color.BLACK);
                }
            }
            return c;
        }
    });
}

private void updateSummary() {
    try {
        BigDecimal total = dao.totalForMonth(YearMonth.now().toString());
        lblSummary.setText(String.format("Total collected this month: PHP %,.2f", total));
    } catch (SQLException ex) {
        ErrorHelper.show(this, ex);
    }
}

// shows how much of the chosen month is paid, and which months of the contract are still unpaid
private void refreshBalance() {
    String month = selectedMonth();
    if (current == null || month == null) {
        lblBalance.setText("Paid so far: - | Balance: -");
        lblUnpaid.setText(" ");
        lblUnpaid.setToolTipText(null);
        return;
    }
    try {
        Map<String, BigDecimal> paidByMonth = dao.paidByMonth(current.getContractId());
        BigDecimal rent = current.getMonthlyRent();
        BigDecimal paid = paidByMonth.getOrDefault(month, BigDecimal.ZERO);
        BigDecimal balance = rent.subtract(paid);
        String tag = month.equals(YearMonth.from(current.getStartDate()).toString()) ? " (move-in advance)" : "";
        if (balance.signum() <= 0) {
            lblBalance.setText(String.format("%s%s: fully paid (PHP %,.2f of PHP %,.2f)", month, tag, paid, rent));
        } else {
            lblBalance.setText(String.format("%s%s | Paid so far: PHP %,.2f | Balance: PHP %,.2f",
                    month, tag, paid, balance));
        }
        showUnpaidMonths(paidByMonth);
    } catch (SQLException ex) {
        lblBalance.setText("Cannot load the balance right now.");
        lblUnpaid.setText(" ");
    }
}

// one line for the whole contract: how many months are paid and which ones are already due but unpaid
private void showUnpaidMonths(Map<String, BigDecimal> paidByMonth) {
    List<YearMonth> months = monthsOf(current);
    BigDecimal rent = current.getMonthlyRent();
    YearMonth now = YearMonth.now();
    int fullyPaid = 0, notYetDue = 0;
    List<String> overdue = new ArrayList<>();
    for (YearMonth m : months) {
        BigDecimal left = rent.subtract(paidByMonth.getOrDefault(m.toString(), BigDecimal.ZERO));
        if (left.signum() <= 0) fullyPaid++;
        else if (m.isAfter(now)) notYetDue++;
        else overdue.add(String.format("%s (%,.2f)", m, left));
    }
    StringBuilder text = new StringBuilder(String.format("Rent paid: %d of %d months", fullyPaid, months.size()));
    if (overdue.isEmpty()) {
        text.append(" | No unpaid months so far");
        lblUnpaid.setForeground(new Color(20, 100, 50));
    } else {
        int shown = Math.min(3, overdue.size());
        text.append(" | UNPAID: ").append(String.join(", ", overdue.subList(0, shown)));
        if (overdue.size() > shown) text.append(" +").append(overdue.size() - shown).append(" more");
        lblUnpaid.setForeground(new Color(170, 30, 30));
    }
    if (notYetDue > 0) text.append(" | ").append(notYetDue).append(" not yet due");
    lblUnpaid.setText(text.toString());
    lblUnpaid.setToolTipText(overdue.isEmpty() ? null : "Unpaid months: " + String.join(", ", overdue));
}

private void clearForm() {
    resetDefaults();
    cmbContract.setEnabled(true);   
    tblPayments.clearSelection();
    loadActiveContracts();   
}

private void warn(String message) {
    JOptionPane.showMessageDialog(this, message, "Validation", JOptionPane.WARNING_MESSAGE);
}

private void showDbError(SQLException ex) {
    String msg = ex.getMessage();
    if (ex.getSQLState() == null && msg != null && msg.startsWith("The contract is not active")) {
        JOptionPane.showMessageDialog(this,
                "This contract is no longer active, so it cannot receive payments.",
                "Cannot Continue", JOptionPane.WARNING_MESSAGE);
        loadActiveContracts();
    } else {
        ErrorHelper.show(this, ex);
    }
}

//validates the form and decides the status (Paid / Partial) from the amount
private Payment readForm() {
    Object c = cmbContract.getSelectedItem();
    if (!(c instanceof ComboItem) || current == null) {
        warn("Select a contract. If the list is empty, create a contract first.");
        return null;
    }

    String month = selectedMonth();
    if (month == null) {
        warn("Select a billing month.");
        return null;
    }

    LocalDate date;
    try {
        date = Validator.parseDate(txtPaymentDate.getText());
    } catch (DateTimeParseException ex) {
        warn("Select a payment date.");
        return null;
    }
    if (date.isAfter(LocalDate.now())) {
        warn("Payment date cannot be in the future.");
        return null;
    }

    // how much of this month is still unpaid (the payment being edited is not counted against itself)
    BigDecimal rent = current.getMonthlyRent();
    BigDecimal remaining;
    try {
        remaining = rent.subtract(dao.paidSoFar(current.getContractId(), month, excludeId()));
    } catch (SQLException ex) {
        ErrorHelper.show(this, ex);
        return null;
    }
    if (remaining.signum() <= 0) {
        JOptionPane.showMessageDialog(this,
                String.format("Billing month %s is already fully paid (PHP %,.2f).\n"
                        + "No further payment is needed for this month.", month, rent),
                "Already Paid", JOptionPane.WARNING_MESSAGE);
        return null;
    }

    BigDecimal amount;
    try {
        amount = Validator.parsePositive(txtAmount.getText());
    } catch (NumberFormatException ex) {
        warn("Amount must be a number greater than 0.");
        txtAmount.requestFocus();
        return null;
    }
    if (amount.compareTo(remaining) > 0) {
        warn(String.format("Only PHP %,.2f is left to pay for %s.\nPlease enter that amount or less.", remaining, month));
        txtAmount.requestFocus();
        return null;
    }

    String remarks = txtRemarks.getText().trim();
    if (remarks.length() > 255) {
        warn("Remarks must be 255 characters or less.");
        return null;
    }

    Payment p = new Payment();
    p.setContractId(((ComboItem) c).getId());
    p.setBillingMonth(month);
    p.setPaymentDate(date);
    p.setAmount(amount);
    p.setPaymentMethod(cmbMethod.getSelectedItem().toString());
    p.setStatus(amount.compareTo(remaining) == 0 ? Constants.PAY_PAID : Constants.PAY_PARTIAL);
    p.setRemarks(remarks);
    return p;
}

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        pnlForm = new javax.swing.JPanel();
        lblPaymentId = new javax.swing.JLabel();
        txtPaymentId = new javax.swing.JTextField();
        lblContract = new javax.swing.JLabel();
        cmbContract = new javax.swing.JComboBox<>();
        lblMonth = new javax.swing.JLabel();
        cmbBillingMonth = new javax.swing.JComboBox<>();
        lblPayDate = new javax.swing.JLabel();
        txtPaymentDate = new javax.swing.JTextField();
        lblAmount = new javax.swing.JLabel();
        txtAmount = new javax.swing.JTextField();
        lblMethod = new javax.swing.JLabel();
        cmbMethod = new javax.swing.JComboBox<>();
        lblStatus = new javax.swing.JLabel();
        cmbStatus = new javax.swing.JComboBox<>();
        lblReceivedByCaption = new javax.swing.JLabel();
        lblReceivedBy = new javax.swing.JLabel();
        lblRemarks = new javax.swing.JLabel();
        txtRemarks = new javax.swing.JTextField();
        jPanel1 = new javax.swing.JPanel();
        btnSave = new javax.swing.JButton();
        btnUpdate = new javax.swing.JButton();
        btnVoid = new javax.swing.JButton();
        btnDelete = new javax.swing.JButton();
        btnClear = new javax.swing.JButton();
        lblBalance = new javax.swing.JLabel();
        lblUnpaid = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        lblSearch = new javax.swing.JLabel();
        txtSearch = new javax.swing.JTextField();
        btnSearch = new javax.swing.JButton();
        btnRefresh = new javax.swing.JButton();
        btnBack = new javax.swing.JButton();
        scrlPayments = new javax.swing.JScrollPane();
        tblPayments = new javax.swing.JTable();
        lblSummary = new javax.swing.JLabel();
        lblInfo = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Payment Management");
        setPreferredSize(new java.awt.Dimension(1020, 720));

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 26)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(140, 40, 100));
        jLabel1.setText("PAYMENT MANAGEMENT");

        pnlForm.setBackground(new java.awt.Color(255, 255, 255));

        lblPaymentId.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblPaymentId.setForeground(new java.awt.Color(60, 40, 55));
        lblPaymentId.setText("Payment ID");

        txtPaymentId.setEditable(false);
        txtPaymentId.setBackground(new java.awt.Color(243, 238, 241));
        txtPaymentId.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N

        lblContract.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblContract.setForeground(new java.awt.Color(60, 40, 55));
        lblContract.setText("Contract");

        cmbContract.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        cmbContract.addActionListener(this::cmbContractActionPerformed);

        lblMonth.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblMonth.setForeground(new java.awt.Color(60, 40, 55));
        lblMonth.setText("Billing Month");

        cmbBillingMonth.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        cmbBillingMonth.addActionListener(this::cmbBillingMonthActionPerformed);

        lblPayDate.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblPayDate.setForeground(new java.awt.Color(60, 40, 55));
        lblPayDate.setText("Payment Date");

        txtPaymentDate.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N

        lblAmount.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblAmount.setForeground(new java.awt.Color(60, 40, 55));
        lblAmount.setText("Amount");

        txtAmount.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N

        lblMethod.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblMethod.setForeground(new java.awt.Color(60, 40, 55));
        lblMethod.setText("Method");

        cmbMethod.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        cmbMethod.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Cash", "GCash", "Bank Transfer" }));

        lblStatus.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblStatus.setForeground(new java.awt.Color(60, 40, 55));
        lblStatus.setText("Status");

        cmbStatus.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        cmbStatus.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Paid", "Partial", "Voided" }));

        lblReceivedByCaption.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblReceivedByCaption.setForeground(new java.awt.Color(60, 40, 55));
        lblReceivedByCaption.setText("Received By");

        lblReceivedBy.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblReceivedBy.setForeground(new java.awt.Color(60, 40, 55));
        lblReceivedBy.setText("-");

        lblRemarks.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblRemarks.setForeground(new java.awt.Color(60, 40, 55));
        lblRemarks.setText("Remarks");

        txtRemarks.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        btnSave.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnSave.setText("SAVE");
        btnSave.addActionListener(this::btnSaveActionPerformed);

        btnUpdate.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnUpdate.setText("UPDATE");
        btnUpdate.addActionListener(this::btnUpdateActionPerformed);

        btnVoid.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnVoid.setText("VOID PAYMENT");
        btnVoid.addActionListener(this::btnVoidActionPerformed);

        btnDelete.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnDelete.setText("DELETE");
        btnDelete.addActionListener(this::btnDeleteActionPerformed);

        btnClear.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnClear.setText("CLEAR");
        btnClear.addActionListener(this::btnClearActionPerformed);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(btnSave, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(btnUpdate, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(btnVoid, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(btnDelete, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(btnClear, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(btnSave, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(btnUpdate, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(btnVoid, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(btnDelete, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(btnClear, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        lblBalance.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblBalance.setForeground(new java.awt.Color(60, 40, 55));
        lblBalance.setText("Paid so far: - | Balance: -");

        lblUnpaid.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblUnpaid.setForeground(new java.awt.Color(60, 40, 55));
        lblUnpaid.setText(" ");

        javax.swing.GroupLayout pnlFormLayout = new javax.swing.GroupLayout(pnlForm);
        pnlForm.setLayout(pnlFormLayout);
        pnlFormLayout.setHorizontalGroup(
            pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlFormLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlFormLayout.createSequentialGroup()
                        .addGroup(pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblPaymentId, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblContract, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblMonth, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblReceivedByCaption, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtPaymentId)
                            .addComponent(cmbContract, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(cmbBillingMonth, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblReceivedBy, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(28, 28, 28)
                        .addGroup(pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblPayDate, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblAmount, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblMethod, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtPaymentDate)
                            .addComponent(txtAmount)
                            .addComponent(cmbMethod, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(cmbStatus, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(pnlFormLayout.createSequentialGroup()
                        .addComponent(lblRemarks, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtRemarks))
                    .addComponent(lblBalance)
                    .addComponent(lblUnpaid))
                .addGap(28, 28, 28)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16))
        );
        pnlFormLayout.setVerticalGroup(
            pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlFormLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlFormLayout.createSequentialGroup()
                        .addGroup(pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(pnlFormLayout.createSequentialGroup()
                                .addGroup(pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(lblPaymentId)
                                    .addComponent(txtPaymentId, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(10, 10, 10)
                                .addGroup(pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(lblContract)
                                    .addComponent(cmbContract, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(10, 10, 10)
                                .addGroup(pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(lblMonth)
                                    .addComponent(cmbBillingMonth, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addGroup(pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(lblReceivedByCaption)
                                    .addComponent(lblReceivedBy)))
                            .addGroup(pnlFormLayout.createSequentialGroup()
                                .addGroup(pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(lblPayDate)
                                    .addComponent(txtPaymentDate, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(10, 10, 10)
                                .addGroup(pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(lblAmount)
                                    .addComponent(txtAmount, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(10, 10, 10)
                                .addGroup(pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(lblMethod)
                                    .addComponent(cmbMethod, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(10, 10, 10)
                                .addGroup(pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(lblStatus)
                                    .addComponent(cmbStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addGap(10, 10, 10)
                        .addGroup(pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblRemarks)
                            .addComponent(txtRemarks, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(10, 10, 10)
                        .addComponent(lblBalance)
                        .addGap(4, 4, 4)
                        .addComponent(lblUnpaid))
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(22, Short.MAX_VALUE))
        );

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));

        lblSearch.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblSearch.setForeground(new java.awt.Color(60, 40, 55));
        lblSearch.setText("Search (tenant name, unit no. or billing month):");

        txtSearch.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        txtSearch.addActionListener(this::txtSearchActionPerformed);

        btnSearch.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnSearch.setText("SEARCH");
        btnSearch.addActionListener(this::btnSearchActionPerformed);

        btnRefresh.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnRefresh.setText("REFRESH");
        btnRefresh.addActionListener(this::btnRefreshActionPerformed);

        btnBack.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnBack.setText("Back to Dashboard");
        btnBack.addActionListener(this::btnBackActionPerformed);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(lblSearch)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 214, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnRefresh, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnBack, javax.swing.GroupLayout.PREFERRED_SIZE, 164, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(45, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblSearch)
                    .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnRefresh, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnBack, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        tblPayments.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "Tenant", "Unit", "Billing Month", "Payment Date", "Amount", "Method", "Status", "Received By"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblPayments.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tblPayments.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblPaymentsMouseClicked(evt);
            }
        });
        scrlPayments.setViewportView(tblPayments);

        lblSummary.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblSummary.setForeground(new java.awt.Color(130, 110, 125));
        lblSummary.setText("Total collected this month: PHP 0.00");

        lblInfo.setForeground(new java.awt.Color(130, 110, 125));
        lblInfo.setText(" ");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(lblInfo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(pnlForm, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(scrlPayments, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblSummary, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(24, 24, 24))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(jLabel1)
                .addGap(34, 34, 34)
                .addComponent(pnlForm, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(scrlPayments, javax.swing.GroupLayout.PREFERRED_SIZE, 209, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblInfo)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblSummary)
                .addContainerGap(9, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteActionPerformed
        if (txtPaymentId.getText().isEmpty()) {
            warn("Select a payment from the table first.");
            return;
        }
        int ok = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this record?\n(Voiding keeps the history. Delete removes it permanently.)",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;

        try {
            dao.delete(Integer.parseInt(txtPaymentId.getText()));
            JOptionPane.showMessageDialog(this, "Payment deleted.");
            clearForm();
            refreshTable();
            updateSummary();
        } catch (SQLException ex) {
            showDbError(ex);
        }
    }//GEN-LAST:event_btnDeleteActionPerformed

    private void btnSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSearchActionPerformed
        try {
            List<Payment> list = dao.search(txtSearch.getText());
            fillTable(list);
            if (list.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No records found.");
            }
        } catch (SQLException ex) {
            ErrorHelper.show(this, ex);
        }
    }//GEN-LAST:event_btnSearchActionPerformed

    private void cmbContractActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbContractActionPerformed
        if (loading || !cmbContract.isEnabled()) return;
        onContractChanged();
    }//GEN-LAST:event_cmbContractActionPerformed

    private void btnClearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClearActionPerformed
        clearForm();
    }//GEN-LAST:event_btnClearActionPerformed

    private void tblPaymentsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblPaymentsMouseClicked
        int r = tblPayments.getSelectedRow();
        if (r < 0) return;

        try {
            int id = Integer.parseInt(tblPayments.getValueAt(r, 0).toString());
            Payment p = dao.getById(id); 
            if (p == null) {
                JOptionPane.showMessageDialog(this, "This payment no longer exists. Click REFRESH.");
                return;
            }

            txtPaymentId.setText(String.valueOf(p.getPaymentId()));

            loading = true;
            cmbContract.removeAllItems();
            addItem(cmbContract, new ComboItem(p.getContractId(),
                    "C-" + p.getContractId() + " | Unit " + p.getUnitNo() + " | " + p.getTenantName()));
            loading = false;

            current = contractDao.getById(p.getContractId());
            if (current != null) loadMonths(current, p.getBillingMonth());
            txtPaymentDate.setText(p.getPaymentDate().toString());
            txtAmount.setText(p.getAmount().toPlainString());
            cmbMethod.setSelectedItem(p.getPaymentMethod());
            cmbStatus.setSelectedItem(p.getStatus());
            lblReceivedBy.setText(p.getReceivedBy());
            txtRemarks.setText(p.getRemarks() == null ? "" : p.getRemarks());

            cmbContract.setEnabled(false);
            refreshBalance();
        } catch (SQLException ex) {
            ErrorHelper.show(this, ex);
        }
    }//GEN-LAST:event_tblPaymentsMouseClicked

    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveActionPerformed
        if (!txtPaymentId.getText().isEmpty()) {
            warn("A payment is selected. Click CLEAR first to record a new payment.");
            return;
        }
        User user = Session.getCurrentUser();
        if (user == null) {
            warn("You must be logged in to record a payment. Open this screen from the Dashboard.");
            return;
        }
        Payment p = readForm();
        if (p == null) return; 
        p.setUserId(user.getUserId()); 

        try {
            dao.add(p);
            JOptionPane.showMessageDialog(this, "Payment saved successfully (" + p.getStatus() + ").");
            clearForm();
            refreshTable();
            updateSummary();
        } catch (SQLException ex) {
            showDbError(ex);
        }
    }//GEN-LAST:event_btnSaveActionPerformed

    private void btnUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpdateActionPerformed
        if (txtPaymentId.getText().isEmpty()) {
            warn("Select a payment from the table first.");
            return;
        }
        if (Constants.PAY_VOIDED.equals(cmbStatus.getSelectedItem().toString())) {
            warn("A voided payment cannot be edited.");
            return;
        }
        Payment p = readForm();
        if (p == null) return;
        p.setPaymentId(Integer.parseInt(txtPaymentId.getText()));

        try {
            Payment current = dao.getById(p.getPaymentId()); 
            if (current == null) {
                warn("This payment no longer exists. Click REFRESH.");
                return;
            }
            if (Constants.PAY_VOIDED.equals(current.getStatus())) {
                warn("A voided payment cannot be edited.");
                return;
            }
            
            dao.update(p);
            JOptionPane.showMessageDialog(this, "Payment updated successfully.");
            clearForm();
            refreshTable();
            updateSummary();
        } catch (SQLException ex) {
            showDbError(ex);
        }
    }//GEN-LAST:event_btnUpdateActionPerformed

    private void btnVoidActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnVoidActionPerformed
        if (txtPaymentId.getText().isEmpty()) {
            warn("Select a payment from the table first.");
            return;
        }
        int id = Integer.parseInt(txtPaymentId.getText());

        try {
            Payment current = dao.getById(id);
            if (current == null) {
                warn("This payment no longer exists. Click REFRESH.");
                return;
            }
            if (Constants.PAY_VOIDED.equals(current.getStatus())) {
                warn("This payment is already voided.");
                return;
            }
            int ok = JOptionPane.showConfirmDialog(this,
                    "Void this payment? It stays in the records but no longer counts as collected money.",
                    "Confirm Void", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (ok != JOptionPane.YES_OPTION) return;

            dao.voidPayment(id);
            JOptionPane.showMessageDialog(this, "Payment voided.");
            clearForm();
            refreshTable();
            updateSummary();
        } catch (SQLException ex) {
            showDbError(ex);
        }
    }//GEN-LAST:event_btnVoidActionPerformed

    private void btnRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshActionPerformed
        txtSearch.setText("");
        refreshTable();
        updateSummary();
        if (txtPaymentId.getText().isEmpty()) {   
            loadActiveContracts();               
        }
    }//GEN-LAST:event_btnRefreshActionPerformed

    private void cmbBillingMonthActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbBillingMonthActionPerformed
        if (loading || current == null) return;
        if (txtPaymentId.getText().isEmpty()) {      // a new payment: suggest what is left for that month
            fillAmountForMonth();
        }
        previewStatus();
        refreshBalance();
    }//GEN-LAST:event_cmbBillingMonthActionPerformed

    private void btnBackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBackActionPerformed
        dispose();      // closes this window; the Dashboard comes back
    }//GEN-LAST:event_btnBackActionPerformed

    private void txtSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtSearchActionPerformed
    btnSearch.doClick();
    }//GEN-LAST:event_txtSearchActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new PaymentFrame().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBack;
    private javax.swing.JButton btnClear;
    private javax.swing.JButton btnDelete;
    private javax.swing.JButton btnRefresh;
    private javax.swing.JButton btnSave;
    private javax.swing.JButton btnSearch;
    private javax.swing.JButton btnUpdate;
    private javax.swing.JButton btnVoid;
    private javax.swing.JComboBox<String> cmbBillingMonth;
    private javax.swing.JComboBox<String> cmbContract;
    private javax.swing.JComboBox<String> cmbMethod;
    private javax.swing.JComboBox<String> cmbStatus;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JLabel lblAmount;
    private javax.swing.JLabel lblBalance;
    private javax.swing.JLabel lblContract;
    private javax.swing.JLabel lblInfo;
    private javax.swing.JLabel lblMethod;
    private javax.swing.JLabel lblMonth;
    private javax.swing.JLabel lblPayDate;
    private javax.swing.JLabel lblPaymentId;
    private javax.swing.JLabel lblReceivedBy;
    private javax.swing.JLabel lblReceivedByCaption;
    private javax.swing.JLabel lblRemarks;
    private javax.swing.JLabel lblSearch;
    private javax.swing.JLabel lblStatus;
    private javax.swing.JLabel lblSummary;
    private javax.swing.JLabel lblUnpaid;
    private javax.swing.JPanel pnlForm;
    private javax.swing.JScrollPane scrlPayments;
    private javax.swing.JTable tblPayments;
    private javax.swing.JTextField txtAmount;
    private javax.swing.JTextField txtPaymentDate;
    private javax.swing.JTextField txtPaymentId;
    private javax.swing.JTextField txtRemarks;
    private javax.swing.JTextField txtSearch;
    // End of variables declaration//GEN-END:variables
}
