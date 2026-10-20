package ui;

import dao.DashboardDAO;
import dao.ReportDAO;
import java.awt.Color;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.function.Supplier;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import model.User;
import util.ErrorHelper;
import util.Session;


public class DashboardFrame extends javax.swing.JFrame {

    private final DashboardDAO dashboardDao = new DashboardDAO();
    private final ReportDAO reportDao = new ReportDAO();

    public DashboardFrame() {
        initComponents();
        setLocationRelativeTo(null);

        PillButtonUI.apply(btnDashboard, Theme.PRIMARY, Color.WHITE);
        PillButtonUI.apply(btnLogout, new Color(150, 60, 115), Color.WHITE);
        javax.swing.JButton[] modules = {btnUnits, btnTenants, btnContracts, btnPayments, btnMaintenance, btnUsers, btnReports};
        for (javax.swing.JButton b : modules) {
            PillButtonUI.apply(b, Theme.PRIMARY, Color.WHITE);
        }
        TableStyle.apply(tblUnits);

        User u = Session.getCurrentUser();
        String name = (u == null || u.getFullName() == null) ? "Guest" : u.getFullName();
        String role = (u == null || u.getRole() == null) ? "-" : u.getRole();
        lblWelcome.setText("Welcome back, " + name);
        lblDate.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")));
        lblUserName.setText(name);
        lblUserRole.setText(role);
        lblInitial.setText(name.isEmpty() ? "?" : name.substring(0, 1).toUpperCase());

        btnUsers.setEnabled(Session.isAdmin());

        refreshCounts();
    }

    private void open(Supplier<JFrame> maker) {
        final JFrame f = maker.get();
        f.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                refreshCounts();                 
                setVisible(true);                
                toFront();
            }
        });
        setVisible(false);
        f.setVisible(true);
    }

    private void refreshCounts() {
        try {
            lblTotal.setText(String.valueOf(dashboardDao.totalUnits()));
            lblOccupied.setText(String.valueOf(dashboardDao.occupiedUnits()));
            lblAvailable.setText(String.valueOf(dashboardDao.availableUnits()));
            lblContracts.setText(String.valueOf(dashboardDao.activeContracts()));
            BigDecimal rent = dashboardDao.rentCollected(YearMonth.now().toString());
            lblRent.setText(String.format("%,.2f", rent));
            lblMaint.setText(String.valueOf(dashboardDao.openMaintenance()));

            DefaultTableModel model = reportDao.unitOccupancy();
            model.setColumnIdentifiers(new String[]{"Unit", "Type", "Monthly Rent (PHP)", "Status", "Current Tenant"});
            tblUnits.setModel(model);
            TableStyle.apply(tblUnits);          // a new model resets the look, so apply it again
        } catch (SQLException ex) {
            ErrorHelper.show(this, ex);
        }
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlRoot = new javax.swing.JPanel();
        pnlSidebar = new javax.swing.JPanel();
        lblBrand = new javax.swing.JLabel();
        lblBrandSub = new javax.swing.JLabel();
        btnDashboard = new javax.swing.JButton();
        btnLogout = new javax.swing.JButton();
        pnlMain = new javax.swing.JPanel();
        pnlHeader = new javax.swing.JPanel();
        lblTitle = new javax.swing.JLabel();
        lblWelcome = new javax.swing.JLabel();
        lblDate = new javax.swing.JLabel();
        pnlUser = new javax.swing.JPanel();
        pnlAvatar = new javax.swing.JPanel();
        lblInitial = new javax.swing.JLabel();
        lblUserName = new javax.swing.JLabel();
        lblUserRole = new javax.swing.JLabel();
        pnlCards = new javax.swing.JPanel();
        pnlTotal = new javax.swing.JPanel();
        lblTotalTitle = new javax.swing.JLabel();
        lblTotal = new javax.swing.JLabel();
        pnlOccupied = new javax.swing.JPanel();
        lblOccupiedTitle = new javax.swing.JLabel();
        lblOccupied = new javax.swing.JLabel();
        pnlAvailable = new javax.swing.JPanel();
        lblAvailableTitle = new javax.swing.JLabel();
        lblAvailable = new javax.swing.JLabel();
        pnlContracts = new javax.swing.JPanel();
        lblContractsTitle = new javax.swing.JLabel();
        lblContracts = new javax.swing.JLabel();
        pnlRent = new javax.swing.JPanel();
        lblRentTitle = new javax.swing.JLabel();
        lblRent = new javax.swing.JLabel();
        pnlMaint = new javax.swing.JPanel();
        lblMaintTitle = new javax.swing.JLabel();
        lblMaint = new javax.swing.JLabel();
        lblModules = new javax.swing.JLabel();
        pnlModules = new javax.swing.JPanel();
        btnUnits = new javax.swing.JButton();
        btnTenants = new javax.swing.JButton();
        btnContracts = new javax.swing.JButton();
        btnPayments = new javax.swing.JButton();
        btnMaintenance = new javax.swing.JButton();
        btnUsers = new javax.swing.JButton();
        btnReports = new javax.swing.JButton();
        lblTable = new javax.swing.JLabel();
        scrlUnits = new javax.swing.JScrollPane();
        tblUnits = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Apartment Rental Management System - Dashboard");
        setPreferredSize(new java.awt.Dimension(1240, 840));

        pnlRoot.setBackground(new java.awt.Color(249, 241, 246));

        pnlSidebar.setBackground(new java.awt.Color(115, 28, 82));

        lblBrand.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        lblBrand.setForeground(new java.awt.Color(255, 255, 255));
        lblBrand.setText("Hotel de Pequilla");

        lblBrandSub.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblBrandSub.setForeground(new java.awt.Color(214, 150, 184));
        lblBrandSub.setText("Rental Management");

        btnDashboard.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnDashboard.setText("Dashboard");
        btnDashboard.addActionListener(this::btnDashboardActionPerformed);

        btnLogout.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnLogout.setText("Log out");
        btnLogout.addActionListener(this::btnLogoutActionPerformed);

        javax.swing.GroupLayout pnlSidebarLayout = new javax.swing.GroupLayout(pnlSidebar);
        pnlSidebar.setLayout(pnlSidebarLayout);
        pnlSidebarLayout.setHorizontalGroup(
            pnlSidebarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlSidebarLayout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addGroup(pnlSidebarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblBrand)
                    .addComponent(lblBrandSub)
                    .addComponent(btnDashboard, javax.swing.GroupLayout.DEFAULT_SIZE, 202, Short.MAX_VALUE)
                    .addComponent(btnLogout, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(24, 24, 24))
        );
        pnlSidebarLayout.setVerticalGroup(
            pnlSidebarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlSidebarLayout.createSequentialGroup()
                .addGap(36, 36, 36)
                .addComponent(lblBrand)
                .addGap(2, 2, 2)
                .addComponent(lblBrandSub)
                .addGap(40, 40, 40)
                .addComponent(btnDashboard, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnLogout, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(30, 30, 30))
        );

        pnlMain.setBackground(new java.awt.Color(249, 241, 246));

        pnlHeader.setBackground(new java.awt.Color(249, 241, 246));

        lblTitle.setFont(new java.awt.Font("Segoe UI", 1, 30)); // NOI18N
        lblTitle.setForeground(new java.awt.Color(140, 40, 100));
        lblTitle.setText("Dashboard");

        lblWelcome.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblWelcome.setForeground(new java.awt.Color(130, 110, 125));
        lblWelcome.setText("Welcome back");

        lblDate.setForeground(new java.awt.Color(130, 110, 125));
        lblDate.setText("Date");

        pnlUser.setBackground(new java.awt.Color(243, 222, 236));

        pnlAvatar.setBackground(new java.awt.Color(140, 40, 100));

        lblInitial.setFont(new java.awt.Font("Segoe UI", 1, 22)); // NOI18N
        lblInitial.setForeground(new java.awt.Color(255, 255, 255));
        lblInitial.setText("A");

        javax.swing.GroupLayout pnlAvatarLayout = new javax.swing.GroupLayout(pnlAvatar);
        pnlAvatar.setLayout(pnlAvatarLayout);
        pnlAvatarLayout.setHorizontalGroup(
            pnlAvatarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlAvatarLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(lblInitial)
                .addGap(20, 20, 20))
        );
        pnlAvatarLayout.setVerticalGroup(
            pnlAvatarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlAvatarLayout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addComponent(lblInitial)
                .addGap(10, 10, 10))
        );

        lblUserName.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        lblUserName.setForeground(new java.awt.Color(60, 40, 55));
        lblUserName.setText("Name");

        lblUserRole.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblUserRole.setForeground(new java.awt.Color(130, 110, 125));
        lblUserRole.setText("Role");

        javax.swing.GroupLayout pnlUserLayout = new javax.swing.GroupLayout(pnlUser);
        pnlUser.setLayout(pnlUserLayout);
        pnlUserLayout.setHorizontalGroup(
            pnlUserLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlUserLayout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addComponent(pnlAvatar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(14, 14, 14)
                .addGroup(pnlUserLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblUserName)
                    .addComponent(lblUserRole))
                .addGap(24, 24, 24))
        );
        pnlUserLayout.setVerticalGroup(
            pnlUserLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlUserLayout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addGroup(pnlUserLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(pnlAvatar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(pnlUserLayout.createSequentialGroup()
                        .addComponent(lblUserName)
                        .addGap(2, 2, 2)
                        .addComponent(lblUserRole)))
                .addGap(8, 8, 8))
        );

        javax.swing.GroupLayout pnlHeaderLayout = new javax.swing.GroupLayout(pnlHeader);
        pnlHeader.setLayout(pnlHeaderLayout);
        pnlHeaderLayout.setHorizontalGroup(
            pnlHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlHeaderLayout.createSequentialGroup()
                .addComponent(lblTitle)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(pnlUser, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addComponent(lblWelcome)
            .addComponent(lblDate)
        );
        pnlHeaderLayout.setVerticalGroup(
            pnlHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlHeaderLayout.createSequentialGroup()
                .addComponent(lblTitle)
                .addGap(4, 4, 4)
                .addComponent(lblWelcome)
                .addGap(2, 2, 2)
                .addComponent(lblDate))
            .addComponent(pnlUser, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pnlCards.setBackground(new java.awt.Color(249, 241, 246));

        pnlTotal.setBackground(new java.awt.Color(255, 255, 255));

        lblTotalTitle.setForeground(new java.awt.Color(130, 110, 125));
        lblTotalTitle.setText("Total Units");

        lblTotal.setFont(new java.awt.Font("Segoe UI", 1, 30)); // NOI18N
        lblTotal.setForeground(new java.awt.Color(140, 40, 100));
        lblTotal.setText("0");

        javax.swing.GroupLayout pnlTotalLayout = new javax.swing.GroupLayout(pnlTotal);
        pnlTotal.setLayout(pnlTotalLayout);
        pnlTotalLayout.setHorizontalGroup(
            pnlTotalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlTotalLayout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(pnlTotalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblTotalTitle)
                    .addComponent(lblTotal))
                .addGap(18, 18, 18))
        );
        pnlTotalLayout.setVerticalGroup(
            pnlTotalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlTotalLayout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(lblTotalTitle)
                .addGap(2, 2, 2)
                .addComponent(lblTotal)
                .addGap(12, 12, 12))
        );

        pnlOccupied.setBackground(new java.awt.Color(255, 255, 255));

        lblOccupiedTitle.setForeground(new java.awt.Color(130, 110, 125));
        lblOccupiedTitle.setText("Occupied Units");

        lblOccupied.setFont(new java.awt.Font("Segoe UI", 1, 30)); // NOI18N
        lblOccupied.setForeground(new java.awt.Color(190, 92, 142));
        lblOccupied.setText("0");

        javax.swing.GroupLayout pnlOccupiedLayout = new javax.swing.GroupLayout(pnlOccupied);
        pnlOccupied.setLayout(pnlOccupiedLayout);
        pnlOccupiedLayout.setHorizontalGroup(
            pnlOccupiedLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlOccupiedLayout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(pnlOccupiedLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblOccupiedTitle)
                    .addComponent(lblOccupied))
                .addGap(18, 18, 18))
        );
        pnlOccupiedLayout.setVerticalGroup(
            pnlOccupiedLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlOccupiedLayout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(lblOccupiedTitle)
                .addGap(2, 2, 2)
                .addComponent(lblOccupied)
                .addGap(12, 12, 12))
        );

        pnlAvailable.setBackground(new java.awt.Color(255, 255, 255));

        lblAvailableTitle.setForeground(new java.awt.Color(130, 110, 125));
        lblAvailableTitle.setText("Available Units");

        lblAvailable.setFont(new java.awt.Font("Segoe UI", 1, 30)); // NOI18N
        lblAvailable.setForeground(new java.awt.Color(40, 140, 80));
        lblAvailable.setText("0");

        javax.swing.GroupLayout pnlAvailableLayout = new javax.swing.GroupLayout(pnlAvailable);
        pnlAvailable.setLayout(pnlAvailableLayout);
        pnlAvailableLayout.setHorizontalGroup(
            pnlAvailableLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlAvailableLayout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(pnlAvailableLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblAvailableTitle)
                    .addComponent(lblAvailable))
                .addGap(18, 18, 18))
        );
        pnlAvailableLayout.setVerticalGroup(
            pnlAvailableLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlAvailableLayout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(lblAvailableTitle)
                .addGap(2, 2, 2)
                .addComponent(lblAvailable)
                .addGap(12, 12, 12))
        );

        pnlContracts.setBackground(new java.awt.Color(255, 255, 255));

        lblContractsTitle.setForeground(new java.awt.Color(130, 110, 125));
        lblContractsTitle.setText("Active Contracts");

        lblContracts.setFont(new java.awt.Font("Segoe UI", 1, 30)); // NOI18N
        lblContracts.setForeground(new java.awt.Color(140, 40, 100));
        lblContracts.setText("0");

        javax.swing.GroupLayout pnlContractsLayout = new javax.swing.GroupLayout(pnlContracts);
        pnlContracts.setLayout(pnlContractsLayout);
        pnlContractsLayout.setHorizontalGroup(
            pnlContractsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlContractsLayout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(pnlContractsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblContractsTitle)
                    .addComponent(lblContracts))
                .addGap(18, 18, 18))
        );
        pnlContractsLayout.setVerticalGroup(
            pnlContractsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlContractsLayout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(lblContractsTitle)
                .addGap(2, 2, 2)
                .addComponent(lblContracts)
                .addGap(12, 12, 12))
        );

        pnlRent.setBackground(new java.awt.Color(255, 255, 255));

        lblRentTitle.setForeground(new java.awt.Color(130, 110, 125));
        lblRentTitle.setText("Rent Collected This Month (PHP)");

        lblRent.setFont(new java.awt.Font("Segoe UI", 1, 30)); // NOI18N
        lblRent.setForeground(new java.awt.Color(190, 92, 142));
        lblRent.setText("0");

        javax.swing.GroupLayout pnlRentLayout = new javax.swing.GroupLayout(pnlRent);
        pnlRent.setLayout(pnlRentLayout);
        pnlRentLayout.setHorizontalGroup(
            pnlRentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlRentLayout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(pnlRentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblRentTitle)
                    .addComponent(lblRent))
                .addGap(18, 18, 18))
        );
        pnlRentLayout.setVerticalGroup(
            pnlRentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlRentLayout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(lblRentTitle)
                .addGap(2, 2, 2)
                .addComponent(lblRent)
                .addGap(12, 12, 12))
        );

        pnlMaint.setBackground(new java.awt.Color(255, 255, 255));

        lblMaintTitle.setForeground(new java.awt.Color(130, 110, 125));
        lblMaintTitle.setText("Open Maintenance Requests");

        lblMaint.setFont(new java.awt.Font("Segoe UI", 1, 30)); // NOI18N
        lblMaint.setForeground(new java.awt.Color(205, 120, 30));
        lblMaint.setText("0");

        javax.swing.GroupLayout pnlMaintLayout = new javax.swing.GroupLayout(pnlMaint);
        pnlMaint.setLayout(pnlMaintLayout);
        pnlMaintLayout.setHorizontalGroup(
            pnlMaintLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlMaintLayout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(pnlMaintLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblMaintTitle)
                    .addComponent(lblMaint))
                .addGap(18, 18, 18))
        );
        pnlMaintLayout.setVerticalGroup(
            pnlMaintLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlMaintLayout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(lblMaintTitle)
                .addGap(2, 2, 2)
                .addComponent(lblMaint)
                .addGap(12, 12, 12))
        );

        javax.swing.GroupLayout pnlCardsLayout = new javax.swing.GroupLayout(pnlCards);
        pnlCards.setLayout(pnlCardsLayout);
        pnlCardsLayout.setHorizontalGroup(
            pnlCardsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlCardsLayout.createSequentialGroup()
                .addGroup(pnlCardsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlTotal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlContracts, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(14, 14, 14)
                .addGroup(pnlCardsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlOccupied, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlRent, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(14, 14, 14)
                .addGroup(pnlCardsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlAvailable, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlMaint, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        pnlCardsLayout.setVerticalGroup(
            pnlCardsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlCardsLayout.createSequentialGroup()
                .addGroup(pnlCardsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlTotal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(pnlOccupied, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(pnlAvailable, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(14, 14, 14)
                .addGroup(pnlCardsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlContracts, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(pnlRent, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(pnlMaint, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        lblModules.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        lblModules.setForeground(new java.awt.Color(60, 40, 55));
        lblModules.setText("Menus");

        pnlModules.setBackground(new java.awt.Color(249, 241, 246));

        btnUnits.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnUnits.setText("Units");
        btnUnits.addActionListener(this::btnUnitsActionPerformed);

        btnTenants.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnTenants.setText("Tenants");
        btnTenants.addActionListener(this::btnTenantsActionPerformed);

        btnContracts.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnContracts.setText("Contracts");
        btnContracts.addActionListener(this::btnContractsActionPerformed);

        btnPayments.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnPayments.setText("Payments");
        btnPayments.addActionListener(this::btnPaymentsActionPerformed);

        btnMaintenance.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnMaintenance.setText("Maintenance");
        btnMaintenance.addActionListener(this::btnMaintenanceActionPerformed);

        btnUsers.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnUsers.setText("Users");
        btnUsers.addActionListener(this::btnUsersActionPerformed);

        btnReports.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnReports.setText("Reports");
        btnReports.addActionListener(this::btnReportsActionPerformed);

        javax.swing.GroupLayout pnlModulesLayout = new javax.swing.GroupLayout(pnlModules);
        pnlModules.setLayout(pnlModulesLayout);
        pnlModulesLayout.setHorizontalGroup(
            pnlModulesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlModulesLayout.createSequentialGroup()
                .addComponent(btnUnits, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(10, 10, 10)
                .addComponent(btnTenants, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(10, 10, 10)
                .addComponent(btnContracts, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(10, 10, 10)
                .addComponent(btnPayments, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(10, 10, 10)
                .addComponent(btnMaintenance, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(10, 10, 10)
                .addComponent(btnUsers, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(10, 10, 10)
                .addComponent(btnReports, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pnlModulesLayout.setVerticalGroup(
            pnlModulesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(btnUnits, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(btnTenants, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(btnContracts, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(btnPayments, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(btnMaintenance, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(btnUsers, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(btnReports, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        lblTable.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        lblTable.setForeground(new java.awt.Color(60, 40, 55));
        lblTable.setText("Unit occupancy");

        tblUnits.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Unit", "Type", "Monthly Rent (PHP)", "Status", "Current Tenant"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblUnits.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        scrlUnits.setViewportView(tblUnits);

        javax.swing.GroupLayout pnlMainLayout = new javax.swing.GroupLayout(pnlMain);
        pnlMain.setLayout(pnlMainLayout);
        pnlMainLayout.setHorizontalGroup(
            pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlMainLayout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlHeader, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlCards, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblModules)
                    .addComponent(pnlModules, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblTable)
                    .addComponent(scrlUnits))
                .addGap(28, 28, 28))
        );
        pnlMainLayout.setVerticalGroup(
            pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlMainLayout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addComponent(pnlHeader, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(pnlCards, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20)
                .addComponent(lblModules)
                .addGap(8, 8, 8)
                .addComponent(pnlModules, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20)
                .addComponent(lblTable)
                .addGap(8, 8, 8)
                .addComponent(scrlUnits, javax.swing.GroupLayout.DEFAULT_SIZE, 365, Short.MAX_VALUE)
                .addGap(24, 24, 24))
        );

        javax.swing.GroupLayout pnlRootLayout = new javax.swing.GroupLayout(pnlRoot);
        pnlRoot.setLayout(pnlRootLayout);
        pnlRootLayout.setHorizontalGroup(
            pnlRootLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlRootLayout.createSequentialGroup()
                .addComponent(pnlSidebar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(pnlMain, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pnlRootLayout.setVerticalGroup(
            pnlRootLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlSidebar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(pnlMain, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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

    private void btnDashboardActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDashboardActionPerformed
        refreshCounts();           
    }//GEN-LAST:event_btnDashboardActionPerformed

    private void btnLogoutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLogoutActionPerformed
        int ok = JOptionPane.showConfirmDialog(this, "Log out now?", "Logout", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;
        Session.logout();
        for (java.awt.Window w : java.awt.Window.getWindows()) w.dispose();   // closes every open window
        new LoginFrame().setVisible(true);
    }//GEN-LAST:event_btnLogoutActionPerformed

    private void btnUnitsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUnitsActionPerformed
        open(() -> new UnitFrame());
    }//GEN-LAST:event_btnUnitsActionPerformed

    private void btnTenantsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTenantsActionPerformed
        open(() -> new TenantFrame());
    }//GEN-LAST:event_btnTenantsActionPerformed

    private void btnContractsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnContractsActionPerformed
        open(() -> new ContractFrame());
    }//GEN-LAST:event_btnContractsActionPerformed

    private void btnPaymentsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPaymentsActionPerformed
        open(() -> new PaymentFrame());
    }//GEN-LAST:event_btnPaymentsActionPerformed

    private void btnMaintenanceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMaintenanceActionPerformed
        open(() -> new MaintenanceFrame());
    }//GEN-LAST:event_btnMaintenanceActionPerformed

    private void btnUsersActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUsersActionPerformed
        open(() -> new UserFrame());
    }//GEN-LAST:event_btnUsersActionPerformed

    private void btnReportsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnReportsActionPerformed
        open(() -> new ReportsFrame());
    }//GEN-LAST:event_btnReportsActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnContracts;
    private javax.swing.JButton btnDashboard;
    private javax.swing.JButton btnLogout;
    private javax.swing.JButton btnMaintenance;
    private javax.swing.JButton btnPayments;
    private javax.swing.JButton btnReports;
    private javax.swing.JButton btnTenants;
    private javax.swing.JButton btnUnits;
    private javax.swing.JButton btnUsers;
    private javax.swing.JLabel lblAvailable;
    private javax.swing.JLabel lblAvailableTitle;
    private javax.swing.JLabel lblBrand;
    private javax.swing.JLabel lblBrandSub;
    private javax.swing.JLabel lblContracts;
    private javax.swing.JLabel lblContractsTitle;
    private javax.swing.JLabel lblDate;
    private javax.swing.JLabel lblInitial;
    private javax.swing.JLabel lblMaint;
    private javax.swing.JLabel lblMaintTitle;
    private javax.swing.JLabel lblModules;
    private javax.swing.JLabel lblOccupied;
    private javax.swing.JLabel lblOccupiedTitle;
    private javax.swing.JLabel lblRent;
    private javax.swing.JLabel lblRentTitle;
    private javax.swing.JLabel lblTable;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JLabel lblTotal;
    private javax.swing.JLabel lblTotalTitle;
    private javax.swing.JLabel lblUserName;
    private javax.swing.JLabel lblUserRole;
    private javax.swing.JLabel lblWelcome;
    private javax.swing.JPanel pnlAvailable;
    private javax.swing.JPanel pnlAvatar;
    private javax.swing.JPanel pnlCards;
    private javax.swing.JPanel pnlContracts;
    private javax.swing.JPanel pnlHeader;
    private javax.swing.JPanel pnlMain;
    private javax.swing.JPanel pnlMaint;
    private javax.swing.JPanel pnlModules;
    private javax.swing.JPanel pnlOccupied;
    private javax.swing.JPanel pnlRent;
    private javax.swing.JPanel pnlRoot;
    private javax.swing.JPanel pnlSidebar;
    private javax.swing.JPanel pnlTotal;
    private javax.swing.JPanel pnlUser;
    private javax.swing.JScrollPane scrlUnits;
    private javax.swing.JTable tblUnits;
    // End of variables declaration//GEN-END:variables
}
