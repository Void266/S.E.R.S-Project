
package javamongo1.sersproject.repository;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.client.result.DeleteResult;

import javamongo1.sersproject.config.MongoConnection;
import javamongo1.sersproject.model.Event;

import org.bson.Document;

// Java imports
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


public class EventRepository {

    // ==================================================
    // 1. FIELDS AND DATABASE CONNECTION
    // ==================================================
    private final MongoCollection<Document> collection;
    private final MongoCollection<Document> counters;


    
    
    
    // ==================================================
    // 2. CONSTRUCTORS
    // ==================================================

    // Default constructor: connects to the events collection.
    public EventRepository() {
        this(MongoConnection.getCollection("events"));
    }

    // Constructor: accepts a collection and gets the counters collection.
    public EventRepository(MongoCollection<Document> collection) {
        this.collection = collection;
        this.counters = MongoConnection.getCollection("counters");

        initializeCounter();
    }


    
    
    
    // ==================================================
    // 3. COUNTER AND EVENT ID GENERATION
    // ==================================================

    // Prepare the event ID counter without resetting an existing counter.
    private void initializeCounter() {

        Document highestEvent = collection.find()
                .sort(Sorts.descending("eventId"))
                .first();

        int highestId = 0;

        if (highestEvent != null) {

            Object eventIdValue = highestEvent.get("eventId");

            if (eventIdValue instanceof Number) {

                highestId = ((Number) eventIdValue).intValue();

            } else if (eventIdValue instanceof String) {

                try {
                    highestId = Integer.parseInt((String) eventIdValue);
                } catch (NumberFormatException e) {
                    // Ignore non-numeric legacy IDs such as "E001".
                    highestId = 0;
                }
            }
        }

        counters.updateOne(
                Filters.eq("_id", "eventId"),
                Updates.max("seq", highestId),
                new UpdateOptions().upsert(true)
        );
    }


    // Generate the next integer event ID.
    private int generateEventId() {
        Document counter = counters.findOneAndUpdate(
                Filters.eq("_id", "eventId"),
                Updates.inc("seq", 1),
                new FindOneAndUpdateOptions()
                        .upsert(true)
                        .returnDocument(ReturnDocument.AFTER)
        );

        return counter.getInteger("seq");
    }


    
    
    
    // ==================================================
    // 4. DATA CONVERSION
    // ==================================================

    // Convert an Event object into a MongoDB document.
    private Document toDocument(Event event) {
        return new Document("eventId", event.getId())
                .append("eventName", event.getEventName())
                .append("description", event.getDescription())
                .append(
                        "eventDateTime",
                        event.getEventDateTime() == null
                                ? null
                                : event.getEventDateTime().toString()
                )
                .append("location", event.getLocation())
                .append("capacity", event.getCapacity())
                .append("approval", event.getApproval())
                .append("status", event.getStatus())
                .append("creator", event.getCreator())
                .append("staffReviewer", event.getStaffReviewer())
                .append("staffStatus", event.getStaffStatus());
    }

    // Convert a MongoDB document back into an Event object.
    private Event fromDocument(Document doc) {
        if (doc == null) {
            return null;
        }

        String dateValue = doc.getString("eventDateTime");

        LocalDateTime eventDateTime = dateValue == null
                ? null
                : LocalDateTime.parse(dateValue);

        Event event = new Event(
                doc.getString("eventName"),
                doc.getString("description"),
                eventDateTime,
                doc.getString("location"),
                getIntegerValue(doc, "capacity"),
                doc.getString("creator")
        );

        event.setId(getIntegerValue(doc, "eventId"));
        event.setApproval(doc.getString("approval"));
        event.setStatus(doc.getString("status"));
        event.setStaffReviewer(doc.getString("staffReviewer"));
        event.setStaffStatus(doc.getString("staffStatus"));

        return event;
    }

    private int getIntegerValue(Document doc, String fieldName) {
        Object value = doc.get(fieldName);
        if (value instanceof Number number) {
            return Math.toIntExact(number.longValue());
        }
        if (value instanceof String text) {
            return Integer.parseInt(text);
        }
        return 0;
    }


    
    
    
    // ==================================================
    // 5. CREATE AND SAVE EVENTS
    // ==================================================

    // Create an event and assign its integer ID.
    public boolean createEvent(Event event) {
        try {
            int eventId = generateEventId();
            event.setId(eventId);

            Document doc = toDocument(event);
            collection.insertOne(doc);

            return true;

        } catch (Exception e) {
            return false;
        }
    }


    
    
    
    // ==================================================
    // 6. FIND AND RETRIEVE EVENTS
    // ==================================================

    // Find an event using its integer event ID.
    public Event findEventById(int eventId) {
        Document doc = collection.find(
                Filters.eq("eventId", eventId)
        ).first();

        return fromDocument(doc);
    }

    // Alias for services that use getEvent().
    public Event getEvent(int eventId) {
        return findEventById(eventId);
    }

    // Retrieve all events.
    public List<Event> findAllEvents() {
        List<Event> events = new ArrayList<>();

        for (Document doc : collection.find()) {
            events.add(fromDocument(doc));
        }

        return events;
    }

    // Alias for services that use getEvents().
    public List<Event> getEvents() {
        return findAllEvents();
    }


    
    
    
    // ==================================================
    // 7. UPDATE EVENTS
    // ==================================================

    // Update an existing event using its event ID.
    public boolean updateEvent(int eventId, Event updated) {
        try {
            UpdateResult result = collection.updateOne(
                    Filters.eq("eventId", eventId),
                    Updates.combine(
                            Updates.set("eventName", updated.getEventName()),
                            Updates.set("description", updated.getDescription()),
                            Updates.set(
                                    "eventDateTime",
                                    updated.getEventDateTime() == null
                                            ? null
                                            : updated.getEventDateTime().toString()
                            ),
                            Updates.set("location", updated.getLocation()),
                            Updates.set("capacity", updated.getCapacity()),
                            Updates.set("approval", updated.getApproval()),
                            Updates.set("status", updated.getStatus()),
                            Updates.set("creator", updated.getCreator()),
                            Updates.set("staffReviewer", updated.getStaffReviewer()),
                            Updates.set("staffStatus", updated.getStaffStatus())
                    )
            );

            return result.getMatchedCount() > 0;

        } catch (Exception e) {
            return false;
        }
    }


    
    
    
    // ==================================================
    // 8. CANCEL EVENTS
    // ==================================================

    // Mark an event as cancelled.
    public boolean cancelEvent(int eventId) {
        UpdateResult result = collection.updateOne(
                Filters.eq("eventId", eventId),
                Updates.set("status", "CANCELLED")
        );

        return result.getMatchedCount() > 0;
    }

    // Permanently remove an event by its system ID.
    public boolean deleteEvent(int eventId) {
        try {
            DeleteResult result = collection.deleteOne(Filters.eq("eventId", eventId));
            return result.getDeletedCount() > 0;
        } catch (Exception e) {
            return false;
        }
    }


    
    
    
}
