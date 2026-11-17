package javamongo1.sersproject.service;

import javamongo1.sersproject.model.User;
import javamongo1.sersproject.repository.UserRepository;
import javax.swing.JOptionPane;
import org.mindrot.jbcrypt.BCrypt;

public class UserService {

    private final UserRepository userRepository;

    public UserService() {
        this.userRepository = new UserRepository();
    }

    // -------- Account Creation --------
    public User createUser(String organizerId, String name, String password) {

       if (!nameValidation(name)) {
            return null;
        }

        if (!passwordValidation(password)) {
            return null;
        }
        if (!organizerIdValidation(organizerId)) {
            return null;
        }

        String hashedPassword = BCrypt.hashpw(
                password,
                BCrypt.gensalt()
        );

        // New accounts receive the ORGANIZER role
        User user = new User(organizerId.trim(),name.trim(),hashedPassword,"ORGANIZER");

        if (!userRepository.createUser(user)) {
            JOptionPane.showMessageDialog(null,"Account creation failed. The Organizer ID may already be registered.","Account Creation Failed",JOptionPane.ERROR_MESSAGE);
            return null;
        }

        return user;
    }

    
    
    
    
    // -------- Validation --------
    public boolean organizerIdValidation(String organizerId) {

        if (organizerId == null || organizerId.isBlank()) {
            JOptionPane.showMessageDialog(
                    null,
                    "Please enter your Organizer ID.",
                    "Invalid Organizer ID",
                    JOptionPane.ERROR_MESSAGE
            );
            return false;
        }

        if (!organizerId.trim().matches("[0-9]+")) {
            JOptionPane.showMessageDialog(
                    null,
                    "Organizer ID must contain numbers only.",
                    "Invalid Organizer ID",
                    JOptionPane.ERROR_MESSAGE
            );
            return false;
        }

        return true;
    }

    public boolean nameValidation(String name) {

        if (name == null || name.isBlank()) {
            JOptionPane.showMessageDialog(
                    null,
                    "Your name cannot be blank.",
                    "Invalid Name",
                    JOptionPane.ERROR_MESSAGE
            );
            return false;
        }

        if (!name.trim().matches("[a-zA-Z ]+")) {
            JOptionPane.showMessageDialog(
                    null,
                    "Your name must contain letters and spaces only.",
                    "Invalid Name",
                    JOptionPane.ERROR_MESSAGE
            );
            return false;
        }

        return true;
    }

    public boolean passwordValidation(String password) {

        if (password == null || password.isBlank()) {
            JOptionPane.showMessageDialog(
                    null,
                    "Your password cannot be blank.",
                    "Invalid Password",
                    JOptionPane.ERROR_MESSAGE
            );
            return false;
        }

        if (password.length() < 8) {
            JOptionPane.showMessageDialog(
                    null,
                    "Password must be at least 8 characters long.",
                    "Invalid Password",
                    JOptionPane.ERROR_MESSAGE
            );
            return false;
        }

        if (!Character.isUpperCase(password.charAt(0))) {
            JOptionPane.showMessageDialog(
                    null,
                    "The first character of your password must be uppercase.",
                    "Invalid Password",
                    JOptionPane.ERROR_MESSAGE
            );
            return false;
        }

        if (!password.matches(".*\\d.*")) {
            JOptionPane.showMessageDialog(
                    null,
                    "Password must contain at least one number.",
                    "Invalid Password",
                    JOptionPane.ERROR_MESSAGE
            );
            return false;
        }

        if (!password.matches(".*[^a-zA-Z0-9].*")) {
            JOptionPane.showMessageDialog(
                    null,
                    "Password must contain at least one special character, such as !, @, or #.",
                    "Invalid Password",
                    JOptionPane.ERROR_MESSAGE
            );
            return false;
        }

        return true;
    }

    
    
    
    
    // -------- Login --------
   public User logIn(String accountId, String password) {

       if (accountId == null || accountId.isBlank()
               || password == null || password.isBlank()) {
           return null;
       }

       User user = userRepository.findUserByStudentId(accountId.trim());

       if (user == null) {
           return null;
       }

       try {
           if (BCrypt.checkpw(password, user.getPassword())) {
               return user;
           }
       } catch (IllegalArgumentException e) {
           return null;
       }

       return null;
   }

   /** Updates an organizer's profile after verifying the current password. */
   public boolean updateOrganizerProfile(User organizer, String currentPassword,
           String newName, String newPassword) {
       if (organizer == null || !"ORGANIZER".equalsIgnoreCase(organizer.getRole())
               || currentPassword == null || currentPassword.isBlank()) return false;
       User stored = userRepository.findUserById(organizer.getId());
       if (stored == null) return false;
       try {
           if (!BCrypt.checkpw(currentPassword, stored.getPassword())) return false;
       } catch (IllegalArgumentException ex) {
           return false;
       }
       if (newName == null || !nameValidation(newName)) return false;
       if (newPassword != null && !newPassword.isBlank()) {
           if (!passwordValidation(newPassword)) return false;
           stored.setPassword(BCrypt.hashpw(newPassword, BCrypt.gensalt()));
       }
       stored.setName(newName.trim());
       if (!userRepository.updateUser(stored.getId(), stored)) return false;
       organizer.setName(stored.getName());
       organizer.setPassword(stored.getPassword());
       return true;
   }


    
    
    
    
    // -------- Admin User Management --------
    public boolean updateUserRole(User admin,User targetUser,String newRole) {

        if (admin == null || targetUser == null|| newRole == null || newRole.isBlank()) {
            return false;
        }

        if (!"ADMIN".equalsIgnoreCase(admin.getRole())) {
            return false;
        }

        String role = newRole.trim().toUpperCase();

        if (!role.equals("ORGANIZER") && !role.equals("ADMIN")) {
            
            return false;
        }

        targetUser.setRole(role);

        return userRepository.updateUser(targetUser.getId(),targetUser);
    }
}
