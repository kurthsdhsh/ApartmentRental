package ui;

import dao.UserDAO;
import java.awt.Color;
import java.awt.Dimension;
import java.sql.SQLException;
import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import model.User;
import util.ErrorHelper;
import util.Session;
import util.Validator;


 
public class LoginFrame extends javax.swing.JFrame {


    private static final Color PLUM   = new Color(140, 40, 100);   
    private static final Color MAUVE  = new Color(153, 100, 117);  
    private static final Color MAROON = new Color(100, 20, 26);    
    private static final Color LINE   = new Color(190, 160, 180);  

    private final UserDAO userDao = new UserDAO();

    public LoginFrame() {
        setContentPane(new BackgroundPanel("/images/login_bg.png"));   
        initComponents();

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        getRootPane().setDefaultButton(btnLogin);                      
        getContentPane().setPreferredSize(new Dimension(760, 480));    
        pack();
        setLocationRelativeTo(null);


        for (javax.swing.JTextField t : new javax.swing.JTextField[]{txtUsername, txtPassword}) {
            t.setForeground(Theme.TEXT);
            t.setCaretColor(PLUM);
            t.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(LINE),
                    BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        }


        PillButtonUI.apply(btnLogin, PLUM,   Color.WHITE);
        PillButtonUI.apply(btnClear, MAUVE,  Color.WHITE);
        PillButtonUI.apply(btnExit,  MAROON, Color.WHITE);

        txtUsername.requestFocus();
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblWelcome = new javax.swing.JLabel();
        lblSubtitle = new javax.swing.JLabel();
        lblUsername = new javax.swing.JLabel();
        txtUsername = new javax.swing.JTextField();
        lblPassword = new javax.swing.JLabel();
        txtPassword = new javax.swing.JPasswordField();
        btnLogin = new javax.swing.JButton();
        btnClear = new javax.swing.JButton();
        btnExit = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Login");

        lblWelcome.setFont(new java.awt.Font("Georgia", 1, 28)); // NOI18N
        lblWelcome.setForeground(new java.awt.Color(140, 40, 100));
        lblWelcome.setText("Welcome");

        lblSubtitle.setForeground(new java.awt.Color(130, 110, 125));
        lblSubtitle.setText("Sign in to continue");

        lblUsername.setFont(new java.awt.Font("Segoe UI", 1, 11)); // NOI18N
        lblUsername.setForeground(new java.awt.Color(60, 40, 55));
        lblUsername.setText("USERNAME");

        txtUsername.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        txtUsername.addActionListener(this::txtUsernameActionPerformed);

        lblPassword.setFont(new java.awt.Font("Segoe UI", 1, 11)); // NOI18N
        lblPassword.setForeground(new java.awt.Color(60, 40, 55));
        lblPassword.setText("PASSWORD");

        txtPassword.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N

        btnLogin.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnLogin.setText("LOG IN");
        btnLogin.addActionListener(this::btnLoginActionPerformed);

        btnClear.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnClear.setText("CLEAR");
        btnClear.addActionListener(this::btnClearActionPerformed);

        btnExit.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnExit.setText("EXIT");
        btnExit.addActionListener(this::btnExitActionPerformed);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(533, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblWelcome)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(lblSubtitle)
                        .addComponent(lblUsername)
                        .addComponent(txtUsername, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(lblPassword)
                        .addComponent(txtPassword, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnLogin, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(btnClear, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(10, 10, 10)
                            .addComponent(btnExit, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(33, 33, 33))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(57, 57, 57)
                .addComponent(lblWelcome)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblSubtitle)
                .addGap(28, 28, 28)
                .addComponent(lblUsername)
                .addGap(4, 4, 4)
                .addComponent(txtUsername, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(14, 14, 14)
                .addComponent(lblPassword)
                .addGap(4, 4, 4)
                .addComponent(txtPassword, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(22, 22, 22)
                .addComponent(btnLogin, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnClear, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnExit, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(57, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnLoginActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLoginActionPerformed
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());   


        if (Validator.isEmpty(username) || Validator.isEmpty(password)) {
            JOptionPane.showMessageDialog(this, "Please enter your username and password.",
                    "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            User user = userDao.authenticate(username, password);
            if (user == null) {                                    
                JOptionPane.showMessageDialog(this, "Invalid username or password.",
                        "Login Failed", JOptionPane.ERROR_MESSAGE);
                txtPassword.setText("");
                txtPassword.requestFocus();
                return;
            }

            Session.login(user);
            dispose();
            new DashboardFrame().setVisible(true);
        } catch (SQLException ex) {
            ErrorHelper.show(this, ex);                              
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
        txtPassword.requestFocus();     
    }//GEN-LAST:event_txtUsernameActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnClear;
    private javax.swing.JButton btnExit;
    private javax.swing.JButton btnLogin;
    private javax.swing.JLabel lblPassword;
    private javax.swing.JLabel lblSubtitle;
    private javax.swing.JLabel lblUsername;
    private javax.swing.JLabel lblWelcome;
    private javax.swing.JPasswordField txtPassword;
    private javax.swing.JTextField txtUsername;
    // End of variables declaration//GEN-END:variables
}
