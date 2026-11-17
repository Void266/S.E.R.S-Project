package javamongo1.sersproject.ui;

import javamongo1.sersproject.model.User;
import javamongo1.sersproject.repository.UserRepository;
import org.mindrot.jbcrypt.BCrypt;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class UserManagementFrame extends JFrame {
    public JPanel createManagementPanel() { return new UserManagementPanel(); }

    private static final class UserManagementPanel extends JPanel {
        private final UserRepository repository = new UserRepository();
        private final List<User> allUsers = new ArrayList<>();
        private JTextField searchField;
        private JTable userTable;
        private DefaultTableModel tableModel;
        private JButton editButton, deleteButton;

        private UserManagementPanel() {
            super(new BorderLayout(12, 12));
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

            JLabel heading = new JLabel("USER MANAGEMENT");
            heading.setFont(new Font("Arial", Font.BOLD, 22));
            JPanel searchPanel = new JPanel(new BorderLayout(8, 0));
            searchPanel.setOpaque(false);
            searchPanel.add(new JLabel("Search by name, User ID, or Student ID:"), BorderLayout.WEST);
            searchField = new JTextField();
            RmmcTheme.styleField(searchField);
            searchPanel.add(searchField, BorderLayout.CENTER);
            JButton searchButton = new JButton("Search"), refreshButton = new JButton("Refresh");
            RmmcTheme.styleButton(searchButton, true);
            RmmcTheme.styleButton(refreshButton, false);
            JPanel searchActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            searchActions.setOpaque(false);
            searchActions.add(searchButton);
            searchActions.add(refreshButton);
            searchPanel.add(searchActions, BorderLayout.EAST);

            JPanel north = new JPanel(new BorderLayout(0, 12));
            north.setOpaque(false);
            north.add(heading, BorderLayout.NORTH);
            north.add(searchPanel, BorderLayout.CENTER);
            add(north, BorderLayout.NORTH);

            tableModel = new DefaultTableModel(new Object[]{"User ID", "Name", "Student ID", "Role"}, 0) {
                @Override public boolean isCellEditable(int row, int column) { return false; }
            };
            userTable = new JTable(tableModel);
            RmmcTheme.styleTable(userTable);
            userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            JScrollPane tableScroll = new JScrollPane(userTable);
            tableScroll.setOpaque(false);
            tableScroll.getViewport().setOpaque(false);
            add(tableScroll, BorderLayout.CENTER);

            editButton = new JButton("Edit");
            deleteButton = new JButton("Delete");
            RmmcTheme.styleButton(editButton, false);
            RmmcTheme.styleButton(deleteButton, true);
            JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            footer.setOpaque(false);
            footer.add(editButton);
            footer.add(deleteButton);
            add(footer, BorderLayout.SOUTH);

            searchButton.addActionListener(e -> applySearch());
            searchField.addActionListener(e -> applySearch());
            refreshButton.addActionListener(e -> { searchField.setText(""); loadUsers(); });
            editButton.addActionListener(e -> editSelectedUser());
            deleteButton.addActionListener(e -> deleteSelectedUser());
            loadUsers();
        }

        private void loadUsers() {
            setActionsEnabled(false);
            new SwingWorker<List<User>, Void>() {
                @Override protected List<User> doInBackground() { return repository.findAllUsers(); }
                @Override protected void done() {
                    setActionsEnabled(true);
                    try {
                        allUsers.clear();
                        allUsers.addAll(get());
                        applySearch();
                    } catch (Exception ex) {
                        showError("Could not load users. Check the MongoDB connection.");
                    }
                }
            }.execute();
        }

        private void setActionsEnabled(boolean enabled) {
            editButton.setEnabled(enabled);
            deleteButton.setEnabled(enabled);
        }

        private void applySearch() {
            String query = searchField.getText().trim().toLowerCase();
            tableModel.setRowCount(0);
            for (User user : allUsers) {
                String internalId = Integer.toString(user.getId());
                String name = safe(user.getName());
                String studentId = safe(user.getStudentId());
                if (query.isEmpty() || name.toLowerCase().contains(query)
                        || internalId.contains(query) || studentId.toLowerCase().contains(query)) {
                    tableModel.addRow(new Object[]{user.getId(), name, studentId, safe(user.getRole())});
                }
            }
        }

        private User getSelectedUser() {
            int row = userTable.getSelectedRow();
            if (row < 0) return null;
            int modelRow = userTable.convertRowIndexToModel(row);
            int userId = Integer.parseInt(tableModel.getValueAt(modelRow, 0).toString());
            for (User user : allUsers) if (user.getId() == userId) return user;
            return null;
        }

        private void editSelectedUser() {
            User user = getSelectedUser();
            if (user == null) { showInfo("Select a user to edit."); return; }

            JTextField nameField = new JTextField(safe(user.getName()), 22);
            JComboBox<String> roleBox = new JComboBox<>(new String[]{"ORGANIZER", "ADMIN"});
            roleBox.setSelectedItem("ADMIN".equalsIgnoreCase(user.getRole()) ? "ADMIN" : "ORGANIZER");
            JPasswordField passwordField = new JPasswordField(22);
            JPasswordField confirmField = new JPasswordField(22);
            JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
            form.add(new JLabel("Name:")); form.add(nameField);
            form.add(new JLabel("Role:")); form.add(roleBox);
            form.add(new JLabel("New password (optional):")); form.add(passwordField);
            form.add(new JLabel("Confirm new password:")); form.add(confirmField);

            int answer = JOptionPane.showConfirmDialog(this, form, "Edit User " + user.getId(), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            char[] password = passwordField.getPassword(), confirmation = confirmField.getPassword();
            if (answer != JOptionPane.OK_OPTION) { java.util.Arrays.fill(password, '\0'); java.util.Arrays.fill(confirmation, '\0'); return; }

            String name = nameField.getText().trim();
            if (name.isBlank() || !name.matches("[a-zA-Z ]+")) {
                java.util.Arrays.fill(password, '\0'); java.util.Arrays.fill(confirmation, '\0');
                showError("Enter a name using letters and spaces only."); return;
            }
            if (!java.util.Arrays.equals(password, confirmation)) {
                java.util.Arrays.fill(password, '\0'); java.util.Arrays.fill(confirmation, '\0');
                showError("The new passwords do not match."); return;
            }
            user.setName(name);
            user.setRole((String) roleBox.getSelectedItem());
            if (password.length > 0) {
                String plain = new String(password);
                if (!validPassword(plain)) {
                    java.util.Arrays.fill(password, '\0'); java.util.Arrays.fill(confirmation, '\0');
                    showError("Password must be at least 8 characters, start with an uppercase letter, and contain a number and special character."); return;
                }
                user.setPassword(BCrypt.hashpw(plain, BCrypt.gensalt()));
            }
            java.util.Arrays.fill(password, '\0'); java.util.Arrays.fill(confirmation, '\0');
            if (repository.updateUser(user.getId(), user)) { applySearch(); showInfo("User updated."); }
            else showError("Could not update this user.");
        }

        private boolean validPassword(String password) {
            return password.length() >= 8 && Character.isUpperCase(password.charAt(0))
                    && password.matches(".*\\d.*") && password.matches(".*[^a-zA-Z0-9].*");
        }

        private void deleteSelectedUser() {
            User user = getSelectedUser();
            if (user == null) { showInfo("Select a user to delete."); return; }
            int answer = JOptionPane.showConfirmDialog(this,
                    "Permanently delete the account for " + safe(user.getName()) + " (User ID " + user.getId() + ")?",
                    "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (answer == JOptionPane.YES_OPTION) {
                if (repository.deleteUser(user.getId())) { allUsers.remove(user); applySearch(); }
                else showError("Could not delete this user.");
            }
        }

        private String safe(String value) { return value == null ? "" : value; }
        private void showInfo(String text) { JOptionPane.showMessageDialog(this, text, "User Management", JOptionPane.INFORMATION_MESSAGE); }
        private void showError(String text) { JOptionPane.showMessageDialog(this, text, "User Management", JOptionPane.ERROR_MESSAGE); }
    }

    public UserManagementFrame() {
        setTitle("User Management - SERS");
        setSize(900, 560);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        RmmcTheme.styleFrame(this);
        setLayout(new BorderLayout());
        add(createManagementPanel(), BorderLayout.CENTER);
    }
}
