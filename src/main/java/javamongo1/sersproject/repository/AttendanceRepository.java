package javamongo1.sersproject.repository;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.Updates;
import javamongo1.sersproject.config.MongoConnection;
import javamongo1.sersproject.model.Attendance;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AttendanceRepository {

    private final MongoCollection<Document> collection;
    private boolean indexInitialized;

    public AttendanceRepository() {
        collection = MongoConnection.getCollection("attendance");
    }

    private synchronized void initializeIndexes() {
        if (indexInitialized) {
            return;
        }
        collection.createIndex(
                new Document("eventId", 1).append("studentId", 1),
                new IndexOptions().unique(true).name("event_student_unique")
        );
        indexInitialized = true;
    }

    public List<Attendance> findByEventId(int eventId) {
        initializeIndexes();
        List<Attendance> attendanceList = new ArrayList<>();
        for (Document document : collection.find(Filters.eq("eventId", eventId))
                .sort(Sorts.ascending("studentId"))) {
            attendanceList.add(fromDocument(document));
        }
        return attendanceList;
    }

    public boolean saveChanges(int eventId, List<Attendance> attendanceList, List<String> deletedIds) {
        try {
            initializeIndexes();
            for (Attendance attendance : attendanceList) {
                if (attendance.getId() == null) {
                    Document document = toDocument(attendance);
                    ObjectId id = new ObjectId();
                    document.put("_id", id);
                    collection.insertOne(document);
                    attendance.setId(id.toHexString());
                } else {
                    Document before = collection.find(Filters.and(
                            Filters.eq("_id", new ObjectId(attendance.getId())),
                            Filters.eq("eventId", eventId))).first();
                    collection.updateOne(
                            Filters.and(Filters.eq("_id", new ObjectId(attendance.getId())),
                                    Filters.eq("eventId", eventId)),
                            Updates.combine(
                                    Updates.set("name", attendance.getName()),
                                    Updates.set("studentId", attendance.getStudentId()),
                                    Updates.set("course", attendance.getCourse()),
                                    Updates.set("checkIn", toStoredDateTime(attendance.getCheckIn())),
                                    Updates.set("checkOut", toStoredDateTime(attendance.getCheckOut())),
                                    Updates.set("duration", attendance.getDuration()),
                                    Updates.set("source", attendance.getSource())
                            )
                    );
                }
            }

            for (String id : deletedIds) {
                if (id != null && ObjectId.isValid(id)) {
                    Document before = collection.find(Filters.and(
                            Filters.eq("_id", new ObjectId(id)), Filters.eq("eventId", eventId))).first();
                    collection.deleteOne(Filters.and(
                            Filters.eq("_id", new ObjectId(id)),
                            Filters.eq("eventId", eventId)
                    ));
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Document toDocument(Attendance attendance) {
        return new Document("eventId", attendance.getEventId())
                .append("name", attendance.getName())
                .append("studentId", attendance.getStudentId())
                .append("course", attendance.getCourse())
                .append("checkIn", toStoredDateTime(attendance.getCheckIn()))
                .append("checkOut", toStoredDateTime(attendance.getCheckOut()))
                .append("duration", attendance.getDuration())
                .append("source", attendance.getSource());
    }

    private Attendance fromDocument(Document document) {
        Attendance attendance = new Attendance(
                document.getInteger("eventId", 0),
                document.getString("name"),
                document.getString("studentId"),
                document.getString("course"),
                fromStoredDateTime(document.getString("checkIn")),
                fromStoredDateTime(document.getString("checkOut")),
                document.getString("duration")
        );
        attendance.setSource(document.getString("source"));
        attendance.setId(document.getObjectId("_id").toHexString());
        return attendance;
    }

    private String toStoredDateTime(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.toString();
    }

    private LocalDateTime fromStoredDateTime(String value) {
        return value == null || value.isBlank() ? null : LocalDateTime.parse(value);
    }
}
