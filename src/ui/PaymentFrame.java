package ui;

import dao.ContractDAO;
import dao.PaymentDAO;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
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

    public PaymentFrame() {
        initComponents();
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
        setLocationRelativeTo(null);
        txtPaymentId.setEditable(false);       
        btnDelete.setEnabled(Session.isAdmin());
        resetDefaults();
        loadActiveContracts();
        refreshTable();
        updateSummary();
        refreshBalance();
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
    fillAmountFromSelectedContract();
    refreshBalance();
}

// suggests the amount
private void fillAmountFromSelectedContract() {
    Object sel = cmbContract.getSelectedItem();
    if (!(sel instanceof ComboItem)) {
        txtAmount.setText("");
        return;
    }
    try {
        Contract k = contractDao.getById(((ComboItem) sel).getId());
        if (k == null) return;

        BigDecimal amount = k.getMonthlyRent();       
        try {
            YearMonth month = YearMonth.parse(txtBillingMonth.getText().trim());
            BigDecimal balance = k.getMonthlyRent()
                    .subtract(dao.paidSoFar(k.getContractId(), month.toString(), 0));
            if (balance.signum() > 0) amount = balance; 
        } catch (DateTimeParseException ex) {
        }
        txtAmount.setText(amount.toPlainString());
    } catch (SQLException ex) {
        ErrorHelper.show(this, ex);
    }
}

private void showReceivedBy() {
    User u = Session.getCurrentUser();
    lblReceivedBy.setText(u == null ? "-" : u.getFullName());
}

private void resetDefaults() {
    txtPaymentId.setText("");
    txtBillingMonth.setText(YearMonth.now().toString()); 
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

// shows how much of the chosen contract's rent is already paid 
private void refreshBalance() {
    Object sel = cmbContract.getSelectedItem();
    if (!(sel instanceof ComboItem)) {
        lblBalance.setText("Paid so far: - | Balance: -");
        return;
    }
    YearMonth month;
    try {
        month = YearMonth.parse(txtBillingMonth.getText().trim());
    } catch (DateTimeParseException ex) {
        lblBalance.setText("Enter a billing month (yyyy-MM) to see the balance.");
        return;
    }
    try {
        Contract k = contractDao.getById(((ComboItem) sel).getId());
        if (k == null) return;
        BigDecimal rent = k.getMonthlyRent();
        
                if (contractDao.countPayments(k.getContractId()) == 0) {
            lblBalance.setText(String.format(
                    "No payments yet. Move-in advance: PHP %,.2f for billing month %s",
                    rent, YearMonth.from(k.getStartDate())));
            return;
        }
                
        BigDecimal paid = dao.paidSoFar(k.getContractId(), month.toString(), 0); // 0 = count every payment
        BigDecimal balance = rent.subtract(paid);
        if (balance.signum() <= 0) {
            lblBalance.setText(String.format("%s: fully paid (PHP %,.2f of PHP %,.2f)", month, paid, rent));
        } else {
            lblBalance.setText(String.format("%s | Paid so far: PHP %,.2f | Balance: PHP %,.2f", month, paid, balance));
        }
    } catch (SQLException ex) {
        lblBalance.setText("Cannot load the balance right now.");
    }
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

//validates the form
private Payment readForm() {
    Object c = cmbContract.getSelectedItem();
    if (!(c instanceof ComboItem)) {
        warn("Select a contract. If the list is empty, create a contract first.");
        return null;
    }

    String month = txtBillingMonth.getText().trim();
    YearMonth billing;
    try {
        billing = YearMonth.parse(month);               
    } catch (DateTimeParseException ex) {
        warn("Billing month must use yyyy-MM, for example 2026-10.");
        txtBillingMonth.requestFocus();
        return null;
    }
    
    // the billing month must fall inside the contract period
    try {
        Contract k = contractDao.getById(((ComboItem) c).getId());
        if (k == null) {
            warn("This contract no longer exists. Click REFRESH.");
            return null;
        }
        YearMonth first = YearMonth.from(k.getStartDate());
        YearMonth last = YearMonth.from(k.getEndDate());
        if (billing.isBefore(first) || billing.isAfter(last)) {
            warn("Billing month must be within the contract period (" + first + " to " + last + ").");
            txtBillingMonth.requestFocus();
            return null;
        }
    } catch (SQLException ex) {
        ErrorHelper.show(this, ex);
        return null;
    }


    LocalDate date;
    try {
        date = Validator.parseDate(txtPaymentDate.getText());
    } catch (DateTimeParseException ex) {
        warn("Payment date must be valid and use yyyy-MM-dd, for example 2026-10-07.");
        txtPaymentDate.requestFocus();
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

    String status = cmbStatus.getSelectedItem().toString();
    if (Constants.PAY_VOIDED.equals(status)) {
        warn("To void a payment, use the VOID PAYMENT button.");
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
    p.setStatus(status);
    p.setRemarks(remarks);
    return p;
}

// checks that the status matches the money. 
    private boolean confirmStatusMatchesAmount(Payment p, int excludePaymentId) throws SQLException {
        Contract k = contractDao.getById(p.getContractId());
        if (k == null) return true;                    
        BigDecimal rent = k.getMonthlyRent();
        BigDecimal total = dao.paidSoFar(p.getContractId(), p.getBillingMonth(), excludePaymentId)
                              .add(p.getAmount());
        boolean markedPaid = Constants.PAY_PAID.equals(p.getStatus());

        String suggestion = null, msg = null;
        if (markedPaid && total.compareTo(rent) < 0) {
            suggestion = Constants.PAY_PARTIAL;
            msg = String.format("With this payment, the total for %s is PHP %,.2f, below the monthly rent of PHP %,.2f "
                    + "(balance PHP %,.2f).\nMark it as Partial instead?",
                    p.getBillingMonth(), total, rent, rent.subtract(total));
        } else if (!markedPaid && total.compareTo(rent) >= 0) {
            suggestion = Constants.PAY_PAID;
            msg = String.format("With this payment, the total for %s is PHP %,.2f, which covers the monthly rent of PHP %,.2f.\n"
                    + "Mark it as Paid instead?", p.getBillingMonth(), total, rent);
        }
        if (msg == null) return true;                  

        int choice = JOptionPane.showConfirmDialog(this, msg, "Check Status",
                JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (choice == JOptionPane.CANCEL_OPTION || choice == JOptionPane.CLOSED_OPTION) return false;
        if (choice == JOptionPane.YES_OPTION) p.setStatus(suggestion);   
        return true;                                                    
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
        txtBillingMonth = new javax.swing.JTextField();
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

        txtBillingMonth.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        txtBillingMonth.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtBillingMonthKeyReleased(evt);
            }
        });

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
                            .addComponent(txtBillingMonth)
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
                    .addComponent(lblBalance))
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
                                    .addComponent(txtBillingMonth, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
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
                        .addComponent(lblBalance))
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(22, Short.MAX_VALUE))
        );

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));

        lblSearch.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblSearch.setForeground(new java.awt.Color(60, 40, 55));
        lblSearch.setText("Search (tenant name, unit no. or billing month):");

        txtSearch.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N

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
        fillAmountFromSelectedContract();
        refreshBalance();
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

            txtBillingMonth.setText(p.getBillingMonth());
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
            // duplicate month payment warning
            if (dao.existsPaid(p.getContractId(), p.getBillingMonth())) {
                int ok = JOptionPane.showConfirmDialog(this,
                        "A payment for this month already exists. Save anyway?",
                        "Duplicate Month", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (ok != JOptionPane.YES_OPTION) return;
            }
            
            if (!confirmStatusMatchesAmount(p, 0)) return; 
            
            dao.add(p);
            JOptionPane.showMessageDialog(this, "Payment saved successfully.");
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
            
            if (!confirmStatusMatchesAmount(p, p.getPaymentId())) return;  
            
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
            refreshBalance();
        }
    }//GEN-LAST:event_btnRefreshActionPerformed

    private void txtBillingMonthKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtBillingMonthKeyReleased
        refreshBalance();
        if (txtPaymentId.getText().isEmpty()) { 
            fillAmountFromSelectedContract();
}
    }//GEN-LAST:event_txtBillingMonthKeyReleased

    private void btnBackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBackActionPerformed
        dispose();      // closes this window; the Dashboard comes back
    }//GEN-LAST:event_btnBackActionPerformed

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
    private javax.swing.JPanel pnlForm;
    private javax.swing.JScrollPane scrlPayments;
    private javax.swing.JTable tblPayments;
    private javax.swing.JTextField txtAmount;
    private javax.swing.JTextField txtBillingMonth;
    private javax.swing.JTextField txtPaymentDate;
    private javax.swing.JTextField txtPaymentId;
    private javax.swing.JTextField txtRemarks;
    private javax.swing.JTextField txtSearch;
    // End of variables declaration//GEN-END:variables
}
