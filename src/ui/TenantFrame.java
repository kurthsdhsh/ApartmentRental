package ui;

import dao.TenantDAO;
import java.awt.Color;
import java.sql.SQLException;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import model.Tenant;
import util.ErrorHelper;
import util.Session;
import util.Validator;


public class TenantFrame extends javax.swing.JFrame {

    private final TenantDAO dao = new TenantDAO();   

    public TenantFrame() {
        initComponents();
        setLocationRelativeTo(null);

        PillButtonUI.apply(btnSave, Theme.PRIMARY, Color.WHITE);
        PillButtonUI.apply(btnUpdate, Theme.PRIMARY_DARK, Color.WHITE);
        PillButtonUI.apply(btnDelete, Theme.DANGER, Color.WHITE);
        PillButtonUI.apply(btnClear, new Color(160, 160, 160), Color.WHITE);
        PillButtonUI.apply(btnSearch, Theme.PRIMARY, Color.WHITE);
        PillButtonUI.apply(btnRefresh, Theme.PRIMARY_DARK, Color.WHITE);
        PillButtonUI.apply(btnBack, Theme.SIDEBAR, Color.WHITE);
        TableStyle.apply(tblTenants);
        tblTenants.getColumnModel().getColumn(0).setMaxWidth(60);

        btnDelete.setEnabled(Session.isAdmin());     

        setupKeyboard();
        clearForm();
        loadTable("");
    }


    // ---- Keyboard: Tab or Enter moves to the next box ----
    // Save / Update are clicked manually.
    private void enterGoesTo(javax.swing.JTextField from, java.awt.Component to) {
        from.addActionListener(e -> to.requestFocusInWindow());
    }

    private void setupKeyboard() {
        txtTenantId.setFocusable(false);             // Tab skips read-only boxes
        txtCurrentUnit.setFocusable(false);
        enterGoesTo(txtFirstName, txtLastName);
        enterGoesTo(txtLastName, txtContact);
        enterGoesTo(txtContact, txtEmail);
        enterGoesTo(txtEmail, txtAddress);
        txtSearch.addActionListener(e -> btnSearch.doClick());
    }


    // Asks twice before deleting. Both answers must be Yes.
    private boolean confirmDeleteTwice(String what) {
        int first = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete " + what + "?",
                "Confirm Delete (1 of 2)", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (first != JOptionPane.YES_OPTION) return false;
        int second = JOptionPane.showConfirmDialog(this,
                "This cannot be undone. Delete " + what + " permanently?",
                "Final Confirmation (2 of 2)", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        return second == JOptionPane.YES_OPTION;
    }

    private void loadTable(String keyword) {
        try {
            List<Tenant> list = dao.search(keyword);
            DefaultTableModel m = (DefaultTableModel) tblTenants.getModel();
            m.setRowCount(0);
            for (Tenant t : list) {
                m.addRow(new Object[]{t.getTenantId(), t.getFirstName(), t.getLastName(), t.getContactNo(),
                        t.getEmail(), t.getAddress(), t.getCurrentUnit()});
            }
            lblInfo.setText(list.isEmpty() ? "No records found." : list.size() + " tenant(s)");
        } catch (SQLException ex) {
            ErrorHelper.show(this, ex);
        }
    }

    private void clearForm() {
        txtTenantId.setText("");
        txtFirstName.setText("");
        txtLastName.setText("");
        txtContact.setText("");
        txtEmail.setText("");
        txtAddress.setText("");
        txtCurrentUnit.setText("");
        tblTenants.clearSelection();
        txtFirstName.requestFocus();
    }

    private Tenant readForm() {
        String first = txtFirstName.getText().trim();
        String last = txtLastName.getText().trim();
        String contact = txtContact.getText().trim();
        String email = txtEmail.getText().trim();
        String address = txtAddress.getText().trim();

        if (Validator.isEmpty(first)) { warn("First name is required."); txtFirstName.requestFocus(); return null; }
        if (first.length() > 50) { warn("First name must be 50 characters or less."); txtFirstName.requestFocus(); return null; }
        if (Validator.isEmpty(last)) { warn("Last name is required."); txtLastName.requestFocus(); return null; }
        if (last.length() > 50) { warn("Last name must be 50 characters or less."); txtLastName.requestFocus(); return null; }
        if (Validator.isEmpty(contact)) { warn("Contact number is required."); txtContact.requestFocus(); return null; }
        contact = contact.replace(" ", "").replace("-", "");      // "0917 123-4567" -> "09171234567"
        if (!contact.matches("09\\d{9}|\\+639\\d{9}")) {
            warn("Contact number must be a Philippine mobile number:\n"
                    + "11 digits starting with 09 (example 09171234567), or +63 followed by 10 digits.");
            txtContact.requestFocus();
            return null;
        }
        if (!email.isEmpty() && (email.length() > 100 || !Validator.isEmail(email))) {
            warn("Please enter a valid email address (or leave it empty)."); txtEmail.requestFocus(); return null;
        }
        if (address.length() > 255) { warn("Address must be 255 characters or less."); txtAddress.requestFocus(); return null; }

        Tenant t = new Tenant();
        t.setFirstName(first);
        t.setLastName(last);
        t.setContactNo(contact);
        t.setEmail(email.isEmpty() ? null : email);
        t.setAddress(address.isEmpty() ? null : address);
        return t;
    }

    private String cell(int row, int col) {
        Object v = tblTenants.getValueAt(row, col);
        return v == null ? "" : v.toString();
    }

    private void warn(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Validation", JOptionPane.WARNING_MESSAGE);
    }

    private void info(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Information", JOptionPane.INFORMATION_MESSAGE);
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlRoot = new javax.swing.JPanel();
        lblTitle = new javax.swing.JLabel();
        pnlDetails = new javax.swing.JPanel();
        lblTenantId = new javax.swing.JLabel();
        txtTenantId = new javax.swing.JTextField();
        lblFirstName = new javax.swing.JLabel();
        txtFirstName = new javax.swing.JTextField();
        lblLastName = new javax.swing.JLabel();
        txtLastName = new javax.swing.JTextField();
        lblContact = new javax.swing.JLabel();
        txtContact = new javax.swing.JTextField();
        lblEmail = new javax.swing.JLabel();
        txtEmail = new javax.swing.JTextField();
        lblAddress = new javax.swing.JLabel();
        txtAddress = new javax.swing.JTextField();
        lblCurrentUnit = new javax.swing.JLabel();
        txtCurrentUnit = new javax.swing.JTextField();
        btnSave = new javax.swing.JButton();
        btnUpdate = new javax.swing.JButton();
        btnDelete = new javax.swing.JButton();
        btnClear = new javax.swing.JButton();
        pnlSearch = new javax.swing.JPanel();
        lblSearch = new javax.swing.JLabel();
        txtSearch = new javax.swing.JTextField();
        btnSearch = new javax.swing.JButton();
        btnRefresh = new javax.swing.JButton();
        btnBack = new javax.swing.JButton();
        scrlTenants = new javax.swing.JScrollPane();
        tblTenants = new javax.swing.JTable();
        lblInfo = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Tenant Management");
        setPreferredSize(new java.awt.Dimension(1020, 720));

        pnlRoot.setBackground(new java.awt.Color(249, 241, 246));

        lblTitle.setFont(new java.awt.Font("Segoe UI", 1, 26)); // NOI18N
        lblTitle.setForeground(new java.awt.Color(140, 40, 100));
        lblTitle.setText("Tenant Management");

        pnlDetails.setBackground(new java.awt.Color(255, 255, 255));

        lblTenantId.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblTenantId.setForeground(new java.awt.Color(60, 40, 55));
        lblTenantId.setText("Tenant ID");

        txtTenantId.setEditable(false);
        txtTenantId.setBackground(new java.awt.Color(243, 238, 241));
        txtTenantId.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N

        lblFirstName.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblFirstName.setForeground(new java.awt.Color(60, 40, 55));
        lblFirstName.setText("First name *");

        txtFirstName.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N

        lblLastName.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblLastName.setForeground(new java.awt.Color(60, 40, 55));
        lblLastName.setText("Last name *");

        txtLastName.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N

        lblContact.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblContact.setForeground(new java.awt.Color(60, 40, 55));
        lblContact.setText("Contact no. *");

        txtContact.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N

        lblEmail.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblEmail.setForeground(new java.awt.Color(60, 40, 55));
        lblEmail.setText("Email");

        txtEmail.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N

        lblAddress.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblAddress.setForeground(new java.awt.Color(60, 40, 55));
        lblAddress.setText("Address");

        txtAddress.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N

        lblCurrentUnit.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblCurrentUnit.setForeground(new java.awt.Color(60, 40, 55));
        lblCurrentUnit.setText("Current unit");

        txtCurrentUnit.setEditable(false);
        txtCurrentUnit.setBackground(new java.awt.Color(243, 238, 241));
        txtCurrentUnit.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N

        btnSave.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnSave.setText("Save");
        btnSave.addActionListener(this::btnSaveActionPerformed);

        btnUpdate.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnUpdate.setText("Update");
        btnUpdate.addActionListener(this::btnUpdateActionPerformed);

        btnDelete.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnDelete.setText("Delete");
        btnDelete.addActionListener(this::btnDeleteActionPerformed);

        btnClear.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnClear.setText("Clear");
        btnClear.addActionListener(this::btnClearActionPerformed);

        javax.swing.GroupLayout pnlDetailsLayout = new javax.swing.GroupLayout(pnlDetails);
        pnlDetails.setLayout(pnlDetailsLayout);
        pnlDetailsLayout.setHorizontalGroup(
            pnlDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlDetailsLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(pnlDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblTenantId)
                    .addComponent(lblFirstName)
                    .addComponent(lblLastName)
                    .addComponent(lblContact))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtTenantId)
                    .addComponent(txtFirstName)
                    .addComponent(txtLastName)
                    .addComponent(txtContact))
                .addGap(28, 28, 28)
                .addGroup(pnlDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblEmail)
                    .addComponent(lblAddress)
                    .addComponent(lblCurrentUnit))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtEmail)
                    .addComponent(txtAddress)
                    .addComponent(txtCurrentUnit))
                .addGap(28, 28, 28)
                .addGroup(pnlDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnSave, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnUpdate, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnDelete, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnClear, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(16, 16, 16))
        );
        pnlDetailsLayout.setVerticalGroup(
            pnlDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlDetailsLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(pnlDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlDetailsLayout.createSequentialGroup()
                        .addGroup(pnlDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblTenantId)
                            .addComponent(txtTenantId, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(10, 10, 10)
                        .addGroup(pnlDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblFirstName)
                            .addComponent(txtFirstName, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(10, 10, 10)
                        .addGroup(pnlDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblLastName)
                            .addComponent(txtLastName, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(10, 10, 10)
                        .addGroup(pnlDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblContact)
                            .addComponent(txtContact, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(pnlDetailsLayout.createSequentialGroup()
                        .addGroup(pnlDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblEmail)
                            .addComponent(txtEmail, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(10, 10, 10)
                        .addGroup(pnlDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblAddress)
                            .addComponent(txtAddress, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(10, 10, 10)
                        .addGroup(pnlDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblCurrentUnit)
                            .addComponent(txtCurrentUnit, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(pnlDetailsLayout.createSequentialGroup()
                        .addComponent(btnSave, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addComponent(btnUpdate, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addComponent(btnDelete, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addComponent(btnClear, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(40, 40, 40))
        );

        pnlSearch.setBackground(new java.awt.Color(255, 255, 255));

        lblSearch.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblSearch.setForeground(new java.awt.Color(60, 40, 55));
        lblSearch.setText("Search (name, contact or email):");

        txtSearch.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        txtSearch.addActionListener(this::txtSearchActionPerformed);

        btnSearch.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnSearch.setText("Search");
        btnSearch.addActionListener(this::btnSearchActionPerformed);

        btnRefresh.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnRefresh.setText("Show all");
        btnRefresh.addActionListener(this::btnRefreshActionPerformed);

        btnBack.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnBack.setText("Back to Dashboard");
        btnBack.addActionListener(this::btnBackActionPerformed);

        javax.swing.GroupLayout pnlSearchLayout = new javax.swing.GroupLayout(pnlSearch);
        pnlSearch.setLayout(pnlSearchLayout);
        pnlSearchLayout.setHorizontalGroup(
            pnlSearchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlSearchLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(lblSearch)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 260, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnRefresh, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnBack, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16))
        );
        pnlSearchLayout.setVerticalGroup(
            pnlSearchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlSearchLayout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addGroup(pnlSearchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblSearch)
                    .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnRefresh, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnBack, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(10, 10, 10))
        );

        tblTenants.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "First Name", "Last Name", "Contact No", "Email", "Address", "Current Unit"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblTenants.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tblTenants.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblTenantsMouseClicked(evt);
            }
        });
        scrlTenants.setViewportView(tblTenants);

        lblInfo.setForeground(new java.awt.Color(130, 110, 125));
        lblInfo.setText(" ");

        javax.swing.GroupLayout pnlRootLayout = new javax.swing.GroupLayout(pnlRoot);
        pnlRoot.setLayout(pnlRootLayout);
        pnlRootLayout.setHorizontalGroup(
            pnlRootLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlRootLayout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addGroup(pnlRootLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblTitle)
                    .addComponent(pnlDetails, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlSearch, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(scrlTenants)
                    .addComponent(lblInfo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(24, 24, 24))
        );
        pnlRootLayout.setVerticalGroup(
            pnlRootLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlRootLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(lblTitle)
                .addGap(34, 34, 34)
                .addComponent(pnlDetails, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(pnlSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(scrlTenants)
                .addGap(6, 6, 6)
                .addComponent(lblInfo)
                .addGap(14, 14, 14))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlRoot, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlRoot, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveActionPerformed
        if (!txtTenantId.getText().isEmpty()) {
            warn("A tenant is selected. Press Clear first to add a new tenant, or press Update to change this one.");
            return;
        }
        Tenant t = readForm();                     
        if (t == null) return;
        try {
            if (dao.isDuplicate(t.getFirstName(), t.getLastName(), t.getContactNo(), 0)) {
                warn("This tenant (same name and contact number) is already registered.");
                return;
            }
            dao.add(t);
            info("Tenant saved successfully.");
            clearForm();
            loadTable(txtSearch.getText());
        } catch (SQLException ex) {
            ErrorHelper.show(this, ex);
        }
    }//GEN-LAST:event_btnSaveActionPerformed

    private void btnUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpdateActionPerformed
        if (txtTenantId.getText().isEmpty()) {
            warn("Select a tenant from the table first.");
            return;
        }
        Tenant t = readForm();
        if (t == null) return;
        t.setTenantId(Integer.parseInt(txtTenantId.getText()));
        try {
            if (dao.isDuplicate(t.getFirstName(), t.getLastName(), t.getContactNo(), t.getTenantId())) {
                warn("Another tenant with the same name and contact number already exists.");
                return;
            }
            dao.update(t);
            info("Tenant updated successfully.");
            clearForm();
            loadTable(txtSearch.getText());
        } catch (SQLException ex) {
            ErrorHelper.show(this, ex);
        }
    }//GEN-LAST:event_btnUpdateActionPerformed

    private void btnDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteActionPerformed
        if (txtTenantId.getText().isEmpty()) {
            warn("Select a record first.");
            return;
        }
        int id = Integer.parseInt(txtTenantId.getText());
        try {
            if (dao.hasHistory(id)) {
                warn("This tenant has contracts or maintenance requests, so the record cannot be deleted.");
                return;
            }
            if (!confirmDeleteTwice(txtFirstName.getText() + " " + txtLastName.getText())) return;
            dao.delete(id);
            info("Tenant deleted.");
            clearForm();
            loadTable(txtSearch.getText());
        } catch (SQLException ex) {
            if ("23503".equals(ex.getSQLState())) {   
                warn("This tenant is used by other records, so it cannot be deleted.");
            } else {
                ErrorHelper.show(this, ex);
            }
        }
    }//GEN-LAST:event_btnDeleteActionPerformed

    private void btnClearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClearActionPerformed
        clearForm();
    }//GEN-LAST:event_btnClearActionPerformed

    private void btnSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSearchActionPerformed
        loadTable(txtSearch.getText());
    }//GEN-LAST:event_btnSearchActionPerformed

    private void txtSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtSearchActionPerformed
        loadTable(txtSearch.getText());      
    }//GEN-LAST:event_txtSearchActionPerformed

    private void btnBackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBackActionPerformed
        dispose();      // closes this window; the Dashboard comes back
    }//GEN-LAST:event_btnBackActionPerformed

    private void btnRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshActionPerformed
        txtSearch.setText("");
        clearForm();
        loadTable("");
    }//GEN-LAST:event_btnRefreshActionPerformed

    private void tblTenantsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblTenantsMouseClicked
        int r = tblTenants.getSelectedRow();
        if (r < 0) return;
        txtTenantId.setText(cell(r, 0));
        txtFirstName.setText(cell(r, 1));
        txtLastName.setText(cell(r, 2));
        txtContact.setText(cell(r, 3));
        txtEmail.setText(cell(r, 4));
        txtAddress.setText(cell(r, 5));
        txtCurrentUnit.setText(cell(r, 6));
    }//GEN-LAST:event_tblTenantsMouseClicked

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBack;
    private javax.swing.JButton btnClear;
    private javax.swing.JButton btnDelete;
    private javax.swing.JButton btnRefresh;
    private javax.swing.JButton btnSave;
    private javax.swing.JButton btnSearch;
    private javax.swing.JButton btnUpdate;
    private javax.swing.JLabel lblAddress;
    private javax.swing.JLabel lblContact;
    private javax.swing.JLabel lblCurrentUnit;
    private javax.swing.JLabel lblEmail;
    private javax.swing.JLabel lblFirstName;
    private javax.swing.JLabel lblInfo;
    private javax.swing.JLabel lblLastName;
    private javax.swing.JLabel lblSearch;
    private javax.swing.JLabel lblTenantId;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JPanel pnlDetails;
    private javax.swing.JPanel pnlRoot;
    private javax.swing.JPanel pnlSearch;
    private javax.swing.JScrollPane scrlTenants;
    private javax.swing.JTable tblTenants;
    private javax.swing.JTextField txtAddress;
    private javax.swing.JTextField txtContact;
    private javax.swing.JTextField txtCurrentUnit;
    private javax.swing.JTextField txtEmail;
    private javax.swing.JTextField txtFirstName;
    private javax.swing.JTextField txtLastName;
    private javax.swing.JTextField txtSearch;
    private javax.swing.JTextField txtTenantId;
    // End of variables declaration//GEN-END:variables
}
