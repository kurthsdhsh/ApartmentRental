package ui;

import dao.ReportDAO;
import java.awt.Color;
import java.sql.SQLException;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import util.ErrorHelper;
import util.Validator;


public class ReportsFrame extends javax.swing.JFrame {

    private final ReportDAO dao = new ReportDAO();

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

        PillButtonUI.apply(btnGenerate, Theme.PRIMARY, Color.WHITE);
        PillButtonUI.apply(btnBack, Theme.SIDEBAR, Color.WHITE);
        TableStyle.apply(tblReport);

        txtMonth.setText(YearMonth.now().toString());     
        txtMonth.setEnabled(false);                       
        generate();                                      
    }

    
    private void generate() {
        int which = cmbReport.getSelectedIndex();
        String month = txtMonth.getText().trim();
        if (which == 3) {                                 
            if (Validator.isEmpty(month)) {
                warn("Please enter the billing month (example 2026-10).");
                txtMonth.requestFocus();
                return;
            }
            try {
                month = YearMonth.parse(month).toString();
            } catch (DateTimeParseException ex) {
                warn("Billing month must look like yyyy-MM, for example 2026-10.");
                txtMonth.requestFocus();
                return;
            }
        }
        try {
            DefaultTableModel model = runReport(which, month);
            if (model.getColumnCount() == HEADERS[which].length) {
                model.setColumnIdentifiers(HEADERS[which]);
            }
            tblReport.setModel(model);
            TableStyle.apply(tblReport);                   
            int n = model.getRowCount();
            lblInfo.setText(cmbReport.getSelectedItem() + ": " + (n == 0 ? "no records found." : n + " record(s)"));
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

    private void warn(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Validation", JOptionPane.WARNING_MESSAGE);
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlRoot = new javax.swing.JPanel();
        lblTitle = new javax.swing.JLabel();
        pnlControls = new javax.swing.JPanel();
        lblReport = new javax.swing.JLabel();
        cmbReport = new javax.swing.JComboBox<>();
        lblMonth = new javax.swing.JLabel();
        txtMonth = new javax.swing.JTextField();
        btnGenerate = new javax.swing.JButton();
        btnBack = new javax.swing.JButton();
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
        lblMonth.setText("Billing month (yyyy-MM)");

        txtMonth.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        txtMonth.addActionListener(this::txtMonthActionPerformed);

        btnGenerate.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnGenerate.setText("Generate");
        btnGenerate.addActionListener(this::btnGenerateActionPerformed);

        btnBack.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnBack.setText("Back to Dashboard");
        btnBack.addActionListener(this::btnBackActionPerformed);

        javax.swing.GroupLayout pnlControlsLayout = new javax.swing.GroupLayout(pnlControls);
        pnlControls.setLayout(pnlControlsLayout);
        pnlControlsLayout.setHorizontalGroup(
            pnlControlsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlControlsLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(lblReport)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cmbReport, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(24, 24, 24)
                .addComponent(lblMonth)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtMonth, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16)
                .addComponent(btnGenerate, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnBack, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16))
        );
        pnlControlsLayout.setVerticalGroup(
            pnlControlsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlControlsLayout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(pnlControlsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblReport)
                    .addComponent(cmbReport, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblMonth)
                    .addComponent(txtMonth, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnGenerate, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnBack, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(36, 36, 36))
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
        txtMonth.setEnabled(cmbReport.getSelectedIndex() == 3);   
        generate();
    }//GEN-LAST:event_cmbReportActionPerformed

    private void txtMonthActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtMonthActionPerformed
        generate();     
    }//GEN-LAST:event_txtMonthActionPerformed

    private void btnGenerateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGenerateActionPerformed
        generate();
    }//GEN-LAST:event_btnGenerateActionPerformed

    private void btnBackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBackActionPerformed
        dispose();      
    }//GEN-LAST:event_btnBackActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBack;
    private javax.swing.JButton btnGenerate;
    private javax.swing.JComboBox<String> cmbReport;
    private javax.swing.JLabel lblInfo;
    private javax.swing.JLabel lblMonth;
    private javax.swing.JLabel lblReport;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JPanel pnlControls;
    private javax.swing.JPanel pnlRoot;
    private javax.swing.JScrollPane scrlReport;
    private javax.swing.JTable tblReport;
    private javax.swing.JTextField txtMonth;
    // End of variables declaration//GEN-END:variables
}
