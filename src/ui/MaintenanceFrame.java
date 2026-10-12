package ui;

import dao.MaintenanceDAO;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import javax.swing.*;
import static javax.swing.WindowConstants.DISPOSE_ON_CLOSE;
import javax.swing.table.DefaultTableModel;
import model.ComboItem;
import model.MaintenanceRequest;
import util.Constants;
import util.ErrorHelper;
import util.Session;
import util.Style;
import util.Validator;

/** Maintenance requests: Add, View, Search, Update, Delete (Admin only), Clear. */
public class MaintenanceFrame extends JFrame {

    private final MaintenanceDAO dao = new MaintenanceDAO();

    private final JTextField txtId = new JTextField(8);
    private final JComboBox<ComboItem> cmbUnit = new JComboBox<>();
    private final JComboBox<ComboItem> cmbTenant = new JComboBox<>();
    private final JTextArea txtDescription = new JTextArea(3, 30);
    private final JComboBox<String> cmbPriority =
            new JComboBox<>(new String[]{"Low", "Medium", "High", "Urgent"});
    private final JTextField txtDateReported = new JTextField(10);
    private final JComboBox<String> cmbStatus = new JComboBox<>(new String[]{
            Constants.MAINT_OPEN, Constants.MAINT_IN_PROGRESS, Constants.MAINT_RESOLVED, Constants.MAINT_CANCELLED});
    private final JTextField txtDateResolved = new JTextField(10);
    private final JTextField txtRepairCost = new JTextField(10);

    private final JButton btnSave = new JButton("Save");
    private final JButton btnUpdate = new JButton("Update");
    private final JButton btnDelete = new JButton("Delete");
    private final JButton btnClear = new JButton("Clear");

    private final JTextField txtSearch = new JTextField(20);
    private final JComboBox<String> cmbFilter = new JComboBox<>(new String[]{
            "All", Constants.MAINT_OPEN, Constants.MAINT_IN_PROGRESS, Constants.MAINT_RESOLVED,
            Constants.MAINT_CANCELLED});
    private final JButton btnSearch = new JButton("Search");
    private final JLabel lblInfo = new JLabel(" ");

    private final DefaultTableModel tableModel = new DefaultTableModel(new String[]{
            "ID", "Unit", "Tenant", "Description", "Priority", "Reported", "Status", "Resolved", "Cost"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable tblRequests = new JTable(tableModel);

    public MaintenanceFrame() {
        setTitle("Maintenance Requests");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);          // NEVER EXIT_ON_CLOSE
        buildLayout();
        setSize(1050, 720);
        setLocationRelativeTo(null);

        txtId.setEditable(false);
        btnDelete.setEnabled(Session.isAdmin());              // role check
        loadCombos();
        clearForm();
        refreshTable();
        wireEvents();
    }

    // ------------------------------------------------------------ layout
    private void buildLayout() {
        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        root.setBackground(Style.BG);

        JLabel title = new JLabel("Maintenance Requests");
        title.setFont(Style.TITLE);
        title.setForeground(Style.PRIMARY);
        root.add(title, BorderLayout.NORTH);

        // ---- form
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Style.BG);
        form.setBorder(BorderFactory.createTitledBorder("Request details"));
        txtDescription.setLineWrap(true);
        txtDescription.setWrapStyleWord(true);
        cmbUnit.setPreferredSize(new Dimension(180, 26));
        cmbTenant.setPreferredSize(new Dimension(180, 26));

        addRow(form, 0, 0, "ID", txtId);
        addRow(form, 0, 2, "Priority", cmbPriority);
        addRow(form, 1, 0, "Unit *", cmbUnit);
        addRow(form, 1, 2, "Tenant", cmbTenant);
        addRow(form, 2, 0, "Date reported (yyyy-MM-dd)", txtDateReported);
        addRow(form, 2, 2, "Status", cmbStatus);
        addRow(form, 3, 0, "Date resolved (yyyy-MM-dd)", txtDateResolved);
        addRow(form, 3, 2, "Repair cost", txtRepairCost);
        addRow(form, 4, 0, "Description * (max 500)", new JScrollPane(txtDescription));

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 4; gc.gridy = 0; gc.gridheight = 5; gc.insets = new Insets(4, 12, 4, 4);
        JPanel buttons = new JPanel(new java.awt.GridLayout(4, 1, 0, 8));
        buttons.setOpaque(false);
        for (JButton b : new JButton[]{btnSave, btnUpdate, btnDelete, btnClear}) {
            Style.styleButton(b);
            buttons.add(b);
        }
        btnDelete.setBackground(Style.DANGER);
        form.add(buttons, gc);

        // ---- search + table
        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        searchBar.setOpaque(false);
        searchBar.add(new JLabel("Search (unit, tenant, description):"));
        searchBar.add(txtSearch);
        searchBar.add(new JLabel("Status:"));
        searchBar.add(cmbFilter);
        Style.styleButton(btnSearch);
        searchBar.add(btnSearch);

        tblRequests.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblRequests.setRowHeight(24);
        tblRequests.getColumnModel().getColumn(0).setPreferredWidth(40);
        tblRequests.getColumnModel().getColumn(3).setPreferredWidth(260);

        JPanel tablePanel = new JPanel(new BorderLayout(4, 4));
        tablePanel.setOpaque(false);
        tablePanel.add(searchBar, BorderLayout.NORTH);
        tablePanel.add(new JScrollPane(tblRequests), BorderLayout.CENTER);
        tablePanel.add(lblInfo, BorderLayout.SOUTH);

        JPanel center = new JPanel(new BorderLayout(8, 8));
        center.setOpaque(false);
        center.add(form, BorderLayout.NORTH);
        center.add(tablePanel, BorderLayout.CENTER);
        root.add(center, BorderLayout.CENTER);

        setContentPane(root);
    }

    private void addRow(JPanel p, int row, int col, String label, java.awt.Component field) {
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 4, 4, 4);
        gc.anchor = GridBagConstraints.WEST;
        gc.gridx = col; gc.gridy = row;
        JLabel l = new JLabel(label);
        l.setFont(Style.LABEL);
        p.add(l, gc);
        gc.gridx = col + 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;
        p.add(field, gc);
    }

    private void wireEvents() {
        btnSave.addActionListener(e -> save());
        btnUpdate.addActionListener(e -> update());
        btnDelete.addActionListener(e -> delete());
        btnClear.addActionListener(e -> clearForm());
        btnSearch.addActionListener(e -> search());
        txtSearch.addActionListener(e -> search());          // Enter key searches too
        tblRequests.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) loadSelectedRow();
        });
    }

    // ------------------------------------------------------------ data loading
    private void loadCombos() {
        try {
            cmbUnit.removeAllItems();
            for (ComboItem i : dao.getUnitItems()) cmbUnit.addItem(i);
            cmbTenant.removeAllItems();
            cmbTenant.addItem(new ComboItem(0, "-- None --"));
            for (ComboItem i : dao.getTenantItems()) cmbTenant.addItem(i);
        } catch (SQLException ex) {
            ErrorHelper.show(this, ex);
        }
    }

    private void refreshTable() {
        try {
            fillTable(dao.getAll());
            lblInfo.setText(" ");
        } catch (SQLException ex) {
            ErrorHelper.show(this, ex);
        }
    }

    private void fillTable(List<MaintenanceRequest> list) {
        tableModel.setRowCount(0);                            // clear old rows
        for (MaintenanceRequest r : list) {
            tableModel.addRow(new Object[]{
                r.getRequestId(), r.getUnitNo(), r.getTenantName(), r.getDescription(), r.getPriority(),
                r.getDateReported(), r.getStatus(),
                r.getDateResolved() == null ? "" : r.getDateResolved().toString(),
                String.format("%,.2f", r.getRepairCost())});
        }
        lblInfo.setText(list.isEmpty() ? "No records found." : list.size() + " record(s).");
    }

    /** Row click: load the full record from the DB into the form. */
    private void loadSelectedRow() {
        int row = tblRequests.getSelectedRow();
        if (row < 0) return;
        int id = (Integer) tableModel.getValueAt(row, 0);
        try {
            MaintenanceRequest r = dao.getById(id);
            if (r == null) return;
            txtId.setText(String.valueOf(r.getRequestId()));
            selectCombo(cmbUnit, r.getUnitId());
            selectCombo(cmbTenant, r.getTenantId());
            txtDescription.setText(r.getDescription());
            cmbPriority.setSelectedItem(r.getPriority());
            txtDateReported.setText(r.getDateReported().toString());
            cmbStatus.setSelectedItem(r.getStatus());
            txtDateResolved.setText(r.getDateResolved() == null ? "" : r.getDateResolved().toString());
            txtRepairCost.setText(r.getRepairCost().toPlainString());
        } catch (SQLException ex) {
            ErrorHelper.show(this, ex);
        }
    }

    private void selectCombo(JComboBox<ComboItem> cmb, int id) {
        for (int i = 0; i < cmb.getItemCount(); i++) {
            if (cmb.getItemAt(i).getId() == id) { cmb.setSelectedIndex(i); return; }
        }
        if (cmb.getItemCount() > 0) cmb.setSelectedIndex(0);
    }

    // ------------------------------------------------------------ button actions
    private void save() {
        MaintenanceRequest r = readForm();
        if (r == null) return;
        try {
            dao.add(r);
            JOptionPane.showMessageDialog(this, "Maintenance request saved successfully.");
            clearForm();
            refreshTable();
        } catch (SQLException ex) {
            ErrorHelper.show(this, ex);
        }
    }

    private void update() {
        if (Validator.isEmpty(txtId.getText())) { warn("Select a record from the table first."); return; }
        MaintenanceRequest r = readForm();
        if (r == null) return;
        r.setRequestId(Integer.parseInt(txtId.getText()));
        try {
            dao.update(r);
            JOptionPane.showMessageDialog(this, "Maintenance request updated successfully.");
            clearForm();
            refreshTable();
        } catch (SQLException ex) {
            ErrorHelper.show(this, ex);
        }
    }

    private void delete() {
        if (!Session.isAdmin()) { warn("Only an Admin can delete records."); return; }
        if (Validator.isEmpty(txtId.getText())) { warn("Select a record from the table first."); return; }
        int ok = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this record?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;
        try {
            dao.delete(Integer.parseInt(txtId.getText()));
            JOptionPane.showMessageDialog(this, "Record deleted.");
            clearForm();
            refreshTable();
        } catch (SQLException ex) {
            ErrorHelper.show(this, ex);
        }
    }

    private void search() {
        try {
            fillTable(dao.search(txtSearch.getText(), (String) cmbFilter.getSelectedItem()));
        } catch (SQLException ex) {
            ErrorHelper.show(this, ex);
        }
    }

    private void clearForm() {
        tblRequests.clearSelection();
        txtId.setText("");
        if (cmbUnit.getItemCount() > 0) cmbUnit.setSelectedIndex(0);
        if (cmbTenant.getItemCount() > 0) cmbTenant.setSelectedIndex(0);
        txtDescription.setText("");
        cmbPriority.setSelectedItem("Medium");
        txtDateReported.setText(LocalDate.now().toString());
        cmbStatus.setSelectedItem(Constants.MAINT_OPEN);
        txtDateResolved.setText("");
        txtRepairCost.setText("0");
    }

    // ------------------------------------------------------------ validation (returns null if invalid)
    private MaintenanceRequest readForm() {
        ComboItem unit = (ComboItem) cmbUnit.getSelectedItem();
        if (unit == null) { warn("Unit is required. Add a unit first."); return null; }

        String desc = txtDescription.getText();
        if (Validator.isEmpty(desc)) { warn("Description is required."); return null; }
        if (desc.trim().length() > 500) { warn("Description is too long (maximum 500 characters)."); return null; }

        LocalDate reported;
        try {
            reported = Validator.parseDate(txtDateReported.getText());
        } catch (DateTimeParseException ex) {
            warn("Date reported must be a valid date in yyyy-MM-dd format (example 2026-10-07).");
            return null;
        }

        String status = (String) cmbStatus.getSelectedItem();
        LocalDate resolved = null;
        if (!Validator.isEmpty(txtDateResolved.getText())) {
            try {
                resolved = Validator.parseDate(txtDateResolved.getText());
            } catch (DateTimeParseException ex) {
                warn("Date resolved must be a valid date in yyyy-MM-dd format.");
                return null;
            }
        } else if (Constants.MAINT_RESOLVED.equals(status)) {
            resolved = LocalDate.now();                      // auto-fill today
            txtDateResolved.setText(resolved.toString());
        }
        if (resolved != null && resolved.isBefore(reported)) {
            warn("Date resolved cannot be before the date reported.");
            return null;
        }

        BigDecimal cost;
        if (Validator.isEmpty(txtRepairCost.getText())) {
            cost = BigDecimal.ZERO;
        } else {
            try {
                cost = Validator.parseNonNegative(txtRepairCost.getText());
            } catch (NumberFormatException ex) {
                warn("Repair cost must be a number, 0 or more.");
                return null;
            }
        }

        MaintenanceRequest r = new MaintenanceRequest();
        r.setUnitId(unit.getId());
        r.setTenantId(((ComboItem) cmbTenant.getSelectedItem()).getId());   // 0 = none -> NULL in DAO
        r.setDescription(desc.trim());
        r.setPriority((String) cmbPriority.getSelectedItem());
        r.setDateReported(reported);
        r.setStatus(status);
        r.setDateResolved(resolved);
        r.setRepairCost(cost);
        return r;
    }

    private void warn(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Validation", JOptionPane.WARNING_MESSAGE);
    }
}
