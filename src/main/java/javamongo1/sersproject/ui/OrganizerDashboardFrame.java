package javamongo1.sersproject.ui;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

import javamongo1.sersproject.model.Event;
import javamongo1.sersproject.model.User;
import javamongo1.sersproject.repository.EventRepository;
import javamongo1.sersproject.service.UserService;

public class OrganizerDashboardFrame extends JFrame implements ActionListener {

    private final User user;

    private JTable currentEventTable, upcomingEventTable;
    private DefaultTableModel currentTableModel, upcomingTableModel;
    private JButton openButton;
    private JButton refreshButton;
    private JButton attendanceAppsButton;
    private JButton logoutButton;
    private JButton optionsButton;
    private JLabel welcomeLabel;
    private JLabel loadStatusLabel;
    private JLabel titleValue;
    private JTextArea descriptionValue;
    private JLabel dateTimeValue;
    private JLabel locationValue;
    private JLabel capacityValue;
    private JLabel approvalValue;
    private JLabel statusValue;
    private JLabel creatorValue;

    private final EventRepository eventRepository = new EventRepository();
    private List<Event> currentEvents = List.of();
    private List<Event> upcomingEvents = List.of();

    public OrganizerDashboardFrame(User user) {
        this.user = user;

        setTitle("S.E.R.S. - Organizer Dashboard");
        setSize(1200, 720);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        RmmcTheme.styleFrame(this);
        RmmcTheme.installBackground(this, 1);

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createContentPanel(), BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.SOUTH);
        RmmcTheme.applyToTree(getContentPane());

        loadEvents();
    }

    // ==================================================
    // 1. DASHBOARD LAYOUT
    // ==================================================

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(18, 24, 8, 24));

        welcomeLabel = new JLabel("Welcome, " + user.getName());
        welcomeLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        welcomeLabel.setForeground(Color.WHITE);
        JPanel brand = RmmcTheme.header("Organizer Dashboard", "School Event Registration System");
        brand.add(welcomeLabel, BorderLayout.EAST);
        headerPanel.add(brand, BorderLayout.CENTER);

        return headerPanel;
    }

    private JPanel createContentPanel() {
        JPanel contentPanel = new JPanel(new BorderLayout(12, 0));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(0, 24, 8, 24));

        contentPanel.add(createEventListPanel(), BorderLayout.WEST);
        contentPanel.add(createEventDetailsPanel(), BorderLayout.CENTER);

        return contentPanel;
    }

    private JPanel createEventListPanel() {
        JPanel eventListPanel = new JPanel(new GridLayout(2, 1, 0, 12));
        eventListPanel.setPreferredSize(new Dimension(410, 0));
        eventListPanel.setOpaque(false);
        currentTableModel = createEventTableModel();
        currentEventTable = createEventTable(currentTableModel);
        upcomingTableModel = createEventTableModel();
        upcomingEventTable = createEventTable(upcomingTableModel);
        currentEventTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && currentEventTable.getSelectedRow() >= 0) upcomingEventTable.clearSelection();
        });
        upcomingEventTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && upcomingEventTable.getSelectedRow() >= 0) currentEventTable.clearSelection();
        });
        eventListPanel.add(createEventListSection("Current Events", currentEventTable, true));
        eventListPanel.add(createEventListSection("Upcoming Events", upcomingEventTable, false));
        return eventListPanel;
    }

    private DefaultTableModel createEventTableModel() {
        return new DefaultTableModel(new String[]{"Event ID", "Event Title", "Date"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
    }

    private JTable createEventTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        RmmcTheme.styleTable(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(this::eventSelectionChanged);
        return table;
    }

    private JPanel createEventListSection(String title, JTable table, boolean current) {
        JPanel section = new JPanel(new BorderLayout(0, 6));
        section.setOpaque(false);
        JLabel heading = new JLabel(title);
        heading.setFont(new Font("Arial", Font.BOLD, 18));
        section.add(heading, BorderLayout.NORTH);
        section.add(new JScrollPane(table), BorderLayout.CENTER);
        if (current) section.add(loadStatusLabel = new JLabel("Loading events..."), BorderLayout.SOUTH);
        return section;
    }

    private JPanel createEventDetailsPanel() {
        JPanel detailsPanel = new JPanel(new BorderLayout(0, 12));
        detailsPanel.setOpaque(true);
        detailsPanel.setBackground(Color.WHITE);
        detailsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(190, 198, 208)),
                BorderFactory.createEmptyBorder(18, 20, 18, 20)
        ));

        JLabel detailsHeading = new JLabel("Event Details");
        detailsHeading.setFont(new Font("Arial", Font.BOLD, 22));
        detailsPanel.add(detailsHeading, BorderLayout.NORTH);

        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        fieldsPanel.setOpaque(false);
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.NORTHWEST;
        constraints.insets = new Insets(7, 4, 7, 12);

        titleValue = new JLabel("Select an event to see its details.");
        descriptionValue = new JTextArea(4, 30);
        descriptionValue.setLineWrap(true);
        descriptionValue.setWrapStyleWord(true);
        descriptionValue.setEditable(false);
        descriptionValue.setOpaque(false);
        dateTimeValue = new JLabel(" ");
        locationValue = new JLabel(" ");
        capacityValue = new JLabel(" ");
        approvalValue = new JLabel(" ");
        statusValue = new JLabel(" ");
        creatorValue = new JLabel(" ");

        int row = 0;
        addDetailRow(fieldsPanel, constraints, row++, "Event Title:", titleValue);
        addDetailRow(fieldsPanel, constraints, row++, "Description:", new JScrollPane(descriptionValue));
        addDetailRow(fieldsPanel, constraints, row++, "Date & Time:", dateTimeValue);
        addDetailRow(fieldsPanel, constraints, row++, "Location:", locationValue);
        addDetailRow(fieldsPanel, constraints, row++, "Capacity:", capacityValue);
        addDetailRow(fieldsPanel, constraints, row++, "Approval:", approvalValue);
        addDetailRow(fieldsPanel, constraints, row++, "Status:", statusValue);
        addDetailRow(fieldsPanel, constraints, row, "Organizer:", creatorValue);

        detailsPanel.add(fieldsPanel, BorderLayout.CENTER);
        return detailsPanel;
    }

    private void addDetailRow(JPanel panel, GridBagConstraints constraints, int row, String label, Component value) {
        constraints.gridy = row;
        constraints.gridx = 0;
        constraints.weightx = 0;
        constraints.fill = GridBagConstraints.NONE;
        JLabel fieldLabel = new JLabel(label);
        fieldLabel.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(fieldLabel, constraints);

        constraints.gridx = 1;
        constraints.weightx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        panel.add(value, constraints);
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 24, 10, 24));

        JPanel eventButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 12));
        eventButtonPanel.setOpaque(false);
        openButton = new JButton("Open");
        RmmcTheme.styleButton(openButton, true);
        openButton.setEnabled(false);
        openButton.addActionListener(this);
        eventButtonPanel.add(openButton);

        refreshButton = new JButton("Refresh");
        RmmcTheme.styleButton(refreshButton, false);
        refreshButton.addActionListener(this);
        eventButtonPanel.add(refreshButton);
        attendanceAppsButton = new JButton("Attendance Apps");
        RmmcTheme.styleButton(attendanceAppsButton, false);
        attendanceAppsButton.addActionListener(this);
        eventButtonPanel.add(attendanceAppsButton);
        buttonPanel.add(eventButtonPanel, BorderLayout.WEST);

        JPanel logoutButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        logoutButtonPanel.setOpaque(false);
        optionsButton = new JButton("Options");
        RmmcTheme.styleButton(optionsButton, false);
        optionsButton.addActionListener(e -> showOptions());
        logoutButtonPanel.add(optionsButton);
        logoutButton = new JButton("Log Out");
        RmmcTheme.styleButton(logoutButton, false);
        logoutButton.addActionListener(this);
        logoutButtonPanel.add(logoutButton);
        buttonPanel.add(logoutButtonPanel, BorderLayout.EAST);

        return buttonPanel;
    }

    // ==================================================
    // 2. EVENT LIST AND DETAILS
    // ==================================================

    private void loadEvents() {
        int selectedEventId = getSelectedEventId();
        refreshButton.setEnabled(false);
        openButton.setEnabled(false);
        loadStatusLabel.setText("Loading events...");

        new SwingWorker<List<Event>, Void>() {
            @Override
            protected List<Event> doInBackground() {
                return eventRepository.findAllEvents();
            }

            @Override
            protected void done() {
                refreshButton.setEnabled(true);
                try {
                    List<Event> allEvents = get();
                    List<Event> loadedCurrent = new ArrayList<>();
                    List<Event> loadedUpcoming = new ArrayList<>();
                    LocalDateTime now = LocalDateTime.now();
                    LocalDate today = now.toLocalDate();
                    for (Event event : allEvents) {
                        String status = event.getStatus() == null ? "" : event.getStatus().toUpperCase();
                        LocalDateTime dateTime = event.getEventDateTime();
                        if ("CANCELLED".equals(status) || "COMPLETED".equals(status) || dateTime == null || dateTime.isBefore(now)) continue;
                        if (dateTime.toLocalDate().equals(today)) loadedCurrent.add(event);
                        else if (dateTime.toLocalDate().isAfter(today)) loadedUpcoming.add(event);
                    }
                    currentEvents = loadedCurrent;
                    upcomingEvents = loadedUpcoming;
                    currentTableModel.setRowCount(0);
                    upcomingTableModel.setRowCount(0);
                    clearEventDetails();

                    int selectedRow = populateEventTable(currentTableModel, currentEvents, selectedEventId);
                    populateEventTable(upcomingTableModel, upcomingEvents, selectedEventId);
                    loadStatusLabel.setText(currentEvents.size() + " current event(s)");
                    if (selectedRow >= 0) currentEventTable.setRowSelectionInterval(selectedRow, selectedRow);
                    else {
                        int upcomingRow = findEventRow(upcomingEvents, selectedEventId);
                        if (upcomingRow >= 0) upcomingEventTable.setRowSelectionInterval(upcomingRow, upcomingRow);
                    }
                } catch (Exception ex) {
                    loadStatusLabel.setText("Could not load events.");
                    JOptionPane.showMessageDialog(OrganizerDashboardFrame.this,
                            "Could not load events from the database.", "Load Failed", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private int getSelectedEventId() {
        Event selected = getSelectedEvent();
        return selected == null ? 0 : selected.getId();
    }

    private Event getSelectedEvent() {
        int row = currentEventTable == null ? -1 : currentEventTable.getSelectedRow();
        if (row >= 0 && row < currentEvents.size()) return currentEvents.get(currentEventTable.convertRowIndexToModel(row));
        row = upcomingEventTable == null ? -1 : upcomingEventTable.getSelectedRow();
        if (row >= 0 && row < upcomingEvents.size()) return upcomingEvents.get(upcomingEventTable.convertRowIndexToModel(row));
        return null;
    }

    private int populateEventTable(DefaultTableModel model, List<Event> source, int preserveId) {
        model.setRowCount(0);
        int selectedRow = -1;
        for (int i=0; i<source.size(); i++) {
            Event event = source.get(i);
            model.addRow(new Object[]{event.getId(), event.getEventName(), event.getEventDateTime()});
            if (event.getId() == preserveId) selectedRow = i;
        }
        return selectedRow;
    }

    private int findEventRow(List<Event> source, int eventId) {
        for (int i=0; i<source.size(); i++) if (source.get(i).getId() == eventId) return i;
        return -1;
    }

    private void eventSelectionChanged(ListSelectionEvent event) {
        if (event.getValueIsAdjusting()) {
            return;
        }

        Event selectedEvent = getSelectedEvent();
        if (selectedEvent == null) {
            clearEventDetails();
            return;
        }
        showEventDetails(selectedEvent);
    }

    private void showEventDetails(Event event) {
        titleValue.setText(event.getEventName());
        descriptionValue.setText(event.getDescription());
        dateTimeValue.setText(event.getEventDateTime() == null ? "Not set" : event.getEventDateTime().toString());
        locationValue.setText(event.getLocation());
        capacityValue.setText(Integer.toString(event.getCapacity()));
        approvalValue.setText(event.getApproval());
        statusValue.setText(event.getStatus());
        creatorValue.setText(event.getCreator());
        openButton.setEnabled(true);
    }

    private void clearEventDetails() {
        titleValue.setText("Select an event to see its details.");
        descriptionValue.setText("");
        dateTimeValue.setText(" ");
        locationValue.setText(" ");
        capacityValue.setText(" ");
        approvalValue.setText(" ");
        statusValue.setText(" ");
        creatorValue.setText(" ");
        openButton.setEnabled(false);
    }

    // ==================================================
    // 3. BUTTON ACTIONS
    // ==================================================

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == openButton) {
            int eventId = getSelectedEventId();
            if (eventId > 0) {
                EventDetailsFrame detailsFrame = new EventDetailsFrame(eventId, this);
                setVisible(false);
                detailsFrame.setVisible(true);
            }
        } else if (e.getSource() == refreshButton) {
            loadEvents();
        } else if (e.getSource() == attendanceAppsButton) {
            showAttendanceApps();
        } else if (e.getSource() == logoutButton) {
            int choice = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to log out?",
                    "Log Out",
                    JOptionPane.YES_NO_OPTION
            );
            if (choice == JOptionPane.YES_OPTION) {
                new LoginFrame().setVisible(true);
                dispose();
            }
        }
    }

    private void showOptions() {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem profile = new JMenuItem("Change name or password...");
        profile.addActionListener(e -> editOrganizerProfile());
        JCheckBoxMenuItem darkMode = new JCheckBoxMenuItem("Dark mode", RmmcTheme.isDarkMode());
        darkMode.addActionListener(e -> {
            RmmcTheme.setDarkMode(darkMode.isSelected());
            getContentPane().setBackground(RmmcTheme.isDarkMode() ? new Color(24, 29, 39) : Color.WHITE);
            RmmcTheme.applyToTree(getContentPane());
            repaint();
        });
        menu.add(profile);
        menu.addSeparator();
        menu.add(darkMode);
        menu.show(optionsButton, 0, optionsButton.getHeight());
    }

    private void editOrganizerProfile() {
        JTextField name = new JTextField(user.getName(), 20);
        JPasswordField current = new JPasswordField(20);
        JPasswordField next = new JPasswordField(20);
        JPasswordField confirm = new JPasswordField(20);
        JPanel form = new JPanel(new GridLayout(0, 1, 4, 4));
        form.add(new JLabel("Display name")); form.add(name);
        form.add(new JLabel("Current password (required to save)")); form.add(current);
        form.add(new JLabel("New password (leave blank to keep current)")); form.add(next);
        form.add(new JLabel("Confirm new password")); form.add(confirm);
        int result = JOptionPane.showConfirmDialog(this, form, "Organizer Account Settings",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;
        char[] currentChars = current.getPassword();
        char[] nextChars = next.getPassword();
        char[] confirmChars = confirm.getPassword();
        try {
            if (!java.util.Arrays.equals(nextChars, confirmChars)) {
                JOptionPane.showMessageDialog(this, "The new passwords do not match.",
                        "Account Settings", JOptionPane.ERROR_MESSAGE);
                return;
            }
            boolean changed = new UserService().updateOrganizerProfile(user,
                    new String(currentChars), name.getText(), new String(nextChars));
            if (changed) {
                welcomeLabel.setText("Welcome, " + user.getName());
                JOptionPane.showMessageDialog(this, "Your account details were updated.",
                        "Account Settings", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Could not update your account. Check your current password and the password requirements.",
                        "Account Settings", JOptionPane.ERROR_MESSAGE);
            }
        } finally {
            java.util.Arrays.fill(currentChars, '\0');
            java.util.Arrays.fill(nextChars, '\0');
            java.util.Arrays.fill(confirmChars, '\0');
        }
    }

    private void showAttendanceApps() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        content.add(createAttendanceAppCard(
                "QR Attendance Control",
                "Use this app to manage QR attendance.",
                "/javamongo1/sersproject/logo/QR Attendance Control logo.webp",
                "https://play.google.com/store/apps/details?id=com.cpdhdevs.qrattendancecontrol"
        ));
        content.add(Box.createVerticalStrut(12));
        content.add(createAttendanceAppCard(
                "Scanner: QR Code and Products",
                "Use this app to scan QR codes.",
                "/javamongo1/sersproject/logo/Scanner QR Code and Products.webp",
                "https://play.google.com/store/apps/details?id=com.atharok.barcodescanner"
        ));

        JOptionPane.showMessageDialog(this, content, "Attendance Apps", JOptionPane.PLAIN_MESSAGE);
    }

    private JPanel createAttendanceAppCard(String name, String description, String logoPath, String storeLink) {
        JPanel card = new JPanel(new BorderLayout(14, 8));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(RmmcTheme.BORDER),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        card.setMaximumSize(new Dimension(500, 116));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel logo = new JLabel(loadAppLogo(logoPath, 72, 72));
        card.add(logo, BorderLayout.WEST);

        JPanel details = new JPanel();
        details.setOpaque(false);
        details.setLayout(new BoxLayout(details, BoxLayout.Y_AXIS));
        JLabel title = new JLabel(name);
        title.setFont(new Font("Arial", Font.BOLD, 16));
        details.add(title);
        details.add(Box.createVerticalStrut(5));
        JLabel note = new JLabel(description);
        note.setForeground(RmmcTheme.MUTED);
        details.add(note);
        details.add(Box.createVerticalStrut(10));
        JButton openStore = new JButton("Open in Google Play");
        RmmcTheme.styleButton(openStore, true);
        openStore.addActionListener(event -> openStoreLink(storeLink));
        details.add(openStore);
        card.add(details, BorderLayout.CENTER);
        return card;
    }

    private ImageIcon loadAppLogo(String resourcePath, int width, int height) {
        URL resource = OrganizerDashboardFrame.class.getResource(resourcePath);
        Image source = null;
        if (resource != null) {
            source = new ImageIcon(resource).getImage();
        } else {
            File file = new File("src/main/java" + resourcePath);
            if (file.isFile()) source = new ImageIcon(file.getAbsolutePath()).getImage();
        }
        return source == null ? new ImageIcon() : new ImageIcon(source.getScaledInstance(width, height, Image.SCALE_SMOOTH));
    }

    private void openStoreLink(String storeLink) {
        try {
            if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                throw new UnsupportedOperationException("Opening web links is not supported on this computer.");
            }
            Desktop.getDesktop().browse(URI.create(storeLink));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Could not open Google Play. Copy this link into your browser:\n" + storeLink,
                    "Unable to Open Link", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
