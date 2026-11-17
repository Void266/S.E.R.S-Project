package javamongo1.sersproject.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javamongo1.sersproject.model.User;
import javamongo1.sersproject.service.UserService;

public class LoginFrame extends JFrame implements ActionListener {
    private JTextField accountIdField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton createAccountButton;

    public LoginFrame() {
        setTitle("RMMC | School Event Registration System");
        setSize(1050, 700);
        setMinimumSize(new Dimension(820, 600));
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        RmmcTheme.styleFrame(this);
        RmmcTheme.installLoginBackground(this);

        JPanel brandPanel = new JPanel();
        brandPanel.setOpaque(false);
            brandPanel.setPreferredSize(new Dimension(470, 0));
            brandPanel.setBorder(BorderFactory.createEmptyBorder(48, 58, 48, 42));
            
        brandPanel.setLayout(new BoxLayout(brandPanel, BoxLayout.Y_AXIS));
        
        JLabel logo = new JLabel(RmmcTheme.logoIcon(200, 200));
            logo.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            brandPanel.add(logo);
            brandPanel.add(Box.createVerticalStrut(32));
            
        JLabel brand = new JLabel("<html><div style='text-align:center'>RAMON MAGSAYSAY<br>MEMORIAL COLLEGES</div></html>");
            brand.setFont(new Font("SansSerif", Font.BOLD, 22));
            brand.setForeground(RmmcTheme.NAVY);
            brand.setAlignmentX(Component.CENTER_ALIGNMENT);
            brandPanel.add(brand);  
        brandPanel.add(Box.createVerticalStrut(12));
        
        JLabel description = new JLabel("<html><div style='text-align:center'>School Event<br>Registration System</div></html>");
            description.setFont(new Font("SansSerif", Font.PLAIN, 16));
            description.setForeground(RmmcTheme.MUTED);
            description.setAlignmentX(Component.CENTER_ALIGNMENT);
            
        brandPanel.add(description);
        brandPanel.add(Box.createVerticalGlue());
        
        JLabel year = new JLabel("GENERAL SANTOS CITY  •  EST. 1960");
            year.setFont(new Font("SansSerif", Font.PLAIN, 11));
            year.setForeground(RmmcTheme.MUTED);
            year.setAlignmentX(Component.LEFT_ALIGNMENT);
            
        brandPanel.add(year);
        add(brandPanel, BorderLayout.WEST);

        JPanel content = new JPanel(new GridBagLayout());
            content.setOpaque(false);
            
        JPanel card = RmmcTheme.card();
            card.setLayout(new GridBagLayout());
            card.setPreferredSize(new Dimension(560, 460));
            card.setBackground(new Color(255, 255, 255, 232));
            card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(255, 255, 255, 220), 2),
                    BorderFactory.createEmptyBorder(28, 30, 28, 30)));
            
        GridBagConstraints c = new GridBagConstraints();
            c.gridx = 0; c.weightx = 1; c.fill = GridBagConstraints.HORIZONTAL;
            c.insets = new Insets(7, 0, 7, 0);
            
        JLabel eyebrow = new JLabel("WELCOME BACK");
            eyebrow.setFont(new Font("SansSerif", Font.BOLD, 12));
            eyebrow.setForeground(RmmcTheme.isDarkMode() ? new Color(177, 197, 244) : RmmcTheme.BLUE);
            
        c.gridy = 0; card.add(eyebrow, c);
        
        JLabel heading = new JLabel("Sign in to your account");
            heading.setFont(new Font("SansSerif", Font.BOLD, 25));
            heading.setForeground(RmmcTheme.isDarkMode() ? new Color(235, 239, 247) : RmmcTheme.INK);
            
        c.gridy = 1; c.insets = new Insets(0, 0, 22, 0); card.add(heading, c);
        c.insets = new Insets(5, 0, 4, 0);
        
        JLabel accountLabel = new JLabel("Account ID");
            accountLabel.setForeground(RmmcTheme.isDarkMode() ? new Color(220, 226, 237) : RmmcTheme.INK);
            
        c.gridy = 2; card.add(accountLabel, c);
        accountIdField = new JTextField();
        RmmcTheme.styleField(accountIdField);
        
        c.gridy = 3; c.insets = new Insets(2, 0, 14, 0); card.add(accountIdField, c);
        
        JLabel passwordLabel = new JLabel("Password");
            passwordLabel.setForeground(RmmcTheme.isDarkMode() ? new Color(220, 226, 237) : RmmcTheme.INK);
            c.gridy = 4; c.insets = new Insets(5, 0, 4, 0); card.add(passwordLabel, c);
            passwordField = new JPasswordField();
            RmmcTheme.styleField(passwordField);
            
        c.gridy = 5; c.insets = new Insets(2, 0, 20, 0); card.add(passwordField, c);
        loginButton = new JButton("Sign In");
        RmmcTheme.styleButton(loginButton, true);
        
        loginButton.addActionListener(this);
        c.gridy = 6; c.insets = new Insets(2, 0, 10, 0); card.add(loginButton, c);
        createAccountButton = new JButton("Create an organizer account");
        RmmcTheme.styleButton(createAccountButton, false);
        
        createAccountButton.addActionListener(this);
        c.gridy = 7; c.insets = new Insets(2, 0, 0, 0); card.add(createAccountButton, c);
        content.add(card);
        
        add(content, BorderLayout.CENTER);
        getRootPane().setDefaultButton(loginButton);
    }

    public String getAccountId() { return accountIdField.getText(); }
    public String getPassword() { return new String(passwordField.getPassword()); }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == loginButton) {
            String accountId = getAccountId();
            String password = getPassword();
            if (accountId.isBlank() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in all fields.");
                return;
            }
            loginButton.setEnabled(false);
            new SwingWorker<User, Void>() {
                @Override protected User doInBackground() { return new UserService().logIn(accountId, password); }
                @Override protected void done() {
                    loginButton.setEnabled(true);
                    try {
                        User user = get();
                        if (user == null) {
                            JOptionPane.showMessageDialog(LoginFrame.this, "Account ID or password is incorrect.", "Login Failed", JOptionPane.ERROR_MESSAGE);
                        } else if (!"ORGANIZER".equalsIgnoreCase(user.getRole())
                                && !"ADMIN".equalsIgnoreCase(user.getRole())) {
                            JOptionPane.showMessageDialog(LoginFrame.this, "Only organizer and admin accounts can use this system.", "Access Restricted", JOptionPane.WARNING_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(LoginFrame.this, "Welcome " + user.getName(), "Login Successful", JOptionPane.INFORMATION_MESSAGE);
                            if ("ADMIN".equalsIgnoreCase(user.getRole())) {
                                new AdminDashboardFrame(user).setVisible(true);
                            } else {
                                new OrganizerDashboardFrame(user).setVisible(true);
                            }
                            dispose();
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(LoginFrame.this, "Could not connect to the database.", "Login Failed", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }.execute();
        } else if (e.getSource() == createAccountButton) {
            new RegistrationFrame().setVisible(true);
            dispose();
        }
    }
}
