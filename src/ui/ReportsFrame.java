package ui;

import dao.ReportDAO;
import java.awt.Color;
import java.sql.SQLException;
import java.time.YearMonth;
import javax.swing.DefaultComboBoxModel;
import javax.swing.table.DefaultTableModel;
import util.ErrorHelper;


public class ReportsFrame extends javax.swing.JFrame {

    private final ReportDAO dao = new ReportDAO();
    private boolean loading = true;     

    private static final String[][] HEADERS = {
        {"Unit", "Type", "Monthly Rent (PHP)", "Status", "Current Tenant"},
        {"ID", "Tenant", "Contact No", "Email", "Active Unit"},
        {"Billing Month", "Payments", "Total Collected (PHP)"},
        {"Contract ID", "Tenant", "Unit", "Monthly Rent (PHP)"},
        {"ID", "Unit", "Tenant", "Description", "Priority", "Date Reported", "Status"},
        {"Contract ID", "Tenant", "Unit", "End Date"}
    };

    public ReportsFrame() {
        initComponents();
        setLocationRelativeTo(null);

        PillButtonUI.apply(btnBack, Theme.SIDEBAR, Color.WHITE);
        TableStyle.apply(tblReport);

        String[] months = new String[12];
        YearMonth ym = YearMonth.now();
        for (int i = 0; i < 12; i++) {
            months[i] = ym.minusMonths(i).toString();
        }
        cmbMonth.setModel(new DefaultComboBoxModel<>(months));
        showMonthBox(false);                  

        loading = false;
        generate();                           
    }

    private void showMonthBox(boolean show) {
        lblMonth.setVisible(show);
        cmbMonth.setVisible(show);
    }

    private void generate() {
        if (loading) return;
        int which = cmbReport.getSelectedIndex();
        String month = (String) cmbMonth.getSelectedItem();
        try {
            DefaultTableModel model = runReport(which, month);
            if (model.getColumnCount() == HEADERS[which].length) {
                model.setColumnIdentifiers(HEADERS[which]);
            }
            tblReport.setModel(model);
            TableStyle.apply(tblReport);                   
            int n = model.getRowCount();
            String title = (which == 3) ? "Overdue rent for " + month : String.valueOf(cmbReport.getSelectedItem());
            lblInfo.setText(title + ": " + (n == 0 ? "no records found." : n + " record(s)"));
        } catch (SQLException ex) {
            ErrorHelper.show(this, ex);
        }
    }

    private DefaultTableModel runReport(int which, String month) throws SQLException {
        switch (which) {
            case 0: return dao.unitOccupancy();
            case 1: return dao.tenantList();
            case 2: return dao.monthlyCollection();
            case 3: return dao.overdueRent(month);
            case 4: return dao.openMaintenance();
            default: return dao.expiringSoon();
        }
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlRoot = new javax.swing.JPanel();
        lblTitle = new javax.swing.JLabel();
        pnlControls = new javax.swing.JPanel();
        lblReport = new javax.swing.JLabel();
        cmbReport = new javax.swing.JComboBox<>();
        lblMonth = new javax.swing.JLabel();
        cmbMonth = new javax.swing.JComboBox<>();
        btnBack = new javax.swing.JButton();
        lblHint = new javax.swing.JLabel();
        scrlReport = new javax.swing.JScrollPane();
        tblReport = new javax.swing.JTable();
        lblInfo = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Reports");
        setPreferredSize(new java.awt.Dimension(1020, 700));

        pnlRoot.setBackground(new java.awt.Color(249, 241, 246));

        lblTitle.setFont(new java.awt.Font("Segoe UI", 1, 26)); // NOI18N
        lblTitle.setForeground(new java.awt.Color(140, 40, 100));
        lblTitle.setText("Reports");

        pnlControls.setBackground(new java.awt.Color(255, 255, 255));

        lblReport.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblReport.setForeground(new java.awt.Color(60, 40, 55));
        lblReport.setText("Report");

        cmbReport.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        cmbReport.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Unit occupancy", "Tenant list", "Monthly rent collection", "Overdue rent", "Open maintenance requests", "Contracts expiring in 30 days" }));
        cmbReport.addActionListener(this::cmbReportActionPerformed);

        lblMonth.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblMonth.setForeground(new java.awt.Color(60, 40, 55));
        lblMonth.setText("Billing month");

        cmbMonth.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        cmbMonth.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "2026-10" }));
        cmbMonth.addActionListener(this::cmbMonthActionPerformed);

        btnBack.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnBack.setText("Back to Dashboard");
        btnBack.addActionListener(this::btnBackActionPerformed);

        lblHint.setForeground(new java.awt.Color(130, 110, 125));
        lblHint.setText("Overdue rent lists active contracts that have no Paid payment for the chosen billing month.");

        javax.swing.GroupLayout pnlControlsLayout = new javax.swing.GroupLayout(pnlControls);
        pnlControls.setLayout(pnlControlsLayout);
        pnlControlsLayout.setHorizontalGroup(
            pnlControlsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlControlsLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(pnlControlsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlControlsLayout.createSequentialGroup()
                        .addComponent(lblReport)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cmbReport, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(24, 24, 24)
                        .addComponent(lblMonth)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cmbMonth, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnBack, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(16, 16, 16))
                    .addComponent(lblHint)))
        );
        pnlControlsLayout.setVerticalGroup(
            pnlControlsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlControlsLayout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(pnlControlsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblReport)
                    .addComponent(cmbReport, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblMonth)
                    .addComponent(cmbMonth, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnBack, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(8, 8, 8)
                .addComponent(lblHint)
                .addGap(12, 12, 12))
        );

        tblReport.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Result"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblReport.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        scrlReport.setViewportView(tblReport);

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
                    .addComponent(pnlControls, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(scrlReport)
                    .addComponent(lblInfo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(24, 24, 24))
        );
        pnlRootLayout.setVerticalGroup(
            pnlRootLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlRootLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(lblTitle)
                .addGap(34, 34, 34)
                .addComponent(pnlControls, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(scrlReport)
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

    private void cmbReportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbReportActionPerformed
        showMonthBox(cmbReport.getSelectedIndex() == 3);   
        generate();
    }//GEN-LAST:event_cmbReportActionPerformed

    private void cmbMonthActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbMonthActionPerformed
        generate();
    }//GEN-LAST:event_cmbMonthActionPerformed

    private void btnBackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBackActionPerformed
        dispose();     
    }//GEN-LAST:event_btnBackActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBack;
    private javax.swing.JComboBox<String> cmbMonth;
    private javax.swing.JComboBox<String> cmbReport;
    private javax.swing.JLabel lblHint;
    private javax.swing.JLabel lblInfo;
    private javax.swing.JLabel lblMonth;
    private javax.swing.JLabel lblReport;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JPanel pnlControls;
    private javax.swing.JPanel pnlRoot;
    private javax.swing.JScrollPane scrlReport;
    private javax.swing.JTable tblReport;
    // End of variables declaration//GEN-END:variables
}
