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

    public ContractFrame() {
        initComponents();
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
        return;
    }
    try {
        Unit u = unitDao.getById(((ComboItem) sel).getId());
        if (u != null) txtRent.setText(u.getMonthlyRent().toPlainString());
    } catch (SQLException ex) {
        ErrorHelper.show(this, ex);
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
    txtDeposit.setText("0");
    cmbStatus.setSelectedItem("Active");
    cmbTenant.setEnabled(true);
    cmbUnit.setEnabled(true);
    txtStartDate.setEditable(true);
    tblContracts.clearSelection();
    loadAvailableUnits();
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
        jPanel1 = new javax.swing.JPanel();
        lblSearch = new javax.swing.JLabel();
        txtSearch = new javax.swing.JTextField();
        lblFilter = new javax.swing.JLabel();
        cmbFilterStatus = new javax.swing.JComboBox<>();
        btnRefresh = new javax.swing.JButton();
        btnSearch = new javax.swing.JButton();
        scrlContracts = new javax.swing.JScrollPane();
        tblContracts = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Contract Management");
        setSize(new java.awt.Dimension(1000, 650));

        lblTitle.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        lblTitle.setText("CONTRACT MANAGEMENT");

        pnlForm.setBorder(javax.swing.BorderFactory.createTitledBorder("Contract Details"));
        pnlForm.setToolTipText("");
        pnlForm.setName(""); // NOI18N

        lblContractId.setText("Contract ID");

        lblTenant.setText("Tenant");

        lblUnit.setText("Unit");

        lblStatus.setText("Status");

        lblStart.setText("Start Date");

        lblEnd.setText("End Date");

        lblRent.setText("Monthly Rent");

        lblDeposit.setText("Deposit");

        txtContractId.setEditable(false);
        txtContractId.addActionListener(this::txtContractIdActionPerformed);

        cmbTenant.addActionListener(this::cmbTenantActionPerformed);

        cmbUnit.addActionListener(this::cmbUnitActionPerformed);

        cmbStatus.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Active", "Ended", "Terminated" }));
        cmbStatus.setEnabled(false);

        btnSave.setText("SAVE");
        btnSave.addActionListener(this::btnSaveActionPerformed);

        btnUpdate.setText("UPDATE");
        btnUpdate.addActionListener(this::btnUpdateActionPerformed);

        btnEnd.setText("END CONTRACT");
        btnEnd.addActionListener(this::btnEndActionPerformed);

        btnDelete.setText("DELETE");
        btnDelete.addActionListener(this::btnDeleteActionPerformed);

        btnClear.setText("CLEAR");
        btnClear.addActionListener(this::btnClearActionPerformed);

        javax.swing.GroupLayout pnlButtonsLayout = new javax.swing.GroupLayout(pnlButtons);
        pnlButtons.setLayout(pnlButtonsLayout);
        pnlButtonsLayout.setHorizontalGroup(
            pnlButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlButtonsLayout.createSequentialGroup()
                .addGap(85, 85, 85)
                .addComponent(btnSave)
                .addGap(28, 28, 28)
                .addComponent(btnUpdate)
                .addGap(27, 27, 27)
                .addComponent(btnEnd)
                .addGap(31, 31, 31)
                .addComponent(btnDelete)
                .addGap(28, 28, 28)
                .addComponent(btnClear)
                .addContainerGap(78, Short.MAX_VALUE))
        );
        pnlButtonsLayout.setVerticalGroup(
            pnlButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlButtonsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnSave)
                    .addComponent(btnUpdate)
                    .addComponent(btnEnd)
                    .addComponent(btnDelete)
                    .addComponent(btnClear))
                .addContainerGap(16, Short.MAX_VALUE))
        );

        lblSearch.setText("Search:");

        txtSearch.addActionListener(this::txtSearchActionPerformed);

        lblFilter.setText("Status:");

        cmbFilterStatus.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "All", "Active", "Ended", "Terminated" }));
        cmbFilterStatus.addActionListener(this::cmbFilterStatusActionPerformed);

        btnRefresh.setText("REFRESH");
        btnRefresh.addActionListener(this::btnRefreshActionPerformed);

        btnSearch.setText("SEARCH");
        btnSearch.addActionListener(this::btnSearchActionPerformed);

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

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(scrlContracts, javax.swing.GroupLayout.PREFERRED_SIZE, 747, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(lblSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 243, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(27, 27, 27)
                        .addComponent(lblFilter, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cmbFilterStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(36, 36, 36)
                        .addComponent(btnSearch)
                        .addGap(18, 18, 18)
                        .addComponent(btnRefresh)))
                .addContainerGap(29, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(11, 11, 11)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblSearch)
                    .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblFilter)
                    .addComponent(cmbFilterStatus, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSearch)
                    .addComponent(btnRefresh))
                .addGap(18, 18, 18)
                .addComponent(scrlContracts, javax.swing.GroupLayout.PREFERRED_SIZE, 194, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout pnlFormLayout = new javax.swing.GroupLayout(pnlForm);
        pnlForm.setLayout(pnlFormLayout);
        pnlFormLayout.setHorizontalGroup(
            pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlFormLayout.createSequentialGroup()
                .addGap(45, 45, 45)
                .addGroup(pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlFormLayout.createSequentialGroup()
                        .addGroup(pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(pnlFormLayout.createSequentialGroup()
                                .addComponent(lblStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(33, 33, 33)
                                .addComponent(cmbStatus, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(pnlFormLayout.createSequentialGroup()
                                .addGroup(pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(lblContractId, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(lblTenant, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(lblUnit, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(33, 33, 33)
                                .addGroup(pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(cmbUnit, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addGroup(pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(txtContractId)
                                        .addComponent(cmbTenant, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                        .addGap(165, 165, 165)
                        .addGroup(pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(pnlFormLayout.createSequentialGroup()
                                .addComponent(lblDeposit, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtDeposit))
                            .addGroup(pnlFormLayout.createSequentialGroup()
                                .addComponent(lblRent, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtRent))
                            .addGroup(pnlFormLayout.createSequentialGroup()
                                .addComponent(lblEnd, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtEndDate))
                            .addGroup(pnlFormLayout.createSequentialGroup()
                                .addComponent(lblStart, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtStartDate)))
                        .addGap(120, 120, 120))
                    .addGroup(pnlFormLayout.createSequentialGroup()
                        .addGroup(pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(pnlFormLayout.createSequentialGroup()
                                .addGap(21, 21, 21)
                                .addComponent(pnlButtons, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );
        pnlFormLayout.setVerticalGroup(
            pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlFormLayout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addGroup(pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlFormLayout.createSequentialGroup()
                        .addGroup(pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblStart)
                            .addComponent(txtStartDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblEnd)
                            .addComponent(txtEndDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblRent)
                            .addComponent(txtRent, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblDeposit)
                            .addComponent(txtDeposit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(pnlFormLayout.createSequentialGroup()
                        .addGroup(pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblContractId)
                            .addComponent(txtContractId, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblTenant)
                            .addComponent(cmbTenant, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblUnit)
                            .addComponent(cmbUnit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblStatus)
                            .addComponent(cmbStatus, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(27, 27, 27)
                .addComponent(pnlButtons, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(43, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(337, 337, 337)
                        .addComponent(lblTitle))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(36, 36, 36)
                        .addComponent(pnlForm, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(38, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(31, 31, 31)
                .addComponent(lblTitle)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlForm, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(239, Short.MAX_VALUE))
        );

        pnlForm.getAccessibleContext().setAccessibleName("");

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void txtContractIdActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtContractIdActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtContractIdActionPerformed

    private void cmbTenantActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbTenantActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cmbTenantActionPerformed

    private void txtSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtSearchActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtSearchActionPerformed

    private void cmbFilterStatusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbFilterStatusActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cmbFilterStatusActionPerformed

    private void cmbUnitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbUnitActionPerformed
        if (loading || !cmbUnit.isEnabled()) return;
        fillRentFromSelectedUnit();    
    }//GEN-LAST:event_cmbUnitActionPerformed

    private void btnClearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClearActionPerformed
        clearForm();
    }//GEN-LAST:event_btnClearActionPerformed

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
        } catch (SQLException ex) {
            ErrorHelper.show(this, ex);
        }
    }//GEN-LAST:event_tblContractsMouseClicked

    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveActionPerformed
        if (!txtContractId.getText().isEmpty()) {
            warn("A contract is selected. Click CLEAR first to create a new contract.");
            return;
        }
        Contract k = readForm();
        if (k == null) return; // validation failed

        try {
            dao.createContract(k); // insert contract + unit becomes occpued, in one transaction
            JOptionPane.showMessageDialog(this, "Contract created successfully.");
            clearForm();
            refreshTable();
        } catch (SQLException ex) {
            showDbError(ex);
        }
    }//GEN-LAST:event_btnSaveActionPerformed

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
    }//GEN-LAST:event_btnRefreshActionPerformed

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
    private javax.swing.JLabel lblEnd;
    private javax.swing.JLabel lblFilter;
    private javax.swing.JLabel lblRent;
    private javax.swing.JLabel lblSearch;
    private javax.swing.JLabel lblStart;
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
