package ui;

import dao.UserDAO;
import java.sql.SQLException;
import javax.swing.JOptionPane;
import model.User;
import util.ErrorHelper;
import util.Session;
import util.Validator;
import java.awt.Color;
import java.awt.Dimension;

public class LoginFrame extends javax.swing.JFrame {

    private final UserDAO userDao = new UserDAO();

    public LoginFrame() {
        setContentPane(new BackgroundPanel("/images/login_bg.png"));   
        initComponents();

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        getRootPane().setDefaultButton(btnLogin);
        getContentPane().setPreferredSize(new Dimension(760, 480));    // your picture's size
        pack();
        setLocationRelativeTo(null);

        // button colors (Windows needs this one line each)
        PillButtonUI.apply(btnLogin, new Color(190, 92, 142), Color.WHITE);
        PillButtonUI.apply(btnClear, new Color(160, 160, 160), Color.WHITE);
        PillButtonUI.apply(btnExit,  new Color(190, 50, 50),  Color.WHITE);

        txtUsername.requestFocus();
    }
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblTitle = new javax.swing.JLabel();
        lblUsername = new javax.swing.JLabel();
        txtUsername = new javax.swing.JTextField();
        lblPassword = new javax.swing.JLabel();
        txtPassword = new javax.swing.JPasswordField();
        btnLogin = new javax.swing.JButton();
        btnClear = new javax.swing.JButton();
        btnExit = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("LOGIN");
        setBackground(new java.awt.Color(51, 0, 51));
        setForeground(java.awt.Color.yellow);
        setPreferredSize(new java.awt.Dimension(760, 480));
        setResizable(false);
        setSize(new java.awt.Dimension(21, 12));

        lblTitle.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblTitle.setForeground(new java.awt.Color(255, 0, 153));
        lblTitle.setText("APARTMENT RENTAL MANAGEMENT SYSTEM");

        lblUsername.setText("Username:");

        txtUsername.setText("Enter your username");
        txtUsername.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        txtUsername.addActionListener(this::txtUsernameActionPerformed);

        lblPassword.setText("Password:");

        txtPassword.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        btnLogin.setText("LOGIN");
        btnLogin.addActionListener(this::btnLoginActionPerformed);

        btnClear.setText("CLEAR");
        btnClear.addActionListener(this::btnClearActionPerformed);

        btnExit.setText("EXIT");
        btnExit.addActionListener(this::btnExitActionPerformed);

        jLabel1.setFont(new java.awt.Font("Segoe UI Semibold", 0, 14)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 0, 200));
        jLabel1.setText("LOG-IN");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(375, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(lblPassword)
                            .addComponent(lblUsername))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(txtUsername)
                            .addComponent(txtPassword, javax.swing.GroupLayout.PREFERRED_SIZE, 176, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(lblTitle)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(2, 2, 2)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(btnLogin, javax.swing.GroupLayout.PREFERRED_SIZE, 241, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(btnExit, javax.swing.GroupLayout.PREFERRED_SIZE, 112, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnClear, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addComponent(jLabel1))
                .addGap(78, 78, 78))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(127, 127, 127)
                .addComponent(lblTitle)
                .addGap(42, 42, 42)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblUsername)
                    .addComponent(txtUsername, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblPassword)
                    .addComponent(txtPassword, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnLogin)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnExit)
                    .addComponent(btnClear))
                .addContainerGap(264, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnLoginActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLoginActionPerformed
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());   // JPasswordField gives char[]

        // 1. validate
        if (Validator.isEmpty(username) || Validator.isEmpty(password)) {
            JOptionPane.showMessageDialog(this, "Please enter your username and password.",
                    "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        // 2. ask the DAO (frames never write SQL)
        try {
            User user = userDao.authenticate(username, password);
            if (user == null) {                                      // wrong login OR Inactive account
                JOptionPane.showMessageDialog(this, "Invalid username or password.",
                        "Login Failed", JOptionPane.ERROR_MESSAGE);
                txtPassword.setText("");
                txtPassword.requestFocus();
                return;
            }
            // 3. success: remember who logged in, swap screens
            Session.login(user);
            dispose();
            new DashboardFrame().setVisible(true);
        } catch (SQLException ex) {
            ErrorHelper.show(this, ex);                              // friendly "Is Derby running?" message
        }
    }//GEN-LAST:event_btnLoginActionPerformed

    private void btnClearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClearActionPerformed
        txtUsername.setText("");
        txtPassword.setText("");
        txtUsername.requestFocus();
    }//GEN-LAST:event_btnClearActionPerformed

    private void btnExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExitActionPerformed
        int ok = JOptionPane.showConfirmDialog(this, "Exit the application?",
                "Exit", JOptionPane.YES_NO_OPTION);
        if (ok == JOptionPane.YES_OPTION) System.exit(0);
    }//GEN-LAST:event_btnExitActionPerformed

    private void txtUsernameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtUsernameActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtUsernameActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnClear;
    private javax.swing.JButton btnExit;
    private javax.swing.JButton btnLogin;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel lblPassword;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JLabel lblUsername;
    private javax.swing.JPasswordField txtPassword;
    private javax.swing.JTextField txtUsername;
    // End of variables declaration//GEN-END:variables
}
