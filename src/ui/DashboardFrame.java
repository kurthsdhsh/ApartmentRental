package ui;

import dao.DashboardDAO;
import dao.ReportDAO;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import model.User;
import util.ErrorHelper;
import util.Session;


public class DashboardFrame extends JFrame {


    private final DashboardDAO dashboardDao = new DashboardDAO();
    private final ReportDAO reportDao = new ReportDAO();

    private final StatCard cardTotal       = new StatCard("Total Units", Theme.PRIMARY_DARK);
    private final StatCard cardOccupied    = new StatCard("Occupied Units", Theme.PRIMARY);
    private final StatCard cardAvailable   = new StatCard("Available Units", Theme.SUCCESS);
    private final StatCard cardContracts   = new StatCard("Active Contracts", Theme.PRIMARY_DARK);
    private final StatCard cardRent        = new StatCard("Rent Collected This Month (PHP)", Theme.PRIMARY);
    private final StatCard cardMaintenance = new StatCard("Open Maintenance Requests", Theme.WARNING);

    private final JTable tblUnits = new JTable();
    private JButton btnUsers;         
    private JMenuItem miUsers;         

    private final Map<String, JFrame> openFrames = new HashMap<>();  
    private boolean loggingOut = false;


    public DashboardFrame() {
        setTitle("Apartment Rental Management System - Dashboard");
        setDefaultCloseOperation(EXIT_ON_CLOSE);     // the Dashboard is the main window: closing it ends the app

        setJMenuBar(buildMenuBar());
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Theme.PAGE_BG);
        root.add(buildSidebar(), BorderLayout.WEST);
        root.add(buildMain(), BorderLayout.CENTER);
        setContentPane(root);

        setSize(1180, 760);
        setMinimumSize(new Dimension(1000, 640));
        setLocationRelativeTo(null);

        // only Admin may open User management
        boolean admin = Session.isAdmin();
        btnUsers.setEnabled(admin);
        miUsers.setEnabled(admin);

        refreshCounts();
    }


    private JMenuBar buildMenuBar() {
        JMenuBar bar = new JMenuBar();
        bar.add(menu("Units",       item("Open Units",       () -> open("units", UnitFrame::new))));
        bar.add(menu("Tenants",     item("Open Tenants",     () -> open("tenants", TenantFrame::new))));
        bar.add(menu("Contracts",   item("Open Contracts",   () -> open("contracts", ContractFrame::new))));
        bar.add(menu("Payments",    item("Open Payments",    () -> open("payments", PaymentFrame::new))));
        bar.add(menu("Maintenance", item("Open Maintenance", () -> open("maintenance", MaintenanceFrame::new))));
        miUsers = item("User Management", () -> open("users", UserFrame::new));
        bar.add(menu("Users", miUsers));
        bar.add(menu("Reports",     item("Open Reports",     () -> open("reports", ReportsFrame::new))));
        bar.add(menu("Account", item("Logout", this::logout), item("Exit", this::exitApp)));
        return bar;
    }


    private JPanel buildSidebar() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(Theme.SIDEBAR);
        p.setBorder(BorderFactory.createEmptyBorder(28, 16, 20, 16));
        p.setPreferredSize(new Dimension(236, 0));

        p.add(sidebarLabel("Apartment", Theme.BRAND, Color.WHITE));
        p.add(sidebarLabel("Rental Management", Theme.SMALL, Theme.LIGHT));
        p.add(Box.createVerticalStrut(30));

        JButton dash = navButton("Dashboard", true);
        dash.addActionListener(e -> refreshCounts());
        p.add(dash);
        addNav(p, "Units",       () -> open("units", UnitFrame::new));
        addNav(p, "Tenants",     () -> open("tenants", TenantFrame::new));
        addNav(p, "Contracts",   () -> open("contracts", ContractFrame::new));
        addNav(p, "Payments",    () -> open("payments", PaymentFrame::new));
        addNav(p, "Maintenance", () -> open("maintenance", MaintenanceFrame::new));
        btnUsers = addNav(p, "Users", () -> open("users", UserFrame::new));
        addNav(p, "Reports",     () -> open("reports", ReportsFrame::new));

        p.add(Box.createVerticalGlue());          // pushes Logout to the bottom
        addNav(p, "Logout", this::logout);
        return p;
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("Dashboard");
        title.setFont(Theme.TITLE);
        title.setForeground(Theme.PRIMARY_DARK);
        JLabel welcome = new JLabel("Welcome back, " + userName() + "  |  "
                + LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")));
        welcome.setFont(Theme.BODY);
        welcome.setForeground(Theme.MUTED);
        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.add(title);
        left.add(welcome);
        header.add(left, BorderLayout.WEST);

        RoundedPanel chip = new RoundedPanel(new BorderLayout(10, 0));
        chip.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 18));
        chip.add(new Avatar(userName()), BorderLayout.WEST);
        JLabel name = new JLabel(userName());
        name.setFont(Theme.BOLD);
        name.setForeground(Theme.TEXT);
        JLabel role = new JLabel("Role: " + userRole());
        role.setFont(Theme.SMALL);
        role.setForeground(Theme.MUTED);
        JPanel texts = new JPanel();
        texts.setOpaque(false);
        texts.setLayout(new BoxLayout(texts, BoxLayout.Y_AXIS));
        texts.add(name);
        texts.add(role);
        chip.add(texts, BorderLayout.CENTER);
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        right.setOpaque(false);
        right.add(chip);
        header.add(right, BorderLayout.EAST);
        return header;
    }


    private JPanel buildCards() {
        JPanel cards = new JPanel(new GridLayout(2, 3, 16, 16));
        cards.setOpaque(false);
        cards.setPreferredSize(new Dimension(0, 196));
        cards.add(cardTotal);
        cards.add(cardOccupied);
        cards.add(cardAvailable);
        cards.add(cardContracts);
        cards.add(cardRent);
        cards.add(cardMaintenance);
        return cards;
    }


    private JPanel buildTablePanel() {
        RoundedPanel p = new RoundedPanel(new BorderLayout(0, 10));
        p.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));

        JLabel title = new JLabel("Unit Occupancy");
        title.setFont(Theme.H2);
        title.setForeground(Theme.PRIMARY_DARK);
        JLabel hint = new JLabel("Every unit and who is renting it right now");
        hint.setFont(Theme.SMALL);
        hint.setForeground(Theme.MUTED);
        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.add(title);
        top.add(hint);
        p.add(top, BorderLayout.NORTH);

        tblUnits.setRowHeight(32);
        tblUnits.setShowGrid(false);
        tblUnits.setIntercellSpacing(new Dimension(0, 0));
        tblUnits.setFillsViewportHeight(true);
        tblUnits.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblUnits.getTableHeader().setReorderingAllowed(false);
        tblUnits.getTableHeader().setDefaultRenderer(new HeaderRenderer());
        tblUnits.getTableHeader().setPreferredSize(new Dimension(0, 34));

        JScrollPane scroll = new JScrollPane(tblUnits);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setViewportBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Theme.CARD);
        p.add(scroll, BorderLayout.CENTER);
        return p;
    }

    private JPanel buildMain() {
        JPanel main = new JPanel(new BorderLayout(0, 20));
        main.setOpaque(false);
        main.setBorder(BorderFactory.createEmptyBorder(22, 26, 22, 26));
        main.add(buildHeader(), BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(0, 18));
        body.setOpaque(false);
        body.add(buildCards(), BorderLayout.NORTH);
        body.add(buildTablePanel(), BorderLayout.CENTER);
        main.add(body, BorderLayout.CENTER);
        return main;
    }


    private void open(String key, Supplier<JFrame> maker) {
        JFrame f = openFrames.get(key);
        if (f != null && f.isDisplayable()) {
            f.setState(Frame.NORMAL);
            f.toFront();
            return;
        }
        f = maker.get();                              // e.g. new UnitFrame()
        f.addWindowListener(new WindowAdapter() {
            @Override public void windowClosed(WindowEvent e) { refreshCounts(); }   // numbers stay live
        });
        openFrames.put(key, f);
        f.setVisible(true);
    }

  
    private void refreshCounts() {
        if (loggingOut) return;
        try {
            cardTotal.setValue(String.valueOf(dashboardDao.totalUnits()));
            cardOccupied.setValue(String.valueOf(dashboardDao.occupiedUnits()));
            cardAvailable.setValue(String.valueOf(dashboardDao.availableUnits()));
            cardContracts.setValue(String.valueOf(dashboardDao.activeContracts()));
            cardMaintenance.setValue(String.valueOf(dashboardDao.openMaintenance()));
            BigDecimal rent = dashboardDao.rentCollected(YearMonth.now().toString());   // "2026-10"
            cardRent.setValue(String.format("%,.2f", rent));
            loadOccupancyTable();
        } catch (SQLException ex) {
            ErrorHelper.show(this, ex);               
        }
    }

    private void loadOccupancyTable() throws SQLException {
        DefaultTableModel model = reportDao.unitOccupancy();
        model.setColumnIdentifiers(new String[]{"Unit", "Type", "Monthly Rent (PHP)", "Status", "Current Tenant"});
        tblUnits.setModel(model);
        CellRenderer cell = new CellRenderer();
        for (int i = 0; i < tblUnits.getColumnCount(); i++) {
            tblUnits.getColumnModel().getColumn(i).setCellRenderer(cell);
        }
    }

    private void logout() {
        int ok = JOptionPane.showConfirmDialog(this, "Log out now?", "Logout", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;
        loggingOut = true;
        Session.logout();
        for (Window w : Window.getWindows()) w.dispose();   // closes every open module AND this window
        new LoginFrame().setVisible(true);
    }

    private void exitApp() {
        int ok = JOptionPane.showConfirmDialog(this, "Exit the application?", "Exit", JOptionPane.YES_NO_OPTION);
        if (ok == JOptionPane.YES_OPTION) System.exit(0);
    }


    private String userName() {
        User u = Session.getCurrentUser();
        return (u == null || u.getFullName() == null) ? "Guest" : u.getFullName();
    }

    private String userRole() {
        User u = Session.getCurrentUser();
        return (u == null || u.getRole() == null) ? "-" : u.getRole();
    }

    private JMenu menu(String title, JMenuItem... items) {
        JMenu m = new JMenu(title);
        for (JMenuItem i : items) m.add(i);
        return m;
    }

    private JMenuItem item(String text, Runnable action) {
        JMenuItem i = new JMenuItem(text);
        i.addActionListener(e -> action.run());
        return i;
    }

    private JLabel sidebarLabel(String text, Font font, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(font);
        l.setForeground(color);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JButton addNav(JPanel sidebar, String text, Runnable action) {
        JButton b = navButton(text, false);
        b.addActionListener(e -> action.run());
        sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(b);
        return b;
    }


    private JButton navButton(String text, boolean active) {
        JButton b = new JButton(text);
        b.setFont(Theme.NAV);
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setBorder(BorderFactory.createEmptyBorder(0, 18, 0, 10));
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        b.setPreferredSize(new Dimension(200, 42));
        if (active) PillButtonUI.apply(b, Color.WHITE, Theme.PRIMARY_DARK);
        else        PillButtonUI.apply(b, Theme.SIDEBAR, Color.WHITE);
        return b;
    }



    private static class RoundedPanel extends JPanel {
        RoundedPanel(LayoutManager layout) {
            super(layout);
            setOpaque(false);
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(0, 0, 0, 18));
            g2.fillRoundRect(2, 3, getWidth() - 4, getHeight() - 4, 22, 22);       // shadow
            g2.setColor(Theme.CARD);
            g2.fillRoundRect(0, 0, getWidth() - 3, getHeight() - 4, 22, 22);       // card
            g2.dispose();
        }
    }


    private static class StatCard extends RoundedPanel {
        private final JLabel value = new JLabel("-");
        private final Color accent;

        StatCard(String title, Color accent) {
            super(new BorderLayout(0, 4));
            this.accent = accent;
            setBorder(BorderFactory.createEmptyBorder(16, 34, 16, 16));
            JLabel t = new JLabel(title);
            t.setFont(Theme.BODY);
            t.setForeground(Theme.MUTED);
            value.setFont(Theme.NUMBER);
            value.setForeground(Theme.TEXT);
            add(t, BorderLayout.NORTH);
            add(value, BorderLayout.CENTER);
        }

        void setValue(String text) { value.setText(text); }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(accent);
            g2.fillRoundRect(16, 18, 6, getHeight() - 40, 6, 6);                   
            g2.dispose();
        }
    }


    private static class Avatar extends JComponent {
        private final String letter;

        Avatar(String name) {
            letter = name.isEmpty() ? "?" : name.substring(0, 1).toUpperCase();
            setPreferredSize(new Dimension(40, 40));
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setColor(Theme.PRIMARY);
            g2.fillOval(0, 0, getWidth() - 1, getHeight() - 1);
            g2.setColor(Color.WHITE);
            g2.setFont(Theme.H2);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(letter, (getWidth() - fm.stringWidth(letter)) / 2,
                    (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }
    }


    private static class HeaderRenderer extends DefaultTableCellRenderer {
        HeaderRenderer() {
            setOpaque(true);
            setHorizontalAlignment(LEFT);
        }

        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel,
                boolean focus, int row, int col) {
            super.getTableCellRendererComponent(t, v, false, false, row, col);
            setBackground(Theme.PRIMARY_DARK);
            setForeground(Color.WHITE);
            setFont(Theme.BOLD);
            setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
            return this;
        }
    }

    private static class CellRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel,
                boolean focus, int row, int col) {
            int c = t.convertColumnIndexToModel(col);
            Object shown = (c == 2 && v instanceof BigDecimal) ? String.format("%,.2f", v) : v;
            super.getTableCellRendererComponent(t, shown, sel, false, row, col);
            setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
            setFont(Theme.BODY);
            setHorizontalAlignment(c == 2 ? RIGHT : LEFT);
            if (sel) {
                setBackground(Theme.PRIMARY);
                setForeground(Color.WHITE);
            } else {
                setBackground(row % 2 == 0 ? Theme.CARD : Theme.ROW_ALT);
                setForeground(Theme.TEXT);
                if (c == 3 && v != null) {                       
                    setFont(Theme.BOLD);
                    String s = v.toString();
                    if ("Available".equals(s))        setForeground(Theme.SUCCESS);
                    else if ("Occupied".equals(s))    setForeground(Theme.PRIMARY_DARK);
                    else if ("Maintenance".equals(s)) setForeground(Theme.WARNING);
                }
            }
            return this;
        }
    }
}