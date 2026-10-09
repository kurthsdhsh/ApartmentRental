package ui;

import java.awt.GridLayout;
import javax.swing.*;
import util.Session;

/** TEMPORARY STUB by Member 1 (Member 2 replaces it). Buttons open every module frame. */
public class DashboardFrame extends JFrame {
    public DashboardFrame() {
        setTitle("Dashboard (stub)");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        JPanel p = new JPanel(new GridLayout(0, 1, 6, 6));
        p.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        p.add(new JLabel("Welcome, " + Session.getCurrentUser().getFullName()
                + " (" + Session.getCurrentUser().getRole() + ")"));
        addBtn(p, "Units", () -> new UnitFrame());
        addBtn(p, "Tenants", () -> new TenantFrame());
        addBtn(p, "Contracts", () -> new ContractFrame());
        addBtn(p, "Payments", () -> new PaymentFrame());
        addBtn(p, "Maintenance", () -> new MaintenanceFrame());
        addBtn(p, "Users", () -> new UserFrame());
        addBtn(p, "Reports", () -> new ReportsFrame());
        add(p);
        pack();
        setLocationRelativeTo(null);
    }

    private void addBtn(JPanel p, String text, java.util.function.Supplier<JFrame> opener) {
        JButton b = new JButton(text);
        b.addActionListener(e -> opener.get().setVisible(true));
        p.add(b);
    }
}
