/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package ui;
import dao.ContractDAO;
import dao.TenantDAO;
import dao.UnitDAO;
import java.sql.SQLException;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import model.ComboItem;
import model.Contract;
import model.Tenant;
import model.Unit;
import util.ErrorHelper;
import util.Session;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import util.Constants;
import util.Validator;

import java.time.YearMonth;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import java.time.temporal.ChronoUnit;
/**
 *
 * @author diana
 */
public class ContractFrame extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(ContractFrame.class.getName());

    private final ContractDAO dao = new ContractDAO();
    private final TenantDAO tenantDao = new TenantDAO();
    private final UnitDAO unitDao = new UnitDAO();
    private boolean loading = false; 
    private static final int DEPOSIT_MONTHS = 2;

    public ContractFrame() {
        initComponents();
        getContentPane().setBackground(new Color(249, 241, 246));

        PillButtonUI.apply(btnSave, Theme.PRIMARY, Color.WHITE);
        PillButtonUI.apply(btnUpdate, Theme.PRIMARY_DARK, Color.WHITE);
        PillButtonUI.apply(btnEnd, Theme.SIDEBAR, Color.WHITE);
        PillButtonUI.apply(btnDelete, Theme.DANGER, Color.WHITE);
        PillButtonUI.apply(btnClear, new Color(160, 160, 160), Color.WHITE);
        PillButtonUI.apply(btnSearch, Theme.PRIMARY, Color.WHITE);
        PillButtonUI.apply(btnRefresh, Theme.PRIMARY_DARK, Color.WHITE);
        PillButtonUI.apply(btnBack, Theme.SIDEBAR, Color.WHITE);
        TableStyle.apply(tblContracts);
        tblContracts.getColumnModel().getColumn(0).setMaxWidth(60);

        installRowColors();
        setLocationRelativeTo(null);
        txtContractId.setEditable(false);         
        btnDelete.setEnabled(Session.isAdmin());  
        loadTenants();
        loadAvailableUnits();
        refreshTable();
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
private static void addItem(JComboBox combo, ComboItem item) {
    combo.addItem(item);
}

private void loadTenants() {
    loading = true;
    try {
        cmbTenant.removeAllItems();
        List<Tenant> tenants = tenantDao.getAll();
        for (Tenant t : tenants) {
            addItem(cmbTenant, new ComboItem(t.getTenantId(), t.getFirstName() + " " + t.getLastName()));
        }
        if (tenants.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No tenants yet. Add a tenant first.");
        }
    } catch (SQLException ex) {
        ErrorHelper.show(this, ex);
    } finally {
        loading = false;
    }
}

private void loadAvailableUnits() {
    loading = true;
    try {
        cmbUnit.removeAllItems();
        List<Unit> units = unitDao.getAvailableUnits();
        for (Unit u : units) {
            addItem(cmbUnit, new ComboItem(u.getUnitId(), u.getUnitNo()));
        }
        if (units.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No available units. Add a unit or end a contract first.");
        }
    } catch (SQLException ex) {
        ErrorHelper.show(this, ex);
    } finally {
        loading = false;
    }
    fillRentFromSelectedUnit();
}

//copies the selected unit's monthly rent 
private void fillRentFromSelectedUnit() {
    Object sel = cmbUnit.getSelectedItem();
    if (!(sel instanceof ComboItem)) {
        txtRent.setText("");
        txtDeposit.setText("");
        return;
    }
    try {
        Unit u = unitDao.getById(((ComboItem) sel).getId());
        if (u != null) {
            txtRent.setText(u.getMonthlyRent().toPlainString());
            fillDepositFromRent();
        }
    } catch (SQLException ex) {
        ErrorHelper.show(this, ex);
    }
}

private void fillDepositFromRent() {
    try {
        BigDecimal rent = Validator.parsePositive(txtRent.getText());
        txtDeposit.setText(rent.multiply(BigDecimal.valueOf(DEPOSIT_MONTHS)).toPlainString());
    } catch (NumberFormatException ex) {
        txtDeposit.setText("");   
    }
}

private void updateLength() {
    try {
        LocalDate s = Validator.parseDate(txtStartDate.getText());
        LocalDate e = Validator.parseDate(txtEndDate.getText());
        long months = ChronoUnit.MONTHS.between(s, e.plusDays(1)); 
        if (months < 1) {
            lblLength.setText("Contract length: less than 1 month");
            return;
        }
        BigDecimal rent = Validator.parsePositive(txtRent.getText());
        lblLength.setText(String.format("Contract length: %d months | Total rent: PHP %,.2f",
                months, rent.multiply(BigDecimal.valueOf(months))));
    } catch (RuntimeException ex) {   // dates or rent not typed yet (or invalid)
        lblLength.setText("Contract length: -");
    }
}

private void refreshTable() {
    try {
        fillTable(dao.getAll());
    } catch (SQLException ex) {
        ErrorHelper.show(this, ex);
    }
}

private void fillTable(List<Contract> list) {
    DefaultTableModel m = (DefaultTableModel) tblContracts.getModel();
    m.setRowCount(0);
    for (Contract k : list) {
        m.addRow(new Object[]{
            k.getContractId(),
            k.getTenantName(),
            k.getUnitNo(),
            k.getStartDate(),
            k.getEndDate(),
            k.getMonthlyRent(),
            k.getDeposit(),
            k.getStatus()
        });
    }
    lblInfo.setText(list.isEmpty() ? "No records found." : list.size() + " contract(s)");
}

private void installRowColors() {
    tblContracts.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (!isSelected) {
                Object status = table.getValueAt(row, 7);   // Status column
                Object end = table.getValueAt(row, 4);      // End Date column
                boolean expired = Constants.CONTRACT_ACTIVE.equals(status)
                        && end instanceof LocalDate
                        && ((LocalDate) end).isBefore(LocalDate.now());
                c.setBackground(expired ? new Color(255, 224, 224) : Color.WHITE);
                c.setForeground(expired ? new Color(150, 30, 30) : Color.BLACK);
            }
            return c;
        }
    });
}

private void selectById(JComboBox combo, int id) {
    for (int i = 0; i < combo.getItemCount(); i++) {
        Object o = combo.getItemAt(i);
        if (o instanceof ComboItem && ((ComboItem) o).getId() == id) {
            combo.setSelectedIndex(i);
            return;
        }
    }
}

private void clearForm() {
    txtContractId.setText("");
    txtStartDate.setText("");
    txtEndDate.setText("");
    cmbStatus.setSelectedItem("Active");
    cmbTenant.setEnabled(true);
    cmbUnit.setEnabled(true);
    txtStartDate.setEditable(true);
    tblContracts.clearSelection();
    loadAvailableUnits();
    
    updateLength();
}

private void warn(String message) {
    JOptionPane.showMessageDialog(this, message, "Validation", JOptionPane.WARNING_MESSAGE);
}

private void showDbError(SQLException ex) {
    String msg = ex.getMessage();
    if (ex.getSQLState() == null && msg != null && msg.startsWith("This ")) {
        JOptionPane.showMessageDialog(this, msg, "Cannot Continue", JOptionPane.WARNING_MESSAGE);
        loadAvailableUnits(); 
    } else {
        ErrorHelper.show(this, ex);
    }
}

// validates the form
private Contract readForm() {
    Object t = cmbTenant.getSelectedItem();
    if (!(t instanceof ComboItem)) {
        warn("Select a tenant.");
        return null;
    }
    Object u = cmbUnit.getSelectedItem();
    if (!(u instanceof ComboItem)) {
        warn("Select a unit. If the list is empty, add a unit or end a contract first.");
        return null;
    }

    LocalDate start, end;
    try {
        start = Validator.parseDate(txtStartDate.getText());
        end = Validator.parseDate(txtEndDate.getText());
    } catch (DateTimeParseException ex) {
        warn("Dates must be valid and use yyyy-MM-dd, for example 2026-10-07.");
        return null;
    }
    if (!end.isAfter(start)) {
        warn("End date must be after the start date.");
        txtEndDate.requestFocus();
        return null;
    }
    
    if (ChronoUnit.MONTHS.between(start, end.plusDays(1)) < 1) {
        warn("A contract must run for at least 1 month.");
        txtEndDate.requestFocus();
        return null;
    }

    BigDecimal rent;
    try {
        rent = Validator.parsePositive(txtRent.getText());
    } catch (NumberFormatException ex) {
        warn("Monthly rent must be a number greater than 0.");
        txtRent.requestFocus();
        return null;
    }

    BigDecimal deposit;
    try {
        deposit = new BigDecimal(txtDeposit.getText().trim());
        if (deposit.compareTo(BigDecimal.ZERO) < 0) throw new NumberFormatException();
    } catch (NumberFormatException ex) {
        warn("Deposit must be a number of 0 or more.");
        txtDeposit.requestFocus();
        return null;
    }

    Contract k = new Contract();
    k.setTenantId(((ComboItem) t).getId());
    k.setUnitId(((ComboItem) u).getId());
    k.setStartDate(start);
    k.setEndDate(end);
    k.setMonthlyRent(rent);
    k.setDeposit(deposit);
    k.setStatus(Constants.CONTRACT_ACTIVE);
    return k;
}

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblTitle = new javax.swing.JLabel();
        pnlForm = new javax.swing.JPanel();
        lblContractId = new javax.swing.JLabel();
        lblTenant = new javax.swing.JLabel();
        lblUnit = new javax.swing.JLabel();
        lblStatus = new javax.swing.JLabel();
        lblStart = new javax.swing.JLabel();
        lblEnd = new javax.swing.JLabel();
        lblRent = new javax.swing.JLabel();
        lblDeposit = new javax.swing.JLabel();
        txtContractId = new javax.swing.JTextField();
        cmbTenant = new javax.swing.JComboBox<>();
        cmbUnit = new javax.swing.JComboBox<>();
        cmbStatus = new javax.swing.JComboBox<>();
        txtStartDate = new javax.swing.JTextField();
        txtEndDate = new javax.swing.JTextField();
        txtRent = new javax.swing.JTextField();
        txtDeposit = new javax.swing.JTextField();
        pnlButtons = new javax.swing.JPanel();
        btnSave = new javax.swing.JButton();
        btnUpdate = new javax.swing.JButton();
        btnEnd = new javax.swing.JButton();
        btnDelete = new javax.swing.JButton();
        btnClear = new javax.swing.JButton();
        lblDeposit1 = new javax.swing.JLabel();
        lblLength = new javax.swing.JLabel();
        lblEnd1 = new javax.swing.JLabel();
        lblStart1 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        lblSearch = new javax.swing.JLabel();
        txtSearch = new javax.swing.JTextField();
        lblFilter = new javax.swing.JLabel();
        cmbFilterStatus = new javax.swing.JComboBox<>();
        btnRefresh = new javax.swing.JButton();
        btnSearch = new javax.swing.JButton();
        btnBack = new javax.swing.JButton();
        scrlContracts = new javax.swing.JScrollPane();
        tblContracts = new javax.swing.JTable();
        lblLegend = new javax.swing.JLabel();
        lblInfo = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Contract Management");
        setPreferredSize(new java.awt.Dimension(1020, 720));

        lblTitle.setFont(new java.awt.Font("Segoe UI", 1, 26)); // NOI18N
        lblTitle.setForeground(new java.awt.Color(140, 40, 100));
        lblTitle.setText("CONTRACT MANAGEMENT");

        pnlForm.setBackground(new java.awt.Color(255, 255, 255));
        pnlForm.setToolTipText("");
        pnlForm.setName(""); // NOI18N

        lblContractId.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblContractId.setForeground(new java.awt.Color(60, 40, 55));
        lblContractId.setText("Contract ID");

        lblTenant.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblTenant.setForeground(new java.awt.Color(60, 40, 55));
        lblTenant.setText("Tenant");

        lblUnit.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblUnit.setForeground(new java.awt.Color(60, 40, 55));
        lblUnit.setText("Unit");

        lblStatus.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblStatus.setForeground(new java.awt.Color(60, 40, 55));
        lblStatus.setText("Status");

        lblStart.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblStart.setForeground(new java.awt.Color(60, 40, 55));
        lblStart.setText("Start Date");

        lblEnd.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblEnd.setForeground(new java.awt.Color(60, 40, 55));
        lblEnd.setText("End Date");

        lblRent.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblRent.setForeground(new java.awt.Color(60, 40, 55));
        lblRent.setText("Monthly Rent");

        lblDeposit.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblDeposit.setForeground(new java.awt.Color(60, 40, 55));
        lblDeposit.setText("Deposit");

        txtContractId.setEditable(false);
        txtContractId.setBackground(new java.awt.Color(243, 238, 241));
        txtContractId.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        txtContractId.addActionListener(this::txtContractIdActionPerformed);

        cmbTenant.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        cmbTenant.addActionListener(this::cmbTenantActionPerformed);

        cmbUnit.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        cmbUnit.addActionListener(this::cmbUnitActionPerformed);

        cmbStatus.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        cmbStatus.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Active", "Ended", "Terminated" }));
        cmbStatus.setEnabled(false);

        txtStartDate.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        txtStartDate.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtStartDateKeyReleased(evt);
            }
        });

        txtEndDate.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        txtEndDate.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtEndDateKeyReleased(evt);
            }
        });

        txtRent.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        txtRent.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtRentKeyReleased(evt);
            }
        });

        txtDeposit.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N

        pnlButtons.setBackground(new java.awt.Color(255, 255, 255));

        btnSave.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnSave.setText("SAVE");
        btnSave.addActionListener(this::btnSaveActionPerformed);

        btnUpdate.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnUpdate.setText("UPDATE");
        btnUpdate.addActionListener(this::btnUpdateActionPerformed);

        btnEnd.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnEnd.setText("END CONTRACT");
        btnEnd.addActionListener(this::btnEndActionPerformed);

        btnDelete.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnDelete.setText("DELETE");
        btnDelete.addActionListener(this::btnDeleteActionPerformed);

        btnClear.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnClear.setText("CLEAR");
        btnClear.addActionListener(this::btnClearActionPerformed);

        javax.swing.GroupLayout pnlButtonsLayout = new javax.swing.GroupLayout(pnlButtons);
        pnlButtons.setLayout(pnlButtonsLayout);
        pnlButtonsLayout.setHorizontalGroup(
            pnlButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(btnSave, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(btnUpdate, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(btnEnd, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(btnDelete, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(btnClear, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        pnlButtonsLayout.setVerticalGroup(
            pnlButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlButtonsLayout.createSequentialGroup()
                .addComponent(btnSave, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(btnUpdate, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(btnEnd, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(btnDelete, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(btnClear, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        lblDeposit1.setFont(new java.awt.Font("Segoe UI", 2, 10)); // NOI18N
        lblDeposit1.setForeground(new java.awt.Color(130, 110, 125));
        lblDeposit1.setText("(2 months rent)");

        lblLength.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblLength.setForeground(new java.awt.Color(60, 40, 55));
        lblLength.setText("Contract length: -");

        lblEnd1.setFont(new java.awt.Font("Segoe UI", 2, 10)); // NOI18N
        lblEnd1.setForeground(new java.awt.Color(130, 110, 125));
        lblEnd1.setText("YYYY-mm-dd");

        lblStart1.setFont(new java.awt.Font("Segoe UI", 2, 10)); // NOI18N
        lblStart1.setForeground(new java.awt.Color(130, 110, 125));
        lblStart1.setText("YYYY-mm-dd");

        javax.swing.GroupLayout pnlFormLayout = new javax.swing.GroupLayout(pnlForm);
        pnlForm.setLayout(pnlFormLayout);
        pnlFormLayout.setHorizontalGroup(
            pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlFormLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblContractId)
                    .addComponent(lblTenant)
                    .addComponent(lblUnit)
                    .addComponent(lblStatus))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtContractId)
                    .addComponent(cmbTenant, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(cmbUnit, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(cmbStatus, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(28, 28, 28)
                .addGroup(pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlFormLayout.createSequentialGroup()
                        .addGroup(pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblStart)
                            .addComponent(lblStart1)
                            .addComponent(lblEnd)
                            .addComponent(lblEnd1)
                            .addComponent(lblRent)
                            .addComponent(lblDeposit)
                            .addComponent(lblDeposit1))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtStartDate)
                            .addComponent(txtEndDate)
                            .addComponent(txtRent)
                            .addComponent(txtDeposit)))
                    .addComponent(lblLength))
                .addGap(28, 28, 28)
                .addComponent(pnlButtons, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16))
        );
        pnlFormLayout.setVerticalGroup(
            pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlFormLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlFormLayout.createSequentialGroup()
                        .addGroup(pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblContractId)
                            .addComponent(txtContractId, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(10, 10, 10)
                        .addGroup(pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblTenant)
                            .addComponent(cmbTenant, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(10, 10, 10)
                        .addGroup(pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblUnit)
                            .addComponent(cmbUnit, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(10, 10, 10)
                        .addGroup(pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblStatus)
                            .addComponent(cmbStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(pnlFormLayout.createSequentialGroup()
                        .addGroup(pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(pnlFormLayout.createSequentialGroup()
                                .addGap(5, 5, 5)
                                .addComponent(lblStart)
                                .addGap(0, 0, 0)
                                .addComponent(lblStart1))
                            .addComponent(txtStartDate, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(10, 10, 10)
                        .addGroup(pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(pnlFormLayout.createSequentialGroup()
                                .addGap(5, 5, 5)
                                .addComponent(lblEnd)
                                .addGap(0, 0, 0)
                                .addComponent(lblEnd1))
                            .addComponent(txtEndDate, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(10, 10, 10)
                        .addGroup(pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblRent)
                            .addComponent(txtRent, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(10, 10, 10)
                        .addGroup(pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(pnlFormLayout.createSequentialGroup()
                                .addGap(5, 5, 5)
                                .addComponent(lblDeposit)
                                .addGap(0, 0, 0)
                                .addComponent(lblDeposit1))
                            .addComponent(txtDeposit, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(10, 10, 10)
                        .addComponent(lblLength))
                    .addComponent(pnlButtons, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(21, Short.MAX_VALUE))
        );

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        lblSearch.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblSearch.setForeground(new java.awt.Color(60, 40, 55));
        lblSearch.setText("Search: (name, unit # or status):");

        txtSearch.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        txtSearch.addActionListener(this::txtSearchActionPerformed);

        lblFilter.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblFilter.setForeground(new java.awt.Color(60, 40, 55));
        lblFilter.setText("Status:");

        cmbFilterStatus.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        cmbFilterStatus.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "All", "Active", "Ended", "Terminated" }));
        cmbFilterStatus.addActionListener(this::cmbFilterStatusActionPerformed);

        btnRefresh.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnRefresh.setText("REFRESH");
        btnRefresh.addActionListener(this::btnRefreshActionPerformed);

        btnSearch.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnSearch.setText("SEARCH");
        btnSearch.addActionListener(this::btnSearchActionPerformed);

        btnBack.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnBack.setText("Back to Dashboard");
        btnBack.addActionListener(this::btnBackActionPerformed);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(lblSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 199, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 167, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblFilter)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cmbFilterStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnRefresh, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnBack, javax.swing.GroupLayout.PREFERRED_SIZE, 164, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(144, 144, 144))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblSearch)
                    .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblFilter)
                    .addComponent(cmbFilterStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnRefresh, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnBack, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(10, 10, 10))
        );

        tblContracts.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "Tenant", "Unit", "Start Date", "End Date", "Monthly Rent", "Deposit", "Status"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblContracts.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tblContracts.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblContractsMouseClicked(evt);
            }
        });
        scrlContracts.setViewportView(tblContracts);

        lblLegend.setFont(new java.awt.Font("Segoe UI", 3, 12)); // NOI18N
        lblLegend.setForeground(new java.awt.Color(130, 110, 125));
        lblLegend.setText("Red rows: Active contracts past their end date. Click END CONTRACT to close them.");

        lblInfo.setForeground(new java.awt.Color(130, 110, 125));
        lblInfo.setText(" ");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(lblLegend, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(lblInfo, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 1055, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblTitle, javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(pnlForm, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 971, Short.MAX_VALUE)
                                .addComponent(scrlContracts, javax.swing.GroupLayout.Alignment.LEADING)))
                        .addGap(0, 0, Short.MAX_VALUE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(lblTitle)
                .addGap(34, 34, 34)
                .addComponent(pnlForm, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(scrlContracts, javax.swing.GroupLayout.PREFERRED_SIZE, 212, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblInfo)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblLegend)
                .addContainerGap(14, Short.MAX_VALUE))
        );

        pnlForm.getAccessibleContext().setAccessibleName("");

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void tblContractsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblContractsMouseClicked
        int r = tblContracts.getSelectedRow();
        if (r < 0) return;

        try {
            int id = Integer.parseInt(tblContracts.getValueAt(r, 0).toString());
            Contract k = dao.getById(id);
            if (k == null) {
                JOptionPane.showMessageDialog(this, "This contract no longer exists. Click REFRESH.");
                return;
            }

            txtContractId.setText(String.valueOf(k.getContractId()));
            selectById(cmbTenant, k.getTenantId());

            loading = true;
            cmbUnit.removeAllItems();
            addItem(cmbUnit, new ComboItem(k.getUnitId(), k.getUnitNo()));
            loading = false;

            txtStartDate.setText(k.getStartDate().toString());
            txtEndDate.setText(k.getEndDate().toString());
            txtRent.setText(k.getMonthlyRent().toPlainString());
            txtDeposit.setText(k.getDeposit().toPlainString());
            cmbStatus.setSelectedItem(k.getStatus());

            cmbTenant.setEnabled(false);
            cmbUnit.setEnabled(false);
            txtStartDate.setEditable(false);

            updateLength();
        } catch (SQLException ex) {
            ErrorHelper.show(this, ex);
        }
    }//GEN-LAST:event_tblContractsMouseClicked

    private void btnSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSearchActionPerformed
        try {
            List<Contract> list = dao.search(txtSearch.getText(),
                cmbFilterStatus.getSelectedItem().toString());
            fillTable(list);
            if (list.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No records found.");
            }
        } catch (SQLException ex) {
            ErrorHelper.show(this, ex);
        }
    }//GEN-LAST:event_btnSearchActionPerformed

    private void btnRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshActionPerformed
        txtSearch.setText("");
        cmbFilterStatus.setSelectedIndex(0);
        refreshTable();

        if (txtContractId.getText().isEmpty()) {   // only in "new contract" mode
            Object t = cmbTenant.getSelectedItem();
            Object u = cmbUnit.getSelectedItem();
            loadTenants();
            loadAvailableUnits();
            if (t instanceof ComboItem) selectById(cmbTenant, ((ComboItem) t).getId());
            if (u instanceof ComboItem) selectById(cmbUnit, ((ComboItem) u).getId());
        }
    }//GEN-LAST:event_btnRefreshActionPerformed

    private void cmbFilterStatusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbFilterStatusActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cmbFilterStatusActionPerformed

    private void txtSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtSearchActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtSearchActionPerformed

    private void btnClearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClearActionPerformed
        clearForm();
    }//GEN-LAST:event_btnClearActionPerformed

    private void btnDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteActionPerformed
        if (txtContractId.getText().isEmpty()) {
            warn("Select a contract from the table first.");
            return;
        }
        int id = Integer.parseInt(txtContractId.getText());

        int ok = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete this record?",
            "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;

        try {
            if (dao.countPayments(id) > 0) {
                JOptionPane.showMessageDialog(this,
                    "This contract has payments and cannot be deleted. End the contract instead.",
                    "Cannot Delete", JOptionPane.WARNING_MESSAGE);
                return;
            }
            dao.delete(id);
            JOptionPane.showMessageDialog(this, "Contract deleted.");
            clearForm();
            refreshTable();
        } catch (SQLException ex) {
            showDbError(ex);
        }
    }//GEN-LAST:event_btnDeleteActionPerformed

    private void btnEndActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEndActionPerformed
        if (txtContractId.getText().isEmpty()) {
            warn("Select a contract from the table first.");
            return;
        }
        if (!Constants.CONTRACT_ACTIVE.equals(cmbStatus.getSelectedItem().toString())) {
            warn("This contract is already closed.");
            return;
        }

        Object[] options = {"Ended", "Terminated", "Cancel"};
        int choice = JOptionPane.showOptionDialog(this,
            "Ended = the lease finished normally.\nTerminated = the lease was cut short.\n\nHow should this contract be closed?",
            "End Contract", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
            null, options, options[0]);
        if (choice != 0 && choice != 1) return; // cancel or the dialog was closed

        String newStatus = (choice == 0) ? Constants.CONTRACT_ENDED : Constants.CONTRACT_TERMINATED;
        try {
            dao.endContract(Integer.parseInt(txtContractId.getText()), newStatus); // contract closed + unit now avail
            JOptionPane.showMessageDialog(this, "Contract " + newStatus.toLowerCase() + ". The unit is available again.");
            clearForm();
            refreshTable();
        } catch (SQLException ex) {
            showDbError(ex);
        }
    }//GEN-LAST:event_btnEndActionPerformed

    private void btnUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpdateActionPerformed
        if (txtContractId.getText().isEmpty()) {
            warn("Select a contract from the table first.");
            return;
        }
        if (!Constants.CONTRACT_ACTIVE.equals(cmbStatus.getSelectedItem().toString())) {
            warn("Only Active contracts can be updated.");
            return;
        }
        Contract k = readForm();
        if (k == null) return;
        k.setContractId(Integer.parseInt(txtContractId.getText()));

        try {
            if (dao.updateContract(k)) {
                JOptionPane.showMessageDialog(this, "Contract updated successfully.");
                clearForm();
                refreshTable();
            } else {
                warn("Nothing was updated. The contract may no longer be Active. Click REFRESH.");
            }
        } catch (SQLException ex) {
            showDbError(ex);
        }
    }//GEN-LAST:event_btnUpdateActionPerformed

    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveActionPerformed
        if (!txtContractId.getText().isEmpty()) {
            warn("A contract is selected. Click CLEAR first to create a new contract.");
            return;
        }
        Contract k = readForm();
        if (k == null) return; // validation failed

        BigDecimal required = k.getMonthlyRent().multiply(BigDecimal.valueOf(DEPOSIT_MONTHS));
        if (k.getDeposit().compareTo(required) != 0) {
            warn(String.format("Policy: the security deposit must be %d months of rent (PHP %,.2f).",
                DEPOSIT_MONTHS, required));
        txtDeposit.requestFocus();
        return;
        }

        try {
            dao.createContract(k); // insert contract + unit becomes occpued, in one transaction
            JOptionPane.showMessageDialog(this, String.format(
                "Contract created successfully.\n\n"
                + "Move-in amounts to collect:\n"
                + "   Security deposit (%d months): PHP %,.2f\n"
                + "   Advance rent (1 month, billing month %s): PHP %,.2f\n\n"
                + "Record the advance in the Payments screen.",
                DEPOSIT_MONTHS, k.getDeposit(),
                YearMonth.from(k.getStartDate()), k.getMonthlyRent()));

        clearForm();
        refreshTable();
        } catch (SQLException ex) {
            showDbError(ex);
        }
    }//GEN-LAST:event_btnSaveActionPerformed

    private void txtRentKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtRentKeyReleased
        if (txtContractId.getText().isEmpty()) {
            fillDepositFromRent();
        }
        updateLength();
    }//GEN-LAST:event_txtRentKeyReleased

    private void txtEndDateKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtEndDateKeyReleased
        updateLength();
    }//GEN-LAST:event_txtEndDateKeyReleased

    private void txtStartDateKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtStartDateKeyReleased
        updateLength();
    }//GEN-LAST:event_txtStartDateKeyReleased

    private void cmbUnitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbUnitActionPerformed
        if (loading || !cmbUnit.isEnabled()) return;
        fillRentFromSelectedUnit();
    }//GEN-LAST:event_cmbUnitActionPerformed

    private void cmbTenantActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbTenantActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cmbTenantActionPerformed

    private void txtContractIdActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtContractIdActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtContractIdActionPerformed

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
        java.awt.EventQueue.invokeLater(() -> new ContractFrame().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBack;
    private javax.swing.JButton btnClear;
    private javax.swing.JButton btnDelete;
    private javax.swing.JButton btnEnd;
    private javax.swing.JButton btnRefresh;
    private javax.swing.JButton btnSave;
    private javax.swing.JButton btnSearch;
    private javax.swing.JButton btnUpdate;
    private javax.swing.JComboBox<String> cmbFilterStatus;
    private javax.swing.JComboBox<String> cmbStatus;
    private javax.swing.JComboBox<String> cmbTenant;
    private javax.swing.JComboBox<String> cmbUnit;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel lblContractId;
    private javax.swing.JLabel lblDeposit;
    private javax.swing.JLabel lblDeposit1;
    private javax.swing.JLabel lblEnd;
    private javax.swing.JLabel lblEnd1;
    private javax.swing.JLabel lblFilter;
    private javax.swing.JLabel lblInfo;
    private javax.swing.JLabel lblLegend;
    private javax.swing.JLabel lblLength;
    private javax.swing.JLabel lblRent;
    private javax.swing.JLabel lblSearch;
    private javax.swing.JLabel lblStart;
    private javax.swing.JLabel lblStart1;
    private javax.swing.JLabel lblStatus;
    private javax.swing.JLabel lblTenant;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JLabel lblUnit;
    private javax.swing.JPanel pnlButtons;
    private javax.swing.JPanel pnlForm;
    private javax.swing.JScrollPane scrlContracts;
    private javax.swing.JTable tblContracts;
    private javax.swing.JTextField txtContractId;
    private javax.swing.JTextField txtDeposit;
    private javax.swing.JTextField txtEndDate;
    private javax.swing.JTextField txtRent;
    private javax.swing.JTextField txtSearch;
    private javax.swing.JTextField txtStartDate;
    // End of variables declaration//GEN-END:variables
}
