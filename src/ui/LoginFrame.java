package ui;

import javax.swing.*;
import model.User;
import util.Session;

/**
 * TEMPORARY STUB by Member 1 (Member 2 replaces it with the real login).
 * The two buttons only exist so everyone can test their module as Admin or Staff before the real login exists.
 */
public class LoginFrame extends JFrame {
    public LoginFrame() {
        setTitle("Login (stub)");
        setDefaultCloseOperation(EXIT_ON_CLOSE);             // login window may exit the app
        JPanel p = new JPanel();
        p.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        JLabel l = new JLabel("Stub login - Member 2 will replace this");
        JButton admin = new JButton("Enter as Admin (test)");
        JButton staff = new JButton("Enter as Staff (test)");
        admin.addActionListener(e -> enter("Admin"));
        staff.addActionListener(e -> enter("Staff"));
        p.add(l); p.add(Box.createVerticalStrut(10)); p.add(admin);
        p.add(Box.createVerticalStrut(6)); p.add(staff);
        add(p);
        pack();
        setLocationRelativeTo(null);
    }

    private void enter(String role) {
        User u = new User();
        u.setUserId(1);                                      // admin row created by DatabaseInitializer
        u.setUsername(role.toLowerCase());
        u.setFullName("Test " + role);
        u.setRole(role);
        u.setStatus("Active");
        Session.login(u);
        dispose();
        new DashboardFrame().setVisible(true);
    }
}
