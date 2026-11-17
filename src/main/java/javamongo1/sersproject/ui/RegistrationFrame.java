package javamongo1.sersproject.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javamongo1.sersproject.model.User;
import javamongo1.sersproject.service.UserService;

public class RegistrationFrame extends JFrame implements ActionListener {

    private JTextField nameField;
    private JPasswordField passwordField;
    private JTextField organizerIdField;
    private JLabel passwordRequirementsLabel;
    private final JCheckBox privacyNoticeAcknowledgment = new JCheckBox("I have read the Privacy Notice");
    private JButton privacyNoticeButton;
    
    private JButton createAccountButton;
    private JButton backButton;
    
    
    UserService US = new UserService();

        public RegistrationFrame() {

            setTitle("RMMC | Create Organizer Account");
        setSize(500, 400);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        RmmcTheme.styleFrame(this);
        RmmcTheme.installLoginBackground(this);

        JPanel shell = new JPanel(new BorderLayout());
        shell.setOpaque(false);
        shell.add(RmmcTheme.header("Create Account", "Register an organizer account"), BorderLayout.NORTH);
        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);
        JPanel form = RmmcTheme.card();
        form.setBackground(new Color(255, 255, 255, 242));
        form.setLayout(new GridBagLayout());
        form.setPreferredSize(new Dimension(480, 700));
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.weightx = 1; c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(6, 0, 5, 0);

            JLabel titleLabel = new JLabel("Create Account");
                titleLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
                titleLabel.setForeground(RmmcTheme.NAVY);
                c.gridy = 0; form.add(titleLabel, c);

            JLabel nameLabel = new JLabel("Name:");
                nameLabel.setForeground(RmmcTheme.INK);
                c.gridy = 1; form.add(nameLabel, c);

                nameField = new JTextField();
                RmmcTheme.styleField(nameField);
                nameField.setPreferredSize(new Dimension(420, 42));
                c.gridy = 2; form.add(nameField, c);

            JLabel passwordLabel = new JLabel("Password:");
                passwordLabel.setForeground(RmmcTheme.INK);
                c.gridy = 3; form.add(passwordLabel, c);

                passwordField = new JPasswordField();
                RmmcTheme.styleField(passwordField);
                passwordField.setPreferredSize(new Dimension(420, 42));
                c.gridy = 4; form.add(passwordField, c);

                passwordRequirementsLabel = new JLabel();
                passwordRequirementsLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
                passwordRequirementsLabel.setForeground(RmmcTheme.MUTED);
                c.gridy = 5; c.insets = new Insets(0, 0, 8, 0); form.add(passwordRequirementsLabel, c);
                passwordField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                    private void updateRequirements() {
                        char[] password = passwordField.getPassword();
                        String value = new String(password);
                        String[] checks = {
                            requirement(value.length() >= 8, "At least 8 characters"),
                            requirement(!value.isEmpty() && Character.isUpperCase(value.charAt(0)), "Starts with an uppercase letter"),
                            requirement(value.matches(".*\\\\d.*"), "Contains a number"),
                            requirement(value.matches(".*[^a-zA-Z0-9].*"), "Contains a special character")
                        };
                        passwordRequirementsLabel.setText("<html>Password must:<br>• " + String.join("<br>• ", checks) + "</html>");
                        java.util.Arrays.fill(password, '\0');
                    }
                    @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { updateRequirements(); }
                    @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { updateRequirements(); }
                    @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { updateRequirements(); }
                });
                passwordRequirementsLabel.setText("<html>Password must:<br>• At least 8 characters<br>• Start with an uppercase letter<br>• Contain a number<br>• Contain a special character</html>");
            

            JLabel roleLabel = new JLabel("Organizer ID:");
                roleLabel.setForeground(RmmcTheme.INK);
            c.gridy = 6; form.add(roleLabel, c);

                organizerIdField = new JTextField();
                RmmcTheme.styleField(organizerIdField);
                organizerIdField.setPreferredSize(new Dimension(420, 42));
                c.gridy = 7; form.add(organizerIdField, c);

                privacyNoticeButton = new JButton("View Privacy Notice");
                RmmcTheme.styleButton(privacyNoticeButton, false);
                privacyNoticeButton.addActionListener(e -> showPrivacyNotice());
                privacyNoticeAcknowledgment.addActionListener(e -> updatePrivacyNoticeButtonAppearance());
                c.gridy = 8; c.insets = new Insets(10, 0, 6, 0);
                form.add(privacyNoticeButton, c);

                createAccountButton = new JButton("Create Account");
                RmmcTheme.styleButton(createAccountButton, true);
                createAccountButton.addActionListener(this);
                c.gridy = 9; c.insets = new Insets(8, 0, 6, 0); form.add(createAccountButton, c);

                backButton = new JButton("Back");
                RmmcTheme.styleButton(backButton, false);
                backButton.addActionListener(this);
                c.gridy = 10; c.insets = new Insets(5, 0, 0, 0); form.add(backButton, c);
        center.add(form);
        shell.add(center, BorderLayout.CENTER);
        add(shell, BorderLayout.CENTER);
    }

    private String requirement(boolean met, String text) {
        return (met ? "✓ " : "○ ") + text;
    }

    private void showPrivacyNotice() {
        String notice = "S.E.R.S. Privacy Notice\n\n"
                + "Information collected: organizer account name and ID, and a password stored as a BCrypt hash. "
                + "Attendance records may include a participant's name, student ID, course, event, and check-in/check-out times.\n\n"
                + "Purpose: account information is used to authenticate organizers and administrators. Attendance information "
                + "is used to manage and review event attendance.\n\n"
                + "Access and storage: the application stores records in its configured MongoDB database. The current project "
                + "configuration uses a local MongoDB server. Organizer and administrator functions can view or manage "
                + "attendance records according to their assigned system role.\n\n"
                + "Retention and privacy contact: the school must provide the applicable retention period and privacy contact "
                + "before this notice is used with real student data. Follow the school's approved privacy and records policies.";

        JTextArea noticeText = new JTextArea(notice, 14, 48);
        noticeText.setEditable(false);
        noticeText.setLineWrap(true);
        noticeText.setWrapStyleWord(true);
        noticeText.setCaretPosition(0);
        noticeText.setBackground(UIManager.getColor("Panel.background"));
        JScrollPane noticeScroll = new JScrollPane(noticeText);
        noticeScroll.setPreferredSize(new Dimension(560, 300));

        privacyNoticeAcknowledgment.setEnabled(false);
        privacyNoticeAcknowledgment.setSelected(false);
        updatePrivacyNoticeButtonAppearance();
        noticeScroll.getVerticalScrollBar().addAdjustmentListener(e -> {
            JScrollBar bar = noticeScroll.getVerticalScrollBar();
            boolean reachedBottom = bar.getValue() + bar.getVisibleAmount() >= bar.getMaximum();
            if (reachedBottom) {
                privacyNoticeAcknowledgment.setEnabled(true);
            }
        });
        SwingUtilities.invokeLater(() -> {
            JScrollBar bar = noticeScroll.getVerticalScrollBar();
            if (bar.getValue() + bar.getVisibleAmount() >= bar.getMaximum()) {
                privacyNoticeAcknowledgment.setEnabled(true);
            }
        });

        JPanel noticePanel = new JPanel(new BorderLayout(0, 10));
        noticePanel.add(noticeScroll, BorderLayout.CENTER);
        noticePanel.add(privacyNoticeAcknowledgment, BorderLayout.SOUTH);
        JOptionPane.showMessageDialog(this, noticePanel,
                "S.E.R.S. Privacy Notice", JOptionPane.INFORMATION_MESSAGE);
    }

    private void updatePrivacyNoticeButtonAppearance() {
        if (privacyNoticeButton == null) {
            return;
        }
        if (privacyNoticeAcknowledgment.isSelected()) {
            privacyNoticeButton.setText("Privacy Notice Acknowledged");
            privacyNoticeButton.setBackground(new Color(46, 125, 50));
            privacyNoticeButton.setForeground(Color.WHITE);
        } else {
            privacyNoticeButton.setText("View Privacy Notice");
            RmmcTheme.styleButton(privacyNoticeButton, false);
        }
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == createAccountButton) {

            if (!privacyNoticeAcknowledgment.isSelected()) {
                JOptionPane.showMessageDialog(this,
                        "Please open the Privacy Notice, scroll to the bottom, and check the acknowledgment before creating an account.",
                        "Privacy Notice", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String name = nameField.getText().trim();
            String password = new String(passwordField.getPassword());
            String organizerId = organizerIdField.getText().trim();
                

                createAccountButton.setEnabled(false);
                new SwingWorker<User, Void>() {
                    @Override
                    protected User doInBackground() {
                        return US.createUser(organizerId, name, password);
                    }

                    @Override
                    protected void done() {
                        createAccountButton.setEnabled(true);
                        try {
                            if (get() != null) {
                                JOptionPane.showMessageDialog(RegistrationFrame.this,"Account created successfully!","Success",JOptionPane.INFORMATION_MESSAGE);
                            }
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(RegistrationFrame.this,"Could not connect to the database.","Account Creation Failed",JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }.execute();

        } else if (e.getSource() == backButton) {
            new LoginFrame().setVisible(true);
            dispose();
        }
    }
}
