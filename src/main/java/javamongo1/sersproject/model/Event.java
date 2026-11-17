package javamongo1.sersproject.model;

import java.time.LocalDateTime;

public class Event {

    // System event ID, separate from MongoDB's internal _id
    private int eventId;

    private String eventName;
    private String description;
    private LocalDateTime eventDateTime;
    private String location;

    private int capacity;

    // Approval: APPROVED, PENDING, REJECTED
    // Status: CANCELLED, UPCOMING, ONGOING, COMPLETED
    private String approval;
    private String status;
    private String creator;

    // Staff review
    private String staffReviewer;
    private String staffStatus;

    // No-argument constructor
    public Event() {
    }

    // Main constructor
    public Event(String eventName, String description,
                 LocalDateTime eventDateTime,
                 String location,
                 int capacity,
                 String creator) {

        this.eventName = eventName;
        this.description = description;
        this.eventDateTime = eventDateTime;
        this.location = location;
        this.capacity = capacity;

        this.approval = "PENDING";
        this.status = "UPCOMING";

        this.creator = creator;
        this.staffReviewer = null;
        this.staffStatus = "PENDING";
    }

    // Getters
    public int getId() {
        return eventId;
    }

    public String getEventName() {
        return eventName;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getEventDateTime() {
        return eventDateTime;
    }

    public String getLocation() {
        return location;
    }

    public int getCapacity() {
        return capacity;
    }

    public String getApproval() {
        return approval;
    }

    public String getStatus() {
        return status;
    }

    public String getCreator() {
        return creator;
    }

    public String getStaffReviewer() {
        return staffReviewer;
    }

    public String getStaffStatus() {
        return staffStatus;
    }

    
    
    
    
    // Setters
    public void setId(int eventId) {
        this.eventId = eventId;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setEventDateTime(LocalDateTime eventDateTime) {
        this.eventDateTime = eventDateTime;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public void setApproval(String approval) {
        this.approval = approval;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public void setStaffReviewer(String staffReviewer) {
        this.staffReviewer = staffReviewer;
    }

    public void setStaffStatus(String staffStatus) {
        this.staffStatus = staffStatus;
    }
}
