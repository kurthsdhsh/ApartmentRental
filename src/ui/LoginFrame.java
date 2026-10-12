package ui;

import dao.UserDAO;
import java.sql.SQLException;
import javax.swing.JOptionPane;
import model.User;
import util.ErrorHelper;
import util.Session;
import util.Validator;

public class LoginFrame extends javax.swing.JFrame {

    private final UserDAO userDao = new UserDAO();

    public LoginFrame() {
        initComponents();                                   // NetBeans builds the screen
        setDefaultCloseOperation(EXIT_ON_CLOSE);            // login is the ONE frame that may exit the app
        setLocationRelativeTo(null);                        // center on screen
        getRootPane().setDefaultButton(btnLogin);           // pressing Enter = click LOGIN
        // look: same colors/fonts as the rest of the app (see util/Style.java)
//        Style.styleButton(btnLogin);
//        Style.styleButton(btnClear);
//        Style.styleButton(btnExit);
//        btnExit.setBackground(Style.DANGER);
//        lblTitle.setFont(Style.TITLE);
//        lblTitle.setForeground(Style.PRIMARY);
//        getContentPane().setBackground(Style.BG);
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

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("LOGIN");
        setBackground(new java.awt.Color(51, 0, 51));
        setForeground(java.awt.Color.yellow);
        setResizable(false);

        lblTitle.setText("APARTMENT RENTAL MANAGEMENT SYSTEM");

        lblUsername.setText("Username:");

        lblPassword.setText("Password:");

        btnLogin.setText("LOGIN");
        btnLogin.addActionListener(this::btnLoginActionPerformed);

        btnClear.setText("CLEAR");
        btnClear.addActionListener(this::btnClearActionPerformed);

        btnExit.setText("EXIT");
        btnExit.addActionListener(this::btnExitActionPerformed);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addComponent(lblTitle))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(188, 188, 188)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(lblPassword)
                            .addComponent(lblUsername))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(txtUsername)
                            .addComponent(txtPassword, javax.swing.GroupLayout.DEFAULT_SIZE, 176, Short.MAX_VALUE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(222, 222, 222)
                        .addComponent(btnExit)
                        .addGap(18, 18, 18)
                        .addComponent(btnClear)
                        .addGap(18, 18, 18)
                        .addComponent(btnLogin)))
                .addContainerGap(6914, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(lblTitle)
                .addGap(51, 51, 51)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblUsername)
                    .addComponent(txtUsername, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblPassword)
                    .addComponent(txtPassword, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnLogin)
                    .addComponent(btnClear)
                    .addComponent(btnExit))
                .addContainerGap(679, Short.MAX_VALUE))
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


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnClear;
    private javax.swing.JButton btnExit;
    private javax.swing.JButton btnLogin;
    private javax.swing.JLabel lblPassword;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JLabel lblUsername;
    private javax.swing.JPasswordField txtPassword;
    private javax.swing.JTextField txtUsername;
    // End of variables declaration//GEN-END:variables
}
