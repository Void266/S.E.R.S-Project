package javamongo1.sersproject.ui;

import javamongo1.sersproject.model.Attendance;
import javamongo1.sersproject.model.Event;
import javamongo1.sersproject.repository.EventRepository;
import javamongo1.sersproject.service.AttendanceService;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class EventDetailsFrame extends JFrame implements ActionListener {

    private static final DateTimeFormatter TIME_DISPLAY = DateTimeFormatter.ofPattern("h:mm:ss a");

    private final EventRepository eventRepository = new EventRepository();
    private final AttendanceService attendanceService = new AttendanceService();
    private final int eventId;
    private final JFrame returnFrame;
    private Event event;

    // Organizer attendance view
    private DefaultTableModel attendanceTableModel;
    private JTable attendanceTable;
    private TableRowSorter<DefaultTableModel> attendanceSorter;
    private JTextField searchField;
    private JLabel eventTitleValue;
    private JLabel eventDateValue;
    private JLabel eventLocationValue;
    private JLabel eventCapacityValue;
    private JLabel eventStatusValue;
    private JTextArea eventDescriptionValue;
    private JButton editButton;
    private JButton deleteButton;
    private JButton addButton;
    private JButton importButton;
    private JButton saveButton;
    private JButton backButton;
    private final List<String> deletedAttendanceIds = new ArrayList<>();
    private List<Attendance> attendanceRows = new ArrayList<>();
    private boolean hasUnsavedChanges;
    private boolean closingAfterSave;

    public EventDetailsFrame(int eventId, JFrame returnFrame) {
        this.eventId = eventId;
        this.returnFrame = returnFrame;

        setTitle("S.E.R.S. - Event Details");
        setSize(1100, 720);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);
        RmmcTheme.styleFrame(this);
        RmmcTheme.installBackground(this, 1);

        initializeOrganizerFrame();
        RmmcTheme.applyToTree(getContentPane());
        loadOrganizerData();
    }

    // ==================================================
    // 1. ORGANIZER ATTENDANCE VIEW
    // ==================================================

    private void initializeOrganizerFrame() {
        setLayout(new BorderLayout(12, 12));
        getContentPane().setBackground(RmmcTheme.isDarkMode()
                ? new Color(24, 29, 39) : RmmcTheme.IVORY);
        add(createOrganizerHeader(), BorderLayout.NORTH);
        add(createOrganizerContent(), BorderLayout.CENTER);
        add(createOrganizerButtonPanel(), BorderLayout.SOUTH);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                handleBack();
            }

            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                if (returnFrame != null) {
                    returnFrame.setVisible(true);
                    returnFrame.toFront();
                    returnFrame.requestFocus();
                }
            }
        });
    }

    private JPanel createOrganizerHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(16, 22, 0, 22));
        header.add(RmmcTheme.header("Event Attendance", "Organizer tools and attendance records"), BorderLayout.CENTER);
        return header;
    }

    private JPanel createOrganizerContent() {
        JPanel content = new JPanel(new BorderLayout(14, 0));
        content.setOpaque(false);
        content.setBorder(BorderFactory.createEmptyBorder(0, 22, 0, 22));
        content.add(createEventDetailsPanel(), BorderLayout.WEST);
        content.add(createAttendancePanel(), BorderLayout.CENTER);
        return content;
    }

    private JPanel createEventDetailsPanel() {
        JPanel detailsPanel = new JPanel(new BorderLayout(0, 16));
        detailsPanel.setPreferredSize(new Dimension(330, 0));
        detailsPanel.setBackground(Color.WHITE);
        detailsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(190, 198, 208)),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        JLabel heading = new JLabel("Event Details");
        heading.setFont(new Font("Arial", Font.BOLD, 21));
        detailsPanel.add(heading, BorderLayout.NORTH);

        JPanel fields = new JPanel(new GridBagLayout());
        fields.setOpaque(false);
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.NORTHWEST;
        constraints.insets = new Insets(0, 0, 14, 0);

        eventTitleValue = new JLabel("Loading event...");
        eventTitleValue.setFont(new Font("Arial", Font.BOLD, 18));
        eventDescriptionValue = new JTextArea(5, 20);
        eventDescriptionValue.setLineWrap(true);
        eventDescriptionValue.setWrapStyleWord(true);
        eventDescriptionValue.setEditable(false);
        eventDescriptionValue.setOpaque(false);
        eventDateValue = new JLabel(" ");
        eventLocationValue = new JLabel(" ");
        eventCapacityValue = new JLabel(" ");
        eventStatusValue = new JLabel(" ");

        addEventField(fields, constraints, "Title", eventTitleValue);
        addEventField(fields, constraints, "Description", new JScrollPane(eventDescriptionValue));
        addEventField(fields, constraints, "Date & Time", eventDateValue);
        addEventField(fields, constraints, "Location", eventLocationValue);
        addEventField(fields, constraints, "Capacity", eventCapacityValue);
        addEventField(fields, constraints, "Status", eventStatusValue);
        detailsPanel.add(fields, BorderLayout.CENTER);
        return detailsPanel;
    }

    private void addEventField(JPanel panel, GridBagConstraints constraints, String name, Component value) {
        JLabel label = new JLabel(name);
        label.setFont(new Font("Arial", Font.BOLD, 13));
        panel.add(label, constraints);
        constraints.gridy++;
        constraints.insets = new Insets(3, 0, 14, 0);
        panel.add(value, constraints);
        constraints.gridy++;
        constraints.insets = new Insets(0, 0, 8, 0);
    }

    private JPanel createAttendancePanel() {
        JPanel attendancePanel = new JPanel(new BorderLayout(0, 10));
        attendancePanel.setOpaque(false);

        JPanel searchPanel = new JPanel(new BorderLayout(8, 0));
        searchPanel.setOpaque(false);
        searchField = new JTextField();
        RmmcTheme.styleField(searchField);
        searchField.setToolTipText("Search by name or student ID");
        JButton searchButton = new JButton("Search");
        RmmcTheme.styleButton(searchButton, true);
        searchButton.addActionListener(e -> filterAttendance());
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchButton, BorderLayout.EAST);
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { filterAttendance(); }
            @Override public void removeUpdate(DocumentEvent e) { filterAttendance(); }
            @Override public void changedUpdate(DocumentEvent e) { filterAttendance(); }
        });
        attendancePanel.add(searchPanel, BorderLayout.NORTH);

        String[] columns = {"Name", "Course", "Student ID", "Time In", "Time Out", "Source"};
        attendanceTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        attendanceTable = new JTable(attendanceTableModel);
        RmmcTheme.styleTable(attendanceTable);
        attendanceTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        attendanceSorter = new TableRowSorter<>(attendanceTableModel);
        attendanceTable.setRowSorter(attendanceSorter);
        attendancePanel.add(new JScrollPane(attendanceTable), BorderLayout.CENTER);
        return attendancePanel;
    }

    private JPanel createOrganizerButtonPanel() {
        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 22, 16, 22));

        JPanel recordButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        recordButtons.setOpaque(false);
        editButton = new JButton("Edit");
        RmmcTheme.styleButton(editButton, false);
        editButton.addActionListener(this);
        deleteButton = new JButton("Delete");
        RmmcTheme.styleButton(deleteButton, false);
        deleteButton.addActionListener(this);
        addButton = new JButton("Add");
        RmmcTheme.styleButton(addButton, false);
        addButton.addActionListener(this);
        recordButtons.add(editButton);
        recordButtons.add(deleteButton);
        recordButtons.add(addButton);
        buttonPanel.add(recordButtons, BorderLayout.WEST);

        JPanel navigationButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        navigationButtons.setOpaque(false);
        importButton = new JButton("Import");
        RmmcTheme.styleButton(importButton, false);
        importButton.addActionListener(this);
        saveButton = new JButton("Save");
        RmmcTheme.styleButton(saveButton, true);
        saveButton.setEnabled(false);
        saveButton.addActionListener(this);
        backButton = new JButton("Back");
        RmmcTheme.styleButton(backButton, false);
        backButton.addActionListener(this);
        navigationButtons.add(importButton);
        navigationButtons.add(saveButton);
        navigationButtons.add(backButton);
        buttonPanel.add(navigationButtons, BorderLayout.EAST);
        return buttonPanel;
    }

    private void loadOrganizerData() {
        setAttendanceControlsEnabled(false);
        new SwingWorker<EventAndAttendance, Void>() {
            @Override
            protected EventAndAttendance doInBackground() {
                return new EventAndAttendance(eventRepository.findEventById(eventId),
                        attendanceService.findByEventId(eventId));
            }

            @Override
            protected void done() {
                setAttendanceControlsEnabled(true);
                try {
                    EventAndAttendance result = get();
                    if (result.event == null) {
                        JOptionPane.showMessageDialog(EventDetailsFrame.this,
                                "Event not found.", "Load Failed", JOptionPane.ERROR_MESSAGE);
                        dispose();
                        return;
                    }
                    event = result.event;
                    attendanceRows = new ArrayList<>(result.attendance);
                    showEventDetails();
                    refreshAttendanceTable();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(EventDetailsFrame.this,
                            "Could not load event attendance from the database.",
                            "Load Failed", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private static class EventAndAttendance {
        private final Event event;
        private final List<Attendance> attendance;

        private EventAndAttendance(Event event, List<Attendance> attendance) {
            this.event = event;
            this.attendance = attendance;
        }
    }

    private void showEventDetails() {
        eventTitleValue.setText(event.getEventName());
        eventDescriptionValue.setText(event.getDescription());
        eventDateValue.setText(event.getEventDateTime() == null ? "Not set" : event.getEventDateTime().toString());
        eventLocationValue.setText(event.getLocation());
        eventCapacityValue.setText(Integer.toString(event.getCapacity()));
        eventStatusValue.setText(event.getStatus());
    }

    private void refreshAttendanceTable() {
        attendanceTableModel.setRowCount(0);
        for (Attendance attendance : attendanceRows) {
            attendanceTableModel.addRow(new Object[]{
                    attendance.getName(), attendance.getCourse(), attendance.getStudentId(),
                    displayTime(attendance.getCheckIn()), displayTime(attendance.getCheckOut()),
                    getSourceLabel(attendance)
            });
        }
    }

    private String displayTime(LocalDateTime dateTime) {
        return dateTime == null ? "" : dateTime.format(TIME_DISPLAY);
    }

    private String getSourceLabel(Attendance attendance) {
        return attendance.getSource() == null || attendance.getSource().isBlank()
                ? "Existing"
                : attendance.getSource();
    }

    private void filterAttendance() {
        if (attendanceSorter == null) {
            return;
        }
        String text = searchField.getText().trim();
        attendanceSorter.setRowFilter(text.isEmpty() ? null
                : RowFilter.regexFilter("(?i)" + Pattern.quote(text), 0, 2));
    }

    private void setAttendanceControlsEnabled(boolean enabled) {
        if (editButton != null) editButton.setEnabled(enabled);
        if (deleteButton != null) deleteButton.setEnabled(enabled);
        if (addButton != null) addButton.setEnabled(enabled);
        if (importButton != null) importButton.setEnabled(enabled);
        if (saveButton != null) saveButton.setEnabled(enabled);
        if (backButton != null) backButton.setEnabled(enabled);
    }

    private int getSelectedAttendanceIndex() {
        int selectedRow = attendanceTable.getSelectedRow();
        return selectedRow < 0 ? -1 : attendanceTable.convertRowIndexToModel(selectedRow);
    }

    private void addAttendance() {
        JTextField nameField = new JTextField();
        JTextField courseField = new JTextField();
        JTextField studentIdField = new JTextField();
        JPanel form = createAttendanceForm(nameField, courseField, studentIdField, null, null);

        while (true) {
            int choice = JOptionPane.showConfirmDialog(this, form, "Add Attendance",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (choice != JOptionPane.OK_OPTION) {
                return;
            }
            String name = nameField.getText().trim();
            String course = courseField.getText().trim();
            String studentId = studentIdField.getText().trim();
            if (name.isEmpty() || course.isEmpty() || studentId.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Name, course, and student ID are required.",
                        "Missing Information", JOptionPane.WARNING_MESSAGE);
                continue;
            }
            if (findAttendanceByStudentId(studentId) >= 0) {
                JOptionPane.showMessageDialog(this, "This student already has an attendance row for this event.",
                        "Duplicate Student", JOptionPane.WARNING_MESSAGE);
                continue;
            }
            Attendance attendance = new Attendance(eventId, name, studentId, course,
                    LocalDateTime.now(), null, "");
            attendance.setSource("Added");
            attendanceRows.add(attendance);
            markAttendanceChanged();
            refreshAttendanceTable();
            return;
        }
    }

    private JPanel createAttendanceForm(JTextField name, JTextField course, JTextField studentId,
                                        JTextField checkIn, JTextField checkOut) {
        JPanel form = new JPanel(new GridLayout(checkIn == null ? 3 : 5, 2, 8, 8));
        form.add(new JLabel("Name:"));
        form.add(name);
        form.add(new JLabel("Course:"));
        form.add(course);
        form.add(new JLabel("Student ID:"));
        form.add(studentId);
        if (checkIn != null) {
            form.add(new JLabel("Time In (optional seconds):"));
            form.add(checkIn);
            form.add(new JLabel("Time Out (blank if none):"));
            form.add(checkOut);
        }
        return form;
    }

    private void editAttendance() {
        int selectedIndex = getSelectedAttendanceIndex();
        if (selectedIndex < 0) {
            JOptionPane.showMessageDialog(this, "Select an attendance row to edit.",
                    "No Row Selected", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Attendance attendance = attendanceRows.get(selectedIndex);
        JTextField nameField = new JTextField(attendance.getName());
        JTextField courseField = new JTextField(attendance.getCourse());
        JTextField studentIdField = new JTextField(attendance.getStudentId());
        JTextField checkInField = new JTextField(displayTime(attendance.getCheckIn()));
        JTextField checkOutField = new JTextField(displayTime(attendance.getCheckOut()));
        JPanel form = createAttendanceForm(nameField, courseField, studentIdField, checkInField, checkOutField);

        while (true) {
            int choice = JOptionPane.showConfirmDialog(this, form, "Edit Attendance",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (choice != JOptionPane.OK_OPTION) {
                return;
            }
            String studentId = studentIdField.getText().trim();
            if (nameField.getText().isBlank() || courseField.getText().isBlank() || studentId.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Name, course, and student ID are required.",
                        "Missing Information", JOptionPane.WARNING_MESSAGE);
                continue;
            }
            int duplicateIndex = findAttendanceByStudentId(studentId);
            if (duplicateIndex >= 0 && duplicateIndex != selectedIndex) {
                JOptionPane.showMessageDialog(this, "Another row already uses this student ID.",
                        "Duplicate Student", JOptionPane.WARNING_MESSAGE);
                continue;
            }
            try {
                LocalDateTime checkIn = parseEditedTime(checkInField.getText(), attendance.getCheckIn(), true);
                LocalDateTime checkOut = parseEditedTime(checkOutField.getText(), attendance.getCheckOut(), false);
                if (checkOut != null && checkOut.isBefore(checkIn)) {
                    throw new IllegalArgumentException("Time Out cannot be earlier than Time In.");
                }
                attendance.setName(nameField.getText().trim());
                attendance.setCourse(courseField.getText().trim());
                attendance.setStudentId(studentId);
                attendance.setCheckIn(checkIn);
                attendance.setCheckOut(checkOut);
                attendance.setDuration(calculateDuration(checkIn, checkOut));
                markAttendanceChanged();
                refreshAttendanceTable();
                return;
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Invalid Time", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private LocalDateTime parseEditedTime(String text, LocalDateTime currentValue, boolean required) {
        String value = text.trim();
        if (value.isEmpty()) {
            if (required) {
                throw new IllegalArgumentException("Time In is required.");
            }
            return null;
        }
        LocalTime time = parseTime(value);
        LocalDate date = currentValue == null ? LocalDate.now() : currentValue.toLocalDate();
        return LocalDateTime.of(date, time);
    }

    private LocalTime parseTime(String value) {
        List<DateTimeFormatter> formats = List.of(
                DateTimeFormatter.ofPattern("h:mm:ss a"),
                DateTimeFormatter.ofPattern("h:mm a"),
                DateTimeFormatter.ofPattern("H:mm:ss"),
                DateTimeFormatter.ofPattern("H:mm")
        );
        for (DateTimeFormatter format : formats) {
            try {
                return LocalTime.parse(value.toUpperCase(), format);
            } catch (RuntimeException ignored) {
                // Try the next common time format.
            }
        }
        throw new IllegalArgumentException("Enter a time such as 8:30:25 PM or 20:30.");
    }

    private String calculateDuration(LocalDateTime checkIn, LocalDateTime checkOut) {
        if (checkIn == null || checkOut == null || checkOut.isBefore(checkIn)) {
            return "";
        }
        Duration duration = Duration.between(checkIn, checkOut);
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();
        return hours + " hrs  " + minutes + " min  " + seconds + " seg";
    }

    private void deleteAttendance() {
        int selectedIndex = getSelectedAttendanceIndex();
        if (selectedIndex < 0) {
            JOptionPane.showMessageDialog(this, "Select an attendance row to delete.",
                    "No Row Selected", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Attendance attendance = attendanceRows.get(selectedIndex);
        int choice = JOptionPane.showConfirmDialog(this,
                "Delete attendance for " + attendance.getName() + " (" + attendance.getStudentId() + ")?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (choice == JOptionPane.YES_OPTION) {
            if (attendance.getId() != null) {
                deletedAttendanceIds.add(attendance.getId());
            }
            attendanceRows.remove(selectedIndex);
            markAttendanceChanged();
            refreshAttendanceTable();
        }
    }

    private void importAttendance() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Import Attendance CSV");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("CSV files", "csv"));
        if (fileChooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File selectedFile = fileChooser.getSelectedFile();
        try {
            attendanceRows = attendanceService.importCsv(selectedFile.toPath(), eventId, attendanceRows);
            markAttendanceChanged();
            refreshAttendanceTable();
            JOptionPane.showMessageDialog(this, "Attendance data imported. Press Save to store the changes.",
                    "Import Complete", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Could not import the CSV file: " + ex.getMessage(),
                    "Import Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private int findAttendanceByStudentId(String studentId) {
        for (int i = 0; i < attendanceRows.size(); i++) {
            if (attendanceRows.get(i).getStudentId().equalsIgnoreCase(studentId)) {
                return i;
            }
        }
        return -1;
    }

    private void markAttendanceChanged() {
        hasUnsavedChanges = true;
        saveButton.setEnabled(true);
    }

    private void saveAttendance(boolean closeWhenSaved) {
        if (!hasUnsavedChanges) {
            if (closeWhenSaved) dispose();
            return;
        }
        closingAfterSave = closeWhenSaved;
        setAttendanceControlsEnabled(false);
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() {
                return attendanceService.saveChanges(eventId, attendanceRows, deletedAttendanceIds);
            }

            @Override
            protected void done() {
                setAttendanceControlsEnabled(true);
                try {
                    if (!get()) {
                        JOptionPane.showMessageDialog(EventDetailsFrame.this,
                                "Could not save attendance. Check the student IDs and database connection, then try again.",
                                "Save Failed", JOptionPane.ERROR_MESSAGE);
                        closingAfterSave = false;
                        return;
                    }
                    hasUnsavedChanges = false;
                    deletedAttendanceIds.clear();
                    JOptionPane.showMessageDialog(EventDetailsFrame.this,
                            "Attendance saved successfully.", "Saved", JOptionPane.INFORMATION_MESSAGE);
                    boolean close = closingAfterSave;
                    closingAfterSave = false;
                    if (close) dispose();
                } catch (Exception ex) {
                    closingAfterSave = false;
                    JOptionPane.showMessageDialog(EventDetailsFrame.this,
                            "Could not save attendance.", "Save Failed", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void handleBack() {
        if (!hasUnsavedChanges) {
            dispose();
            return;
        }
        int choice = JOptionPane.showConfirmDialog(this,
                "You have unsaved attendance changes. Save before leaving?",
                "Unsaved Changes", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
        if (choice == JOptionPane.YES_OPTION) {
            saveAttendance(true);
        } else if (choice == JOptionPane.NO_OPTION) {
            dispose();
        }
    }

    // ==================================================
    // BUTTON ACTIONS
    // ==================================================

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == addButton) addAttendance();
        else if (e.getSource() == editButton) editAttendance();
        else if (e.getSource() == deleteButton) deleteAttendance();
        else if (e.getSource() == importButton) importAttendance();
        else if (e.getSource() == saveButton) saveAttendance(false);
        else if (e.getSource() == backButton) handleBack();
    }
}
