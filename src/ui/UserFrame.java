package ui;

import dao.UserDAO;
import java.awt.Color;
import java.sql.SQLException;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import model.User;
import util.Constants;
import util.ErrorHelper;
import util.Session;
import util.Validator;

public class UserFrame extends javax.swing.JFrame {

    private static final String STATUS_ACTIVE = "Active";
    private static final String STATUS_INACTIVE = "Inactive";

    private final UserDAO dao = new UserDAO();   

    public UserFrame() {
        initComponents();
        setLocationRelativeTo(null);

        PillButtonUI.apply(btnSave, Theme.PRIMARY, Color.WHITE);
        PillButtonUI.apply(btnUpdate, Theme.PRIMARY_DARK, Color.WHITE);
        PillButtonUI.apply(btnDelete, Theme.DANGER, Color.WHITE);
        PillButtonUI.apply(btnClear, new Color(160, 160, 160), Color.WHITE);
        PillButtonUI.apply(btnSearch, Theme.PRIMARY, Color.WHITE);
        PillButtonUI.apply(btnRefresh, Theme.PRIMARY_DARK, Color.WHITE);
        PillButtonUI.apply(btnBack, Theme.SIDEBAR, Color.WHITE);
        TableStyle.apply(tblUsers);
        tblUsers.getColumnModel().getColumn(0).setMaxWidth(70);

        boolean admin = Session.isAdmin();
        btnDelete.setEnabled(admin);
        btnSave.setEnabled(admin);
        btnUpdate.setEnabled(admin);

        clearForm();
        loadTable("");
        if (!admin) {
            lblInfo.setText("Only an Admin can manage users.");
        }
    }


    private void loadTable(String keyword) {
        try {
            List<User> list = dao.search(keyword);
            DefaultTableModel m = (DefaultTableModel) tblUsers.getModel();
            m.setRowCount(0);
            for (User u : list) {
                m.addRow(new Object[]{u.getUserId(), u.getUsername(), u.getFullName(), u.getRole(), u.getStatus()});
            }
            lblInfo.setText(list.isEmpty() ? "No records found." : list.size() + " user(s)");
        } catch (SQLException ex) {
            ErrorHelper.show(this, ex);
        }
    }

    private void clearForm() {
        txtUserId.setText("");
        txtUsername.setText("");
        txtPassword.setText("");
        txtConfirm.setText("");
        txtFullName.setText("");
        cmbRole.setSelectedItem(Constants.ROLE_STAFF);
        cmbStatus.setSelectedItem(STATUS_ACTIVE);
        tblUsers.clearSelection();
        txtUsername.requestFocus();
    }

    private User readForm(boolean passwordRequired) {
        String username = txtUsername.getText().trim();
        String fullName = txtFullName.getText().trim();
        String pw = new String(txtPassword.getPassword());
        String confirm = new String(txtConfirm.getPassword());

        if (Validator.isEmpty(username)) { warn("Username is required."); txtUsername.requestFocus(); return null; }
        if (username.length() < 3 || username.length() > 50 || username.contains(" ")) {
            warn("Username must be 3 to 50 characters with no spaces."); txtUsername.requestFocus(); return null;
        }
        if (Validator.isEmpty(fullName)) { warn("Full name is required."); txtFullName.requestFocus(); return null; }
        if (fullName.length() > 100) { warn("Full name must be 100 characters or less."); txtFullName.requestFocus(); return null; }
        if (passwordRequired || !pw.isEmpty() || !confirm.isEmpty()) {
            if (pw.length() < 6) { warn("Password must be at least 6 characters."); txtPassword.requestFocus(); return null; }
            if (!pw.equals(confirm)) { warn("Password and Confirm password do not match."); txtConfirm.requestFocus(); return null; }
        }
        User u = new User();
        u.setUsername(username);
        u.setFullName(fullName);
        u.setPassword(pw);                      
        u.setRole((String) cmbRole.getSelectedItem());
        u.setStatus((String) cmbStatus.getSelectedItem());
        return u;
    }

    private void showSqlError(SQLException ex) {
        if ("23505".equals(ex.getSQLState())) {   
            warn("Username already exists. Please choose another one.");
            txtUsername.requestFocus();
        } else {
            ErrorHelper.show(this, ex);
        }
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
        lblUserId = new javax.swing.JLabel();
        txtUserId = new javax.swing.JTextField();
        lblUsername = new javax.swing.JLabel();
        txtUsername = new javax.swing.JTextField();
        lblPassword = new javax.swing.JLabel();
        txtPassword = new javax.swing.JPasswordField();
        lblConfirm = new javax.swing.JLabel();
        txtConfirm = new javax.swing.JPasswordField();
        lblFullName = new javax.swing.JLabel();
        txtFullName = new javax.swing.JTextField();
        lblRole = new javax.swing.JLabel();
        cmbRole = new javax.swing.JComboBox<>();
        lblStatus = new javax.swing.JLabel();
        cmbStatus = new javax.swing.JComboBox<>();
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
        scrlUsers = new javax.swing.JScrollPane();
        tblUsers = new javax.swing.JTable();
        lblInfo = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("User Management");
        setPreferredSize(new java.awt.Dimension(1020, 720));

        pnlRoot.setBackground(new java.awt.Color(249, 241, 246));

        lblTitle.setFont(new java.awt.Font("Segoe UI", 1, 26)); // NOI18N
        lblTitle.setForeground(new java.awt.Color(140, 40, 100));
        lblTitle.setText("User Management");

        pnlDetails.setBackground(new java.awt.Color(255, 255, 255));

        lblUserId.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblUserId.setForeground(new java.awt.Color(60, 40, 55));
        lblUserId.setText("User ID");

        txtUserId.setEditable(false);
        txtUserId.setBackground(new java.awt.Color(243, 238, 241));
        txtUserId.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N

        lblUsername.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblUsername.setForeground(new java.awt.Color(60, 40, 55));
        lblUsername.setText("Username *");

        txtUsername.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N

        lblPassword.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblPassword.setForeground(new java.awt.Color(60, 40, 55));
        lblPassword.setText("Password *");

        txtPassword.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N

        lblConfirm.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblConfirm.setForeground(new java.awt.Color(60, 40, 55));
        lblConfirm.setText("Confirm password *");

        txtConfirm.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N

        lblFullName.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblFullName.setForeground(new java.awt.Color(60, 40, 55));
        lblFullName.setText("Full name *");

        txtFullName.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N

        lblRole.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblRole.setForeground(new java.awt.Color(60, 40, 55));
        lblRole.setText("Role");

        cmbRole.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        cmbRole.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Admin", "Staff" }));

        lblStatus.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblStatus.setForeground(new java.awt.Color(60, 40, 55));
        lblStatus.setText("Status");

        cmbStatus.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        cmbStatus.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Active", "Inactive" }));

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
                    .addComponent(lblUserId)
                    .addComponent(lblUsername)
                    .addComponent(lblPassword)
                    .addComponent(lblConfirm))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtUserId)
                    .addComponent(txtUsername)
                    .addComponent(txtPassword)
                    .addComponent(txtConfirm))
                .addGap(28, 28, 28)
                .addGroup(pnlDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblFullName)
                    .addComponent(lblRole)
                    .addComponent(lblStatus))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtFullName)
                    .addComponent(cmbRole, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(cmbStatus, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
                            .addComponent(lblUserId)
                            .addComponent(txtUserId, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(10, 10, 10)
                        .addGroup(pnlDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblUsername)
                            .addComponent(txtUsername, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(10, 10, 10)
                        .addGroup(pnlDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblPassword)
                            .addComponent(txtPassword, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(10, 10, 10)
                        .addGroup(pnlDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblConfirm)
                            .addComponent(txtConfirm, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(pnlDetailsLayout.createSequentialGroup()
                        .addGroup(pnlDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblFullName)
                            .addComponent(txtFullName, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(10, 10, 10)
                        .addGroup(pnlDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblRole)
                            .addComponent(cmbRole, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(10, 10, 10)
                        .addGroup(pnlDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblStatus)
                            .addComponent(cmbStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)))
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
        lblSearch.setText("Search (username or full name):");

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

        tblUsers.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "Username", "Full Name", "Role", "Status"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblUsers.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tblUsers.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblUsersMouseClicked(evt);
            }
        });
        scrlUsers.setViewportView(tblUsers);

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
                    .addComponent(scrlUsers)
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
                .addComponent(scrlUsers)
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
        if (!txtUserId.getText().isEmpty()) {
            warn("A user is selected. Press Clear first to add a new user, or press Update to change this one.");
            return;
        }
        User u = readForm(true);                    
        if (u == null) return;
        try {
            dao.add(u);
            info("User saved successfully.");
            clearForm();
            loadTable(txtSearch.getText());
        } catch (SQLException ex) {
            showSqlError(ex);
        }
    }//GEN-LAST:event_btnSaveActionPerformed

    private void btnUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpdateActionPerformed
        if (txtUserId.getText().isEmpty()) {
            warn("Select a user from the table first.");
            return;
        }
        User u = readForm(false);                    
        if (u == null) return;
        u.setUserId(Integer.parseInt(txtUserId.getText()));

        User me = Session.getCurrentUser();
        boolean isMe = me != null && me.getUserId() == u.getUserId();
        if (isMe && STATUS_INACTIVE.equals(u.getStatus())) {
            warn("You cannot deactivate your own account.");
            return;
        }
        if (isMe && !Constants.ROLE_ADMIN.equals(u.getRole())) {
            warn("You cannot remove your own Admin role.");
            return;
        }
        try {
            dao.update(u);
            if (isMe) {                               
                me.setUsername(u.getUsername());
                me.setFullName(u.getFullName());
            }
            info("User updated successfully.");
            clearForm();
            loadTable(txtSearch.getText());
        } catch (SQLException ex) {
            showSqlError(ex);
        }
    }//GEN-LAST:event_btnUpdateActionPerformed

    private void btnDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteActionPerformed
        if (txtUserId.getText().isEmpty()) {
            warn("Select a record first.");
            return;
        }
        int id = Integer.parseInt(txtUserId.getText());
        User me = Session.getCurrentUser();
        if (me != null && me.getUserId() == id) {
            warn("You cannot delete your own account.");
            return;
        }
        try {
            User target = dao.getById(id);
            if (target == null) {
                warn("This user no longer exists.");
                clearForm();
                loadTable(txtSearch.getText());
                return;
            }
            if (Constants.ROLE_ADMIN.equals(target.getRole()) && dao.countAdmins() <= 1) {
                warn("You cannot delete the last Admin.");
                return;
            }
            int ok = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to delete the user \"" + target.getUsername() + "\"?",
                    "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (ok != JOptionPane.YES_OPTION) return;
            dao.delete(id);
            info("User deleted.");
            clearForm();
            loadTable(txtSearch.getText());
        } catch (SQLException ex) {
            if ("23503".equals(ex.getSQLState())) {   // foreign key: this user processed payments
                warn("This user has processed payments, so the account cannot be deleted.\n"
                        + "Set the status to Inactive instead.");
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
        loadTable(txtSearch.getText());      // Enter key searches
    }//GEN-LAST:event_txtSearchActionPerformed

    private void btnBackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBackActionPerformed
        dispose();     
    }//GEN-LAST:event_btnBackActionPerformed

    private void btnRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshActionPerformed
        txtSearch.setText("");
        clearForm();
        loadTable("");
    }//GEN-LAST:event_btnRefreshActionPerformed

    private void tblUsersMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblUsersMouseClicked
        int r = tblUsers.getSelectedRow();
        if (r < 0) return;
        txtUserId.setText(String.valueOf(tblUsers.getValueAt(r, 0)));
        txtUsername.setText(String.valueOf(tblUsers.getValueAt(r, 1)));
        txtFullName.setText(String.valueOf(tblUsers.getValueAt(r, 2)));
        cmbRole.setSelectedItem(tblUsers.getValueAt(r, 3));
        cmbStatus.setSelectedItem(tblUsers.getValueAt(r, 4));
        txtPassword.setText("");                       // passwords are never shown
        txtConfirm.setText("");
    }//GEN-LAST:event_tblUsersMouseClicked

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBack;
    private javax.swing.JButton btnClear;
    private javax.swing.JButton btnDelete;
    private javax.swing.JButton btnRefresh;
    private javax.swing.JButton btnSave;
    private javax.swing.JButton btnSearch;
    private javax.swing.JButton btnUpdate;
    private javax.swing.JComboBox<String> cmbRole;
    private javax.swing.JComboBox<String> cmbStatus;
    private javax.swing.JLabel lblConfirm;
    private javax.swing.JLabel lblFullName;
    private javax.swing.JLabel lblInfo;
    private javax.swing.JLabel lblPassword;
    private javax.swing.JLabel lblRole;
    private javax.swing.JLabel lblSearch;
    private javax.swing.JLabel lblStatus;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JLabel lblUserId;
    private javax.swing.JLabel lblUsername;
    private javax.swing.JPanel pnlDetails;
    private javax.swing.JPanel pnlRoot;
    private javax.swing.JPanel pnlSearch;
    private javax.swing.JScrollPane scrlUsers;
    private javax.swing.JTable tblUsers;
    private javax.swing.JPasswordField txtConfirm;
    private javax.swing.JTextField txtFullName;
    private javax.swing.JPasswordField txtPassword;
    private javax.swing.JTextField txtSearch;
    private javax.swing.JTextField txtUserId;
    private javax.swing.JTextField txtUsername;
    // End of variables declaration//GEN-END:variables
}
