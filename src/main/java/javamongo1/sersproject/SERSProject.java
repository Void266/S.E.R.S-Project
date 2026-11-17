package javamongo1.sersproject;

import javamongo1.sersproject.ui.LoginFrame;
import javax.swing.SwingUtilities;
import javamongo1.sersproject.config.MongoConnection;
import javamongo1.sersproject.repository.UserRepository;
import javamongo1.sersproject.model.User;
import org.mindrot.jbcrypt.BCrypt;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JPasswordField;
import java.awt.GridLayout;

public class SERSProject {

    public static void main(String[] args) {
        
        SwingUtilities.invokeLater(() -> {
            if (!ensureDefaultAdmin()) {
                return;
            }
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }

    /** Creates the initial administrator interactively on a fresh database. */
    private static boolean ensureDefaultAdmin() {
        try {
            UserRepository users = new UserRepository();
            User existingAdmin = users.findUserByStudentId("ADMIN001");
            if (existingAdmin != null) {
                if ("ADMIN".equalsIgnoreCase(existingAdmin.getRole())) {
                    return true;
                }
                JOptionPane.showMessageDialog(null,
                        "Account ID ADMIN001 already exists but is not an admin. Contact your system administrator.",
                        "Admin Setup", JOptionPane.ERROR_MESSAGE);
                return false;
            }

            JTextField name = new JTextField("System Administrator");
            JPasswordField password = new JPasswordField();
            JPasswordField confirm = new JPasswordField();
            JPanel form = new JPanel(new GridLayout(0, 1, 4, 4));
            form.add(new JLabel("Create the default admin account (ID: ADMIN001)."));
            form.add(new JLabel("Admin name"));
            form.add(name);
            form.add(new JLabel("Choose a password (8+ chars, uppercase first, number and symbol)."));
            form.add(password);
            form.add(new JLabel("Confirm password"));
            form.add(confirm);

            while (true) {
                int result = JOptionPane.showConfirmDialog(null, form,
                        "Initial Administrator Setup", JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.PLAIN_MESSAGE);
                if (result != JOptionPane.OK_OPTION) {
                    return false;
                }
                String adminName = name.getText().trim();
                char[] pass = password.getPassword();
                char[] repeated = confirm.getPassword();
                String plain = new String(pass);
                boolean validName = !adminName.isBlank();
                boolean validPassword = plain.length() >= 8
                        && Character.isUpperCase(plain.charAt(0))
                        && plain.matches(".*\\d.*")
                        && plain.matches(".*[^a-zA-Z0-9].*");
                boolean matches = java.util.Arrays.equals(pass, repeated);
                java.util.Arrays.fill(pass, '\0');
                java.util.Arrays.fill(repeated, '\0');
                if (!validName || !validPassword || !matches) {
                    JOptionPane.showMessageDialog(null,
                            "Enter a name and a matching password that meets the listed requirements.",
                            "Admin Setup", JOptionPane.ERROR_MESSAGE);
                    password.setText("");
                    confirm.setText("");
                    continue;
                }
                User admin = new User("ADMIN001", adminName,
                        BCrypt.hashpw(plain, BCrypt.gensalt()), "ADMIN");
                if (users.createUser(admin)) {
                    JOptionPane.showMessageDialog(null,
                            "Default admin account created. Sign in with ID ADMIN001 and the password you chose.",
                            "Admin Setup Complete", JOptionPane.INFORMATION_MESSAGE);
                    return true;
                }
                JOptionPane.showMessageDialog(null,
                        "Could not create the admin account. Check the database connection and try again.",
                        "Admin Setup Failed", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null,
                    "Could not connect to MongoDB. Start MongoDB and try again.",
                    "Database Connection Failed", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
}
