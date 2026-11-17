package javamongo1.sersproject.service;

import java.time.LocalDateTime;
import javamongo1.sersproject.model.Event;

public class EventService {
    
    //-------- Create Event --------
    public Event createEvent(String eventName, String description, LocalDateTime eventDateTime, String location, int capacity, String creator){
        
                  
            if(!eventNameValidation(eventName)){
                 return null;
            }

            if(!descriptionValidation(description)){
                 return null;
            }

            if(!eventDateTimeValidation(eventDateTime)){
                 return null;
            }

            if(!locationValidation(location)){
                 return null;
            }

            if(!capacityValidation(capacity)){
                 return null;
            }

            if(!creatorValidation(creator)){
                 return null;
            }
        
    
        return new Event(eventName, description, eventDateTime, location, capacity,creator);
    }
    
    
    
    //-------- Validation --------
    public boolean eventNameValidation(String title) {

        if (title == null || title.isBlank()) {
            return false;
        }
        
        if (!title.matches("[a-zA-Z0-9 .,!?'-]+")) {
            return false;
        }
        
        return true;
    }
    
    public boolean descriptionValidation(String description){
        if (description == null || description.isBlank()) {
            return false;
        }
        
        return true;
    }
    
    public boolean eventDateTimeValidation(LocalDateTime eventDateTime){
        if(eventDateTime == null){
            return false;
        }
        
        if(eventDateTime.isBefore(LocalDateTime.now())){
            return false;
        }
        
        return true;
    }
    
    public boolean locationValidation(String location){
        
        if(location == null || location.isBlank()){
            return false;
        }
        
        return true;
    }
    
    public boolean capacityValidation(int capacity){
        if(capacity <= 0){
            return false;
        }
        return true;
    }
    
    public boolean creatorValidation(String creator) {

        if (creator == null || creator.isBlank()) {
            return false;
        }

        return true;
    }
    
    public boolean staffStatusValidation(String staffStatus)    {

        if (staffStatus == null || staffStatus.isBlank()) {
            return false;
        }

        return staffStatus.equalsIgnoreCase("APPROVED") || staffStatus.equalsIgnoreCase("REJECTED");
    }
     
    public boolean  staffReviewerValidation(String staffReviewer){
        
        if (staffReviewer == null || staffReviewer.isBlank()) {
            return false;
        }
        
        return true;
    }
    
    
    
    //-------- Service --------
    public boolean staffReviewEvent(Event event, String staffReviewer, String staffStatus) {

        if (event == null) {
            return false;
        }

        if (!staffReviewerValidation(staffReviewer)) {
            return false;
        }

        if (!staffStatusValidation(staffStatus)) {
            return false;
        }

        // Staff can only review an event that is still pending
        if (!event.getStaffStatus().equalsIgnoreCase("PENDING")) {
            return false;
        }

        event.setStaffReviewer(staffReviewer);
        event.setStaffStatus(staffStatus.toUpperCase());

        return true;
    }


    public boolean adminApproval(Event event) {

        if (event == null) {
            return false;
        }

        // Admin can only approve an event after staff approves it
        if (!event.getStaffStatus().equalsIgnoreCase("APPROVED")) {
            return false;
        }

        // Event must still be waiting for admin approval
        if (!event.getApproval().equalsIgnoreCase("PENDING")) {
            return false;
        }

        event.setApproval("APPROVED");

        return true;
    }


    public boolean adminRejection(Event event) {

        if (event == null) {
            return false;
        }

        // Admin can only reject an event after staff approves it
        if (!event.getStaffStatus().equalsIgnoreCase("APPROVED")) {
            return false;
        }

        // Event must still be waiting for admin approval
        if (!event.getApproval().equalsIgnoreCase("PENDING")) {
            return false;
        }

        event.setApproval("REJECTED");

        return true;
    }


    public boolean updateEvent(Event event,String title,String description,LocalDateTime eventDateTime,String location, int capacity) {

        if (event == null) {
            return false;
        }
        
        if(event.getStatus().equalsIgnoreCase("COMPLETED")){
            return false;
        }
        
        if(event.getStatus().equalsIgnoreCase("CANCELLED")){
            return false;
        }
        
        if (!eventNameValidation(title)) {
            return false;
        }

        if (!descriptionValidation(description)) {
            return false;
        }

        if (!eventDateTimeValidation(eventDateTime)) {
            return false;
        }

        if (!locationValidation(location)) {
            return false;
        }

        if (!capacityValidation(capacity)) {
            return false;
        }

        // Apply the updated information
        event.setEventName(title);
        event.setDescription(description);
        event.setEventDateTime(eventDateTime);
        event.setLocation(location);
        event.setCapacity(capacity);

        return true;
    }


    public boolean cancelEvent(Event event) {

        if (event == null) {
            return false;
        }

        // A completed event cannot be cancelled
        if (event.getStatus().equalsIgnoreCase("COMPLETED")) {
            return false;
        }

        // An already cancelled event cannot be cancelled again
        if (event.getStatus().equalsIgnoreCase("CANCELLED")) {
            return false;
        }

        event.setStatus("CANCELLED");

        return true;
    }
    
}
