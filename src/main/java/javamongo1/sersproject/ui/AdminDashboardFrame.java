package javamongo1.sersproject.ui;

import javamongo1.sersproject.model.Attendance;
import javamongo1.sersproject.model.Event;
import javamongo1.sersproject.model.User;
import javamongo1.sersproject.repository.AttendanceRepository;
import javamongo1.sersproject.repository.EventRepository;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class AdminDashboardFrame extends JFrame {
    // ==================================================
    // 1. FIELDS
    // ==================================================

    private final User adminUser;
    private final CardLayout cards = new CardLayout();
    private final JPanel workspace = new JPanel(cards);
    
    private final EventRepository eventRepository = new EventRepository();
    private final AttendanceRepository attendanceRepository = new AttendanceRepository();
    
    private final DateTimeFormatter displayDate = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    
    private JTable currentTable, upcomingTable, passedTable, cancelledTable;
    private DefaultTableModel currentModel, upcomingModel, passedModel, cancelledModel;
    private JTextField titleField, locationField, capacityField;
    private JSpinner yearSpinner, monthSpinner, daySpinner, hourSpinner, minuteSpinner;
    private JTextArea descriptionArea;
    private JComboBox<String> approvalBox;
    private JButton editButton;
    private Event editingEvent;
    private JTable attendanceTable;
    private DefaultTableModel eventChoiceModel, attendanceModel;
    private JTable upcomingRegistrationTable, passedRegistrationTable;
    private DefaultTableModel upcomingRegistrationModel, passedRegistrationModel;

    // ==================================================
    // 2. FRAME AND DASHBOARD LAYOUT
    // ==================================================

    public AdminDashboardFrame() {
        this(null);
    }

    public AdminDashboardFrame(User adminUser) {
        if (adminUser != null && !"ADMIN".equalsIgnoreCase(adminUser.getRole())) {
            throw new IllegalArgumentException("Admin access is required.");
        }
        
        this.adminUser = adminUser;
            setTitle("Admin Dashboard - SERS");
            setSize(1250, 800);
            setMinimumSize(new Dimension(1000, 680));
            setExtendedState(JFrame.MAXIMIZED_BOTH);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setLocationRelativeTo(null);
            setLayout(new BorderLayout());
            
        RmmcTheme.styleFrame(this);
        RmmcTheme.installBackground(this, 2);

        JPanel shell = new JPanel(new BorderLayout());
        shell.setOpaque(false);
        
        String subtitle = "School Event Registration System";
        
            if (adminUser != null && adminUser.getName() != null && !adminUser.getName().isBlank()) {
                subtitle += "  •  Welcome, " + adminUser.getName();
            }
            
        shell.add(RmmcTheme.header("Admin Dashboard", subtitle), BorderLayout.NORTH);
        
            JPanel navigation = createNavigation();
            
            shell.add(navigation, BorderLayout.WEST);
                workspace.add(createWelcomePanel(), "home");
                workspace.add(createEventPanel(), "events");
                workspace.add(createRegistrationPanel(), "registrations");
                workspace.add(new UserManagementFrame().createManagementPanel(), "users");
                
            shell.add(workspace, BorderLayout.CENTER);
            workspace.setOpaque(false);
            add(shell, BorderLayout.CENTER);
            cards.show(workspace, "home");
    }

    // ==================================================
    // 3. NAVIGATION
    // ==================================================

    private JPanel createNavigation() {
        JPanel nav = new JPanel(new BorderLayout(0, 12));
            nav.setOpaque(false);
            nav.setBorder(BorderFactory.createEmptyBorder(20, 14, 16, 14));
            nav.setPreferredSize(new Dimension(190, 0));
            
        JPanel buttons = new JPanel();
            buttons.setLayout(new BoxLayout(buttons, BoxLayout.Y_AXIS));
            buttons.setOpaque(false);
            
            addNavButton(buttons, "User Management", "users");
            addNavButton(buttons, "Event Management", "events");
            addNavButton(buttons, "Registration Management", "registrations");
            
        buttons.add(Box.createVerticalGlue());
        
            nav.add(new JLabel("Admin", SwingConstants.CENTER), BorderLayout.NORTH);
            nav.add(buttons, BorderLayout.CENTER);
            JButton logout = new JButton("Log out");
            
        RmmcTheme.styleButton(logout, false);
            logout.setFont(new Font("SansSerif", Font.PLAIN, 12));
            logout.setPreferredSize(new Dimension(145, 34));
            logout.addActionListener(e -> {
                int answer = JOptionPane.showConfirmDialog(this, "Are you sure you want to log out?", "Log out", JOptionPane.YES_NO_OPTION);
                if (answer == JOptionPane.YES_OPTION) { new LoginFrame().setVisible(true); dispose(); }
            });
        nav.add(logout, BorderLayout.SOUTH);
        
        return nav;
    }

    private void addNavButton(JPanel container,String label,String card){
        JButton button=navButton(label,card);
            button.setAlignmentX(Component.CENTER_ALIGNMENT);
            button.setMaximumSize(new Dimension(155,36));
            container.add(button);
            container.add(Box.createVerticalStrut(8));
    }

    private JButton navButton(String label, String card) {
        
        JButton button = new JButton(label);
        
            RmmcTheme.styleButton(button, true);
            
            button.setFont(new Font("SansSerif", Font.PLAIN, 12));
            button.setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));
            button.setPreferredSize(new Dimension(150, 34));
            button.addActionListener(e -> {
                if ("events".equals(card)) refreshEvents();
                
                if ("registrations".equals(card)) refreshRegistrationEvents();
                cards.show(workspace, card);
            });
            
        return button;
    }

    private JPanel createWelcomePanel() {
        JPanel panel = new JPanel(new BorderLayout());
            panel.setOpaque(false);
            panel.setBorder(BorderFactory.createEmptyBorder(30, 35, 30, 35));
        
        JLabel text = new JLabel("Choose a management section from the left.");
            text.setFont(new Font("SansSerif", Font.PLAIN, 20));
            panel.add(text, BorderLayout.NORTH);
        return panel;
    }

    // ==================================================
    // 4. EVENT MANAGEMENT UI
    // ==================================================

    private JPanel createEventPanel() {
        JPanel eventPanel = new JPanel(new BorderLayout(14, 0));
               eventPanel.setOpaque(false);
               eventPanel.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
               eventPanel.add(createEventForm(), BorderLayout.WEST);
               eventPanel.add(createEventListPanel(), BorderLayout.CENTER);
            refreshEvents();
            
        return eventPanel;
    }

    private JPanel createEventForm() {
        JPanel form = new JPanel(new BorderLayout(0, 10));
            form.setPreferredSize(new Dimension(370, 0));
            form.setBackground(RmmcTheme.BLUE);
            form.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
            
        JLabel heading = new JLabel("Event Details");
            heading.setForeground(Color.WHITE); heading.setFont(new Font("SansSerif", Font.BOLD, 20));
            form.add(heading, BorderLayout.NORTH);
        
        JPanel fields = new JPanel(new GridBagLayout()); fields.setOpaque(false);
        
        GridBagConstraints c = new GridBagConstraints(); c.gridx=0; c.weightx=1; c.fill=GridBagConstraints.HORIZONTAL; c.insets=new Insets(4,0,4,0);
        
            titleField = addField(fields,c,0,"Title",new JTextField());
        
        addDateTimeSelectors(fields, c, 2);
        
            locationField = addField(fields,c,4,"Location",new JTextField());
            
        JLabel capacityLabel = whiteLabel("Capacity"); c.gridy=6; fields.add(capacityLabel,c);
        
            capacityField = new JTextField("100");
            
        ((javax.swing.text.AbstractDocument)capacityField.getDocument()).setDocumentFilter(new javax.swing.text.DocumentFilter(){
            
            @Override public void insertString(FilterBypass fb,int offset,String text,javax.swing.text.AttributeSet attr)throws javax.swing.text.BadLocationException{
                if(text!=null&&text.matches("\\d*"))super.insertString(fb,offset,text,attr);
            }
            
            @Override public void replace(FilterBypass fb,int offset,int length,String text,javax.swing.text.AttributeSet attr)throws javax.swing.text.BadLocationException{
                if(text!=null&&text.matches("\\d*"))super.replace(fb,offset,length,text,attr);
            }
        
        });
        
            c.gridy=7; 
            RmmcTheme.styleField(capacityField); 
            fields.add(capacityField,c);
        
        JLabel descriptionLabel = whiteLabel("Description"); 
            c.gridy=8; 
            fields.add(descriptionLabel,c);
            
            descriptionArea = new JTextArea(9,20); 
            descriptionArea.setLineWrap(true); 
            descriptionArea.setWrapStyleWord(true);
        
            c.gridy=9; 
            c.weighty=1; 
            c.fill=GridBagConstraints.BOTH; 
            fields.add(new JScrollPane(descriptionArea),c); 
            c.weighty=0; 
            c.fill=GridBagConstraints.HORIZONTAL;
            
            
        JLabel approvalLabel = whiteLabel("Approval"); 
        
            c.gridy=10; fields.add(approvalLabel,c);
            approvalBox = new JComboBox<>(new String[]{"PENDING", "APPROVED"}); 
            c.gridy=11; fields.add(approvalBox,c);
            
        form.add(fields,BorderLayout.CENTER);
        
        JPanel actions = new JPanel(new GridLayout(2,2,6,6)); 
               actions.setOpaque(false);
        
        JButton add = new JButton("Add"); 
            editButton = new JButton("Edit"); 
        JButton delete = new JButton("Delete"); 
        JButton refresh = new JButton("Refresh");
        
        for(JButton b: List.of(add,editButton,delete,refresh)) { 
            RmmcTheme.styleButton(b,b==add); 
            b.setFont(new Font("SansSerif",Font.PLAIN,12)); 
            b.setBorder(BorderFactory.createEmptyBorder(4,8,4,8)); 
        }
        
            actions.add(add); 
            actions.add(editButton); 
            actions.add(delete); 
            actions.add(refresh);
        
        form.add(actions,BorderLayout.SOUTH);
        add.addActionListener(e -> confirmAddEvent()); 
            editButton.addActionListener(e -> {
                if(editingEvent==null)beginEdit();
                else saveEvent();
            }); 
        
            delete.addActionListener(e -> deleteEvent()); 
            refresh.addActionListener(e -> refreshEvents());
            
        return form;
    }

    private JTextField addField(JPanel panel, GridBagConstraints c, int y, String label, JTextField field) {
            c.gridy=y; panel.add(whiteLabel(label),c); 
            c.gridy=y+1; RmmcTheme.styleField(field); 
            panel.add(field,c); 
            
        return field;
    }
    
    private JLabel whiteLabel(String text) { 
        JLabel label=new JLabel(text); 
        label.setForeground(Color.WHITE); 
        
        return label; 
    }
    
    private void addDateTimeSelectors(JPanel fields, GridBagConstraints c, int y) {
        
        c.gridy=y; 
        fields.add(whiteLabel("Date and time"),c);
        
        JPanel selectors=new JPanel(new GridLayout(2,5,4,4)); 
        selectors.setOpaque(false);
        LocalDateTime now=LocalDateTime.now();
        
            yearSpinner=new JSpinner(new SpinnerNumberModel(now.getYear(),now.getYear(),now.getYear()+10,1));
            monthSpinner=new JSpinner(new SpinnerNumberModel(now.getMonthValue(),1,12,1));
            daySpinner=new JSpinner(new SpinnerNumberModel(now.getDayOfMonth(),1,31,1));
            hourSpinner=new JSpinner(new SpinnerNumberModel(now.getHour(),0,23,1));
            minuteSpinner=new JSpinner(new SpinnerNumberModel((now.getMinute()/5)*5,0,55,5));
            monthSpinner.addChangeListener(e->updateDayLimit());
            
            yearSpinner.addChangeListener(e->updateDayLimit());
            
        for(String label:List.of("Year","Month","Day","Hour","Minute"))selectors.add(whiteLabel(label));
        
        for(JSpinner spinner:List.of(yearSpinner,monthSpinner,daySpinner,hourSpinner,minuteSpinner))selectors.add(spinner);
        
        c.gridy=y+1;fields.add(selectors,c);
    }

    private JPanel createEventListPanel(){
        JPanel panel=new JPanel(new BorderLayout(0,10));panel.setOpaque(false);
            currentModel=eventModel();
            currentTable=eventTable(currentModel);
            upcomingModel=eventModel();
            upcomingTable=eventTable(upcomingModel);
            passedModel=eventModel();
            passedTable=eventTable(passedModel);
            cancelledModel=eventModel();
            cancelledTable=eventTable(cancelledModel);
            
        configureExclusiveSelection(currentTable,upcomingTable,passedTable,cancelledTable);
        
        JPanel sections=new JPanel(new GridLayout(4,1,0,6));sections.setOpaque(false);
               sections.add(eventSection("Current Events",currentTable));
               sections.add(eventSection("Upcoming Events",upcomingTable));
               sections.add(eventSection("Passed Events",passedTable));
               sections.add(eventSection("Cancelled Events",cancelledTable));
               panel.add(sections,BorderLayout.CENTER);
                
                return panel;
        }
    
    private DefaultTableModel eventModel(){
        return new DefaultTableModel(new Object[]{"Event ID","Title","Date & Time","Location","Approval"},0){
            public boolean isCellEditable(int r,int c){
                return false;
            }};
    }
    
    private JTable eventTable(DefaultTableModel model){
        
        JTable table=new JTable(model);
            RmmcTheme.styleTable(table);
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        return table;
        
    }
    
    private void configureExclusiveSelection(JTable... tables){
        for(JTable selected:tables)selected.getSelectionModel().addListSelectionListener(e->{
            if(e.getValueIsAdjusting()||selected.getSelectedRow()<0)return;
            for(JTable other:tables)if(other!=selected)other.clearSelection();
        });
    }
    
    private JPanel eventSection(String title,JTable table){
        JPanel section=new JPanel(new BorderLayout(0,3));
            section.setOpaque(false);
            JLabel heading=new JLabel(title);
            heading.setFont(new Font("SansSerif",Font.BOLD,14));
            section.add(heading,BorderLayout.NORTH);
            section.add(new JScrollPane(table),BorderLayout.CENTER);

        return section;
    }

    // ==================================================
    // 5. EVENT ACTIONS AND DATA
    // ==================================================

    private void confirmAddEvent() {
        int answer = JOptionPane.showConfirmDialog(
                this,
                "Add this event to Upcoming Events?",
                "Confirm Add Event",
                JOptionPane.YES_NO_OPTION
        );
        
        if (answer == JOptionPane.YES_OPTION) {
            editingEvent = null;
            saveEvent();
        }
    }
    
    private void beginEdit() {
        Event selected=selectedMutableEvent(); 
            if(selected==null){message("Select a current or upcoming event first.");
            
                return;
            }
            
        editingEvent=selected; titleField.setText(selected.getEventName()); 
        setDateTimeSelectors(selected.getEventDateTime()); 
        locationField.setText(selected.getLocation()); 
        capacityField.setText(Integer.toString(selected.getCapacity())); 
        descriptionArea.setText(selected.getDescription()); 
        approvalBox.setSelectedItem(selected.getApproval()); 
        editButton.setText("Update");
    }
    
    private void clearForm(){
        titleField.setText("");
        locationField.setText("");
        descriptionArea.setText("");
        capacityField.setText("100");
        approvalBox.setSelectedItem("PENDING");
        setDateTimeSelectors(LocalDateTime.now().plusDays(1));
    }
    
    private void setDateTimeSelectors(LocalDateTime date){
        yearSpinner.setValue(date.getYear());
        monthSpinner.setValue(date.getMonthValue());
        daySpinner.setValue(date.getDayOfMonth());
        hourSpinner.setValue(date.getHour());minuteSpinner.setValue(date.getMinute());
    }
    
    private void updateDayLimit(){
        if(yearSpinner==null||monthSpinner==null||daySpinner==null)return;
        
        int year=(Integer)yearSpinner.getValue(),month=(Integer)monthSpinner.getValue();
        
        int max=java.time.YearMonth.of(year,month).lengthOfMonth();
        
        SpinnerNumberModel model=(SpinnerNumberModel)daySpinner.getModel();
        
        int day=(Integer)model.getValue();
            model.setMaximum(max);
        
            if(day>max)model.setValue(max);
    }
    
    private LocalDateTime getSelectedDateTime(){
        
        return LocalDateTime.of((Integer)yearSpinner.getValue(),
                                (Integer)monthSpinner.getValue(),
                                (Integer)daySpinner.getValue(),
                                (Integer)hourSpinner.getValue(),
                                (Integer)minuteSpinner.getValue());
    }
    
    private void saveEvent() {
        
        try {
            String title=titleField.getText().trim(), 
                location=locationField.getText().trim(), 
             description=descriptionArea.getText().trim();
            
            LocalDateTime date=getSelectedDateTime(); 
            
            int capacity=capacityField.getText().isBlank()?0:Integer.parseInt(capacityField.getText());
            
            if(title.isBlank()){message("Enter an event title.");
                titleField.requestFocusInWindow();
                return;
            }
            
            if(location.isBlank()){message("Enter the event location.");
                locationField.requestFocusInWindow();
                return;
            }
            
            if(description.isBlank()){message("Enter an event description.");
                descriptionArea.requestFocusInWindow();
                return;
            }
            
            if(capacity<=0){message("Enter a capacity greater than zero.");
                capacityField.requestFocusInWindow();
                return;
            }
            
            if(date.isBefore(LocalDateTime.now())){message("Choose a date and time in the future. For events happening today, choose a later time.");
                return;
            }
            
            boolean saved;
            
            if(editingEvent==null){
                Event event=new Event(title,description,date,location,capacity,"Admin"); 
                event.setApproval((String)approvalBox.getSelectedItem()); 
                event.setStatus("UPCOMING"); saved=eventRepository.createEvent(event);
            }
            
            else { editingEvent.setEventName(title);
                editingEvent.setDescription(description);
                editingEvent.setEventDateTime(date);
                editingEvent.setLocation(location);
                editingEvent.setCapacity(capacity);
                editingEvent.setApproval((String)approvalBox.getSelectedItem()); 
                saved=eventRepository.updateEvent(editingEvent.getId(),editingEvent); 
            }
            
            if(!saved){message("The event could not be saved. Check that MongoDB is running and try again.");
                return;
            }
            
                clearForm();
                editingEvent=null;
                editButton.setText("Edit");
                refreshEvents();
                
        } catch(java.time.DateTimeException ex){
            message("The selected date is invalid. Check the month and day.");
        }catch(Exception ex){
            message("The event could not be saved because one of the entered values is invalid. Check the fields and try again.");
        }
    }
    
    private Event selectedMutableEvent(){
        if(passedTable.getSelectedRow()>=0||cancelledTable.getSelectedRow()>=0)return null;
        
            int row=currentTable.getSelectedRow();
            
        if(row>=0)return eventRepository.findEventById((Integer)currentModel.getValueAt(currentTable.convertRowIndexToModel(row),0));
        
            row=upcomingTable.getSelectedRow();
            
        if(row>=0)return eventRepository.findEventById((Integer)upcomingModel.getValueAt(upcomingTable.convertRowIndexToModel(row),0));
        
        return null;
    }
    
    private void deleteEvent(){
        Event selected=selectedAnyEvent();
        
        if(selected==null){
            message("Select an event from any category first.");
            return;
        }
        
        Object[] destinations={"Move to Passed","Move to Cancelled","Keep Event"};
        
        int choice=JOptionPane.showOptionDialog(this,"Move ‘"+selected.getEventName()+"’ to which category?","Move Event",JOptionPane.DEFAULT_OPTION,JOptionPane.QUESTION_MESSAGE,null,destinations,destinations[0]);
        
        if(choice==0||choice==1){
            selected.setStatus(choice==0?"COMPLETED":"CANCELLED");
            
            if(eventRepository.updateEvent(selected.getId(),selected))refreshEvents();
            else message("Could not move the event.");
            
        }
    }
    
    private Event selectedAnyEvent(){
        Event selected=selectedMutableEvent();
        
            if(selected!=null)return selected;
            
        JTable[] tables={passedTable,cancelledTable};
        DefaultTableModel[] models={passedModel,cancelledModel};
        
            for(int i=0;i<tables.length;i++){int row=tables[i].getSelectedRow();if(row>=0)return eventRepository.findEventById((Integer)models[i].getValueAt(tables[i].convertRowIndexToModel(row),0));}
       
        return null;
    }
    
    private void refreshEvents(){
        if(currentModel==null)return;
        
        currentModel.setRowCount(0);
        upcomingModel.setRowCount(0);
        passedModel.setRowCount(0);
        cancelledModel.setRowCount(0);
        
        LocalDateTime now=LocalDateTime.now();
        
        try{
            for(Event e:eventRepository.findAllEvents()){
                String status=e.getStatus()==null?"":e.getStatus().toUpperCase();
                String category;
                
                    if("CANCELLED".equals(status))category="Cancelled Events";
                    else if("ONGOING".equals(status)&&e.getEventDateTime()!=null&&e.getEventDateTime().toLocalDate().equals(now.toLocalDate()))category="Current Event";
                    else if("COMPLETED".equals(status)||(e.getEventDateTime()!=null&&e.getEventDateTime().isBefore(now)))category="Passed Events";
                    else if(e.getEventDateTime()!=null&&e.getEventDateTime().toLocalDate().equals(now.toLocalDate()))category="Current Event";
                    else category="Upcoming Event";
                    
                    if("Passed Events".equals(category)&&!"COMPLETED".equals(status)){
                        e.setStatus("COMPLETED");
                        eventRepository.updateEvent(e.getId(),e);
                    }
                    
                    DefaultTableModel target="Current Event".equals(category)?currentModel:"Upcoming Event".equals(category)?upcomingModel:"Passed Events".equals(category)?passedModel:cancelledModel;
                    target.addRow(new Object[]{e.getId(),e.getEventName(),e.getEventDateTime()==null?"":e.getEventDateTime().format(displayDate),e.getLocation(),e.getApproval()});
            }
        }catch(Exception ex){
            message("Could not load events from the database.");
        }
    }
    
    
    // ==================================================
    // 6. REGISTRATION MANAGEMENT
    // ==================================================

    private JPanel createRegistrationPanel(){
        
        JPanel registrationPanel=new JPanel(new BorderLayout(12,0));
               registrationPanel.setOpaque(false);
               registrationPanel.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
                
        upcomingRegistrationModel=registrationEventModel();
        passedRegistrationModel=registrationEventModel();
        upcomingRegistrationTable=registrationEventTable(upcomingRegistrationModel);
        passedRegistrationTable=registrationEventTable(passedRegistrationModel);
        
        attendanceModel=new DefaultTableModel(new Object[]{"Name","Course","Student ID","Time In","Time Out","Source"},0){
            public boolean isCellEditable(int r,int c){return false;}
        };
        
        attendanceTable=new JTable(attendanceModel);
        
        RmmcTheme.styleTable(attendanceTable);
        attendanceTable.setAutoCreateRowSorter(true);
        JTable[] registrationTables={upcomingRegistrationTable,passedRegistrationTable};
        for(JTable table:registrationTables)table.getSelectionModel().addListSelectionListener(e->{
            if(!e.getValueIsAdjusting()&&table.getSelectedRow()>=0){
                for(JTable other:registrationTables)if(other!=table)other.clearSelection();
                loadAttendance(table);
            }
        });
        
        JPanel left=new JPanel(new BorderLayout(0,8));
               left.setOpaque(false);left.setPreferredSize(new Dimension(390,0));
               left.add(new JLabel("Select an event"),BorderLayout.NORTH);
        JPanel eventSections=new JPanel(new GridLayout(2,1,0,8));
        eventSections.setOpaque(false);
        eventSections.add(registrationEventSection("Upcoming Events",upcomingRegistrationTable));
        eventSections.add(registrationEventSection("Passed Events",passedRegistrationTable));
        left.add(eventSections,BorderLayout.CENTER);
        
        JPanel right=new JPanel(new BorderLayout(0,8));right.setOpaque(false);
               right.add(new JLabel("Participants (click column headers to sort)"),BorderLayout.NORTH);
               right.add(new JScrollPane(attendanceTable),BorderLayout.CENTER);
        
        registrationPanel.add(left,BorderLayout.WEST);
        registrationPanel.add(right,BorderLayout.CENTER);
        refreshRegistrationEvents();
        return registrationPanel;
    }
    
    private DefaultTableModel registrationEventModel(){
        return new DefaultTableModel(new Object[]{"Event ID","Event Title","Date & Time","Location"},0){
            @Override public boolean isCellEditable(int r,int c){return false;}
        };
    }

    private JTable registrationEventTable(DefaultTableModel model){
        JTable table=new JTable(model);
        RmmcTheme.styleTable(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        return table;
    }

    private JPanel registrationEventSection(String title,JTable table){
        JPanel section=new JPanel(new BorderLayout(0,4));
        section.setOpaque(false);
        JLabel heading=new JLabel(title);
        heading.setFont(heading.getFont().deriveFont(Font.BOLD));
        section.add(heading,BorderLayout.NORTH);
        section.add(new JScrollPane(table),BorderLayout.CENTER);
        return section;
    }

    private void refreshRegistrationEvents(){
        if(upcomingRegistrationModel==null)return;
        upcomingRegistrationTable.clearSelection();
        passedRegistrationTable.clearSelection();
        attendanceModel.setRowCount(0);
        upcomingRegistrationModel.setRowCount(0);
        passedRegistrationModel.setRowCount(0);
        try{
            LocalDateTime now=LocalDateTime.now();
            for(Event event:eventRepository.findAllEvents()){
                String status=event.getStatus()==null?"":event.getStatus().toUpperCase(Locale.ROOT);
                if("CANCELLED".equals(status))continue;
                DefaultTableModel target;
                if("COMPLETED".equals(status)||(event.getEventDateTime()!=null&&event.getEventDateTime().isBefore(now)))target=passedRegistrationModel;
                else target=upcomingRegistrationModel;
                target.addRow(new Object[]{event.getId(),event.getEventName(),event.getEventDateTime()==null?"":event.getEventDateTime().format(displayDate),event.getLocation()});
            }
        }catch(Exception ex){message("Could not load events.");}
    }

    private void loadAttendance(JTable sourceTable){
        attendanceModel.setRowCount(0);
        int row=sourceTable.getSelectedRow();
        if(row<0)return;
        int id=(Integer)sourceTable.getModel().getValueAt(sourceTable.convertRowIndexToModel(row),0);
        try{for(Attendance a:attendanceRepository.findByEventId(id))attendanceModel.addRow(new Object[]{a.getName(),a.getCourse(),a.getStudentId(),format(a.getCheckIn()),format(a.getCheckOut()),a.getSource()==null?"Existing":a.getSource()});}catch(Exception ex){message("Could not load attendance records.");}
    }

    private String format(LocalDateTime value){return value==null?"":value.format(displayDate);}
    private void message(String text){JOptionPane.showMessageDialog(this,text,"Admin Dashboard",JOptionPane.INFORMATION_MESSAGE);}

}
