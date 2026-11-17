package javamongo1.sersproject;

import javamongo1.sersproject.config.MongoConnection;
import javamongo1.sersproject.model.User;
import javamongo1.sersproject.repository.UserRepository;
import org.mindrot.jbcrypt.BCrypt;

import javax.swing.JPasswordField;
import javax.swing.JOptionPane;
import java.awt.GraphicsEnvironment;
import java.util.Arrays;

/** One-time utility for resetting the password of ADMIN001. */
public final class ResetAdminPassword {
    private static final String ADMIN_ACCOUNT_ID = "ADMIN001";

    private ResetAdminPassword() { }

    public static void main(String[] args) {
        if (GraphicsEnvironment.isHeadless()) {
            System.err.println("Run this utility in a desktop session so the password prompts can open.");
            return;
        }

        char[] password = promptPassword("Enter a new password for " + ADMIN_ACCOUNT_ID + ":");
        if (password == null) return;
        char[] confirmation = promptPassword("Enter the new password again:");
        if (confirmation == null) {
            Arrays.fill(password, '\0');
            return;
        }

        try {
            if (!Arrays.equals(password, confirmation)) {
                showError("The passwords do not match. No changes were made.");
                return;
            }
            String plainPassword = new String(password);
            if (!isValidPassword(plainPassword)) {
                showError("Password must be at least 8 characters, start with an uppercase letter, contain a number, and include a special character. No changes were made.");
                return;
            }

            UserRepository repository = new UserRepository();
            User admin = repository.findUserByStudentId(ADMIN_ACCOUNT_ID);
            if (admin == null || !"ADMIN".equalsIgnoreCase(admin.getRole())) {
                showError("Admin account " + ADMIN_ACCOUNT_ID + " was not found. No changes were made.");
                return;
            }

            admin.setPassword(BCrypt.hashpw(plainPassword, BCrypt.gensalt()));
            if (repository.updateUser(admin.getId(), admin)) {
                JOptionPane.showMessageDialog(null, "Password reset successfully for " + ADMIN_ACCOUNT_ID + ".", "Password Reset", JOptionPane.INFORMATION_MESSAGE);
            } else {
                showError("The password could not be updated. No changes were made.");
            }
        } catch (Exception ex) {
            showError("Could not connect to the database or update the password. No changes were made.");
        } finally {
            Arrays.fill(password, '\0');
            Arrays.fill(confirmation, '\0');
            MongoConnection.close();
        }
    }

    private static char[] promptPassword(String prompt) {
        JPasswordField field = new JPasswordField(24);
        int choice = JOptionPane.showConfirmDialog(null, field, prompt, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (choice != JOptionPane.OK_OPTION) {
            Arrays.fill(field.getPassword(), '\0');
            return null;
        }
        return field.getPassword();
    }

    private static boolean isValidPassword(String password) {
        return password != null
                && password.length() >= 8
                && Character.isUpperCase(password.charAt(0))
                && password.matches(".*\\d.*")
                && password.matches(".*[^a-zA-Z0-9].*");
    }

    private static void showError(String message) {
        JOptionPane.showMessageDialog(null, message, "Password Reset", JOptionPane.ERROR_MESSAGE);
    }
}
