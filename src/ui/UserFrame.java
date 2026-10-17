package ui;

import dao.UserDAO;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import model.User;
import util.Constants;
import util.ErrorHelper;
import util.Session;
import util.Validator;

public class UserFrame extends JFrame {

    private static final String STATUS_ACTIVE = "Active";
    private static final String STATUS_INACTIVE = "Inactive";


    private final UserDAO dao = new UserDAO();

    private final JTextField txtUserId = new JTextField(6);
    private final JTextField txtUsername = new JTextField(18);
    private final JPasswordField txtPassword = new JPasswordField(18);
    private final JPasswordField txtConfirm = new JPasswordField(18);
    private final JTextField txtFullName = new JTextField(18);
    private final JComboBox<String> cmbRole =
            new JComboBox<>(new String[]{Constants.ROLE_ADMIN, Constants.ROLE_STAFF});
    private final JComboBox<String> cmbStatus =
            new JComboBox<>(new String[]{STATUS_ACTIVE, STATUS_INACTIVE});

    private final JButton btnSave = new JButton("Save");
    private final JButton btnUpdate = new JButton("Update");
    private final JButton btnDelete = new JButton("Delete");
    private final JButton btnClear = new JButton("Clear");

    private final JTextField txtSearch = new JTextField(22);
    private final JButton btnSearch = new JButton("Search");
    private final JLabel lblInfo = new JLabel(" ");

    private final DefaultTableModel tableModel =
            new DefaultTableModel(new String[]{"ID", "Username", "Full Name", "Role", "Status"}, 0) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };
    private final JTable tblUsers = new JTable(tableModel);

    public UserFrame() {
        setTitle("User Management");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);          

        buildLayout();
        setSize(1020, 720);
        setMinimumSize(new Dimension(900, 620));
        setLocationRelativeTo(null);

        txtUserId.setEditable(false);                         
        wireEvents();

        // only an Admin may change users
        boolean admin = Session.isAdmin();
        btnDelete.setEnabled(admin);
        if (!admin) {
            btnSave.setEnabled(false);
            btnUpdate.setEnabled(false);
        }

        clearForm();
        loadTable("");
        if (!admin) lblInfo.setText("Only an Admin can manage users.");
    }

    private void buildLayout() {
        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setBackground(Theme.PAGE_BG);
        root.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        JLabel title = new JLabel("User Management");
        title.setFont(Theme.TITLE);
        title.setForeground(Theme.PRIMARY_DARK);
        JLabel sub = new JLabel("Create accounts, choose a role (Admin or Staff) and activate or deactivate users");
        sub.setFont(Theme.BODY);
        sub.setForeground(Theme.MUTED);
        JPanel head = new JPanel();
        head.setOpaque(false);
        head.setLayout(new BoxLayout(head, BoxLayout.Y_AXIS));
        head.add(title);
        head.add(sub);
        root.add(head, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(0, 14));
        center.setOpaque(false);
        center.add(buildForm(), BorderLayout.NORTH);
        center.add(buildTablePanel(), BorderLayout.CENTER);
        root.add(center, BorderLayout.CENTER);
        setContentPane(root);
    }

    private JPanel buildForm() {
        RoundedPanel form = new RoundedPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        for (JComponent c : new JComponent[]{txtUserId, txtUsername, txtPassword, txtConfirm,
                                             txtFullName, cmbRole, cmbStatus}) {
            c.setFont(Theme.BODY);
            c.setPreferredSize(new Dimension(220, 32));
            c.setMinimumSize(new Dimension(140, 32));
            if (c instanceof JTextField) {                    // JPasswordField is a JTextField too
                c.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(215, 190, 205)),
                        BorderFactory.createEmptyBorder(3, 8, 3, 8)));
            }
        }
        txtUserId.setBackground(new Color(243, 238, 241));    // grey = read-only

        addField(form, 0, 0, "User ID", txtUserId);
        addField(form, 0, 1, "Username *", txtUsername);
        addField(form, 1, 0, "Password *", txtPassword);
        addField(form, 1, 1, "Confirm password *", txtConfirm);
        addField(form, 2, 0, "Full name *", txtFullName);
        addField(form, 2, 1, "Role", cmbRole);
        addField(form, 3, 1, "Status", cmbStatus);

        JLabel hint = new JLabel("Password: at least 6 characters. When updating, leave both password boxes "
                + "empty to keep the current password.");
        hint.setFont(Theme.SMALL);
        hint.setForeground(Theme.MUTED);
        GridBagConstraints gh = new GridBagConstraints();
        gh.gridx = 0; gh.gridy = 4; gh.gridwidth = 4; gh.anchor = GridBagConstraints.WEST;   // its own row, full width
        gh.insets = new Insets(8, 6, 0, 6);
        form.add(hint, gh);

        // the four buttons in a column on the right
        JPanel buttons = new JPanel(new GridLayout(4, 1, 0, 10));
        buttons.setOpaque(false);
        pill(btnSave, Theme.PRIMARY);
        pill(btnUpdate, Theme.PRIMARY_DARK);
        pill(btnDelete, Theme.DANGER);
        pill(btnClear, new Color(160, 160, 160));
        buttons.add(btnSave);
        buttons.add(btnUpdate);
        buttons.add(btnDelete);
        buttons.add(btnClear);
        GridBagConstraints gb = new GridBagConstraints();
        gb.gridx = 4; gb.gridy = 0; gb.gridheight = 5; gb.insets = new Insets(6, 18, 6, 6);
        gb.anchor = GridBagConstraints.NORTH;
        form.add(buttons, gb);
        return form;
    }

    private JPanel buildTablePanel() {
        RoundedPanel p = new RoundedPanel(new BorderLayout(0, 10));
        p.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        bar.setOpaque(false);
        JLabel l = new JLabel("Search (username or full name):");
        l.setFont(Theme.BOLD);
        l.setForeground(Theme.TEXT);
        txtSearch.setFont(Theme.BODY);
        txtSearch.setPreferredSize(new Dimension(240, 32));
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(215, 190, 205)),
                BorderFactory.createEmptyBorder(3, 8, 3, 8)));
        pill(btnSearch, Theme.PRIMARY);
        btnSearch.setPreferredSize(new Dimension(100, 32));
        bar.add(l);
        bar.add(txtSearch);
        bar.add(btnSearch);
        p.add(bar, BorderLayout.NORTH);

        tblUsers.setRowHeight(32);
        tblUsers.setShowGrid(false);
        tblUsers.setIntercellSpacing(new Dimension(0, 0));
        tblUsers.setFillsViewportHeight(true);
        tblUsers.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblUsers.getTableHeader().setReorderingAllowed(false);
        tblUsers.getTableHeader().setDefaultRenderer(new HeaderRenderer());
        tblUsers.getTableHeader().setPreferredSize(new Dimension(0, 34));
        CellRenderer cell = new CellRenderer();
        for (int i = 0; i < tblUsers.getColumnCount(); i++) {
            tblUsers.getColumnModel().getColumn(i).setCellRenderer(cell);
        }
        tblUsers.getColumnModel().getColumn(0).setMaxWidth(70);

        JScrollPane scroll = new JScrollPane(tblUsers);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setViewportBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Theme.CARD);
        p.add(scroll, BorderLayout.CENTER);

        lblInfo.setFont(Theme.SMALL);
        lblInfo.setForeground(Theme.MUTED);
        p.add(lblInfo, BorderLayout.SOUTH);
        return p;
    }

    private void addField(JPanel p, int row, int col, String label, JComponent field) {
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridy = row;
        gc.insets = new Insets(6, 6, 6, 6);
        gc.anchor = GridBagConstraints.WEST;
        gc.gridx = col * 2;
        JLabel l = new JLabel(label);
        l.setFont(Theme.BOLD);
        l.setForeground(Theme.TEXT);
        p.add(l, gc);
        gc.gridx = col * 2 + 1;
        gc.fill = GridBagConstraints.HORIZONTAL;      // the box stretches with the window
        gc.weightx = 1;
        p.add(field, gc);
    }

    private void pill(JButton b, Color color) {
        b.setFont(Theme.BOLD);
        b.setPreferredSize(new Dimension(120, 36));
        PillButtonUI.apply(b, color, Color.WHITE);
    }

    private void wireEvents() {
        btnSave.addActionListener(e -> save());
        btnUpdate.addActionListener(e -> update());
        btnDelete.addActionListener(e -> delete());
        btnClear.addActionListener(e -> clearForm());
        btnSearch.addActionListener(e -> loadTable(txtSearch.getText()));
        txtSearch.addActionListener(e -> loadTable(txtSearch.getText()));      // Enter key searches
        tblUsers.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) fillFormFromSelection();             // click a row -> fill the form
        });
    }


    private void save() {
        if (!txtUserId.getText().isEmpty()) {
            warn("A user is selected. Press Clear first to add a new user, or press Update to change this one.");
            return;
        }
        User u = readForm(true);                      // null = a warning was already shown
        if (u == null) return;
        try {
            dao.add(u);
            info("User saved successfully.");
            clearForm();
            loadTable(txtSearch.getText());
        } catch (SQLException ex) {
            showSqlError(ex);
        }
    }

    private void update() {
        if (txtUserId.getText().isEmpty()) {
            warn("Select a user from the table first.");
            return;
        }
        User u = readForm(false);                     // password may stay empty = keep the old one
        if (u == null) return;
        u.setUserId(Integer.parseInt(txtUserId.getText()));

        User me = Session.getCurrentUser();
        boolean isMe = me != null && me.getUserId() == u.getUserId();
        if (isMe && STATUS_INACTIVE.equals(u.getStatus())) {
            warn("You cannot deactivate your own account.");
            return;
        }
        if (isMe && !Constants.ROLE_ADMIN.equals(u.getRole())) {
            warn("You cannot remove your own Admin role.");
            return;
        }
        try {
            dao.update(u);
            if (isMe) {                               // keep the "Welcome..." info in sync
                me.setUsername(u.getUsername());
                me.setFullName(u.getFullName());
            }
            info("User updated successfully.");
            clearForm();
            loadTable(txtSearch.getText());
        } catch (SQLException ex) {
            showSqlError(ex);
        }
    }

    private void delete() {
        if (txtUserId.getText().isEmpty()) {
            warn("Select a record first.");
            return;
        }
        int id = Integer.parseInt(txtUserId.getText());
        User me = Session.getCurrentUser();
        if (me != null && me.getUserId() == id) {
            warn("You cannot delete your own account.");
            return;
        }
        try {
            User target = dao.getById(id);
            if (target == null) {
                warn("This user no longer exists.");
                clearForm();
                loadTable(txtSearch.getText());
                return;
            }
            if (Constants.ROLE_ADMIN.equals(target.getRole()) && dao.countAdmins() <= 1) {
                warn("You cannot delete the last Admin.");
                return;
            }
            int ok = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to delete the user \"" + target.getUsername() + "\"?",
                    "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (ok != JOptionPane.YES_OPTION) return;
            dao.delete(id);
            info("User deleted.");
            clearForm();
            loadTable(txtSearch.getText());
        } catch (SQLException ex) {
            if ("23503".equals(ex.getSQLState())) {   // foreign key: this user processed payments
                warn("This user has processed payments, so the account cannot be deleted.\n"
                        + "Set the status to Inactive instead.");
            } else {
                ErrorHelper.show(this, ex);
            }
        }
    }

    private void clearForm() {
        txtUserId.setText("");
        txtUsername.setText("");
        txtPassword.setText("");
        txtConfirm.setText("");
        txtFullName.setText("");
        cmbRole.setSelectedItem(Constants.ROLE_STAFF);
        cmbStatus.setSelectedItem(STATUS_ACTIVE);
        tblUsers.clearSelection();
        txtUsername.requestFocus();
    }

    private void loadTable(String keyword) {
        try {
            List<User> list = dao.search(keyword);
            tableModel.setRowCount(0);
            for (User u : list) {
                tableModel.addRow(new Object[]{u.getUserId(), u.getUsername(), u.getFullName(),
                        u.getRole(), u.getStatus()});
            }
            lblInfo.setText(list.isEmpty() ? "No records found." : list.size() + " user(s)");
        } catch (SQLException ex) {
            ErrorHelper.show(this, ex);
        }
    }

    private void fillFormFromSelection() {
        int r = tblUsers.getSelectedRow();
        if (r < 0) return;
        txtUserId.setText(String.valueOf(tableModel.getValueAt(r, 0)));
        txtUsername.setText(String.valueOf(tableModel.getValueAt(r, 1)));
        txtFullName.setText(String.valueOf(tableModel.getValueAt(r, 2)));
        cmbRole.setSelectedItem(tableModel.getValueAt(r, 3));
        cmbStatus.setSelectedItem(tableModel.getValueAt(r, 4));
        txtPassword.setText("");                       
        txtConfirm.setText("");
    }



    /**
     * Reads the form and checks every rule. Returns the User, or null after showing a warning.
     * @param passwordRequired true for Save (new user), false for Update (blank = keep old password)
     */
    private User readForm(boolean passwordRequired) {
        String username = txtUsername.getText().trim();
        String fullName = txtFullName.getText().trim();
        String pw = new String(txtPassword.getPassword());      
        String confirm = new String(txtConfirm.getPassword());

        if (Validator.isEmpty(username)) {
            warn("Username is required.");
            txtUsername.requestFocus();
            return null;
        }
        if (username.length() < 3 || username.length() > 50 || username.contains(" ")) {
            warn("Username must be 3 to 50 characters with no spaces.");
            txtUsername.requestFocus();
            return null;
        }
        if (Validator.isEmpty(fullName)) {
            warn("Full name is required.");
            txtFullName.requestFocus();
            return null;
        }
        if (fullName.length() > 100) {
            warn("Full name must be 100 characters or less.");
            txtFullName.requestFocus();
            return null;
        }
        if (passwordRequired || !pw.isEmpty() || !confirm.isEmpty()) {
            if (pw.length() < 6) {
                warn("Password must be at least 6 characters.");
                txtPassword.requestFocus();
                return null;
            }
            if (!pw.equals(confirm)) {
                warn("Password and Confirm password do not match.");
                txtConfirm.requestFocus();
                return null;
            }
        }
        User u = new User();
        u.setUsername(username);
        u.setFullName(fullName);
        u.setPassword(pw);                             
        u.setRole((String) cmbRole.getSelectedItem());
        u.setStatus((String) cmbStatus.getSelectedItem());
        return u;
    }

    private void showSqlError(SQLException ex) {
        if ("23505".equals(ex.getSQLState())) {        
            warn("Username already exists. Please choose another one.");
            txtUsername.requestFocus();
        } else {
            ErrorHelper.show(this, ex);
        }
    }

    private void warn(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Validation", JOptionPane.WARNING_MESSAGE);
    }

    private void info(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Information", JOptionPane.INFORMATION_MESSAGE);
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
            g2.fillRoundRect(2, 3, getWidth() - 4, getHeight() - 4, 22, 22);
            g2.setColor(Theme.CARD);
            g2.fillRoundRect(0, 0, getWidth() - 3, getHeight() - 4, 22, 22);
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
            super.getTableCellRendererComponent(t, v, sel, false, row, col);
            setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
            setFont(Theme.BODY);
            setHorizontalAlignment(LEFT);
            if (sel) {
                setBackground(Theme.PRIMARY);
                setForeground(Color.WHITE);
            } else {
                setBackground(row % 2 == 0 ? Theme.CARD : Theme.ROW_ALT);
                setForeground(Theme.TEXT);
                String s = v == null ? "" : v.toString();
                if (col == 3 && Constants.ROLE_ADMIN.equals(s)) {
                    setFont(Theme.BOLD);
                    setForeground(Theme.PRIMARY_DARK);
                }
                if (col == 4) {
                    setFont(Theme.BOLD);
                    setForeground(STATUS_ACTIVE.equals(s) ? Theme.SUCCESS : Theme.DANGER);
                }
            }
            return this;
        }
    }
}