package javamongo1.sersproject.model;

import java.time.LocalDateTime;

public class Attendance {

    private String id;
    private int eventId;
    private String name;
    private String studentId;
    private String course;
    private LocalDateTime checkIn;
    private LocalDateTime checkOut;
    private String duration;
    private String source;

    public Attendance() {
    }

    public Attendance(int eventId, String name, String studentId, String course,
                      LocalDateTime checkIn, LocalDateTime checkOut, String duration) {
        this.eventId = eventId;
        this.name = name;
        this.studentId = studentId;
        this.course = course;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.duration = duration;
    }

    public String getId() { return id; }
    public int getEventId() { return eventId; }
    public String getName() { return name; }
    public String getStudentId() { return studentId; }
    public String getCourse() { return course; }
    public LocalDateTime getCheckIn() { return checkIn; }
    public LocalDateTime getCheckOut() { return checkOut; }
    public String getDuration() { return duration; }
    public String getSource() { return source; }

    public void setId(String id) { this.id = id; }
    public void setEventId(int eventId) { this.eventId = eventId; }
    public void setName(String name) { this.name = name; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public void setCourse(String course) { this.course = course; }
    public void setCheckIn(LocalDateTime checkIn) { this.checkIn = checkIn; }
    public void setCheckOut(LocalDateTime checkOut) { this.checkOut = checkOut; }
    public void setDuration(String duration) { this.duration = duration; }
    public void setSource(String source) { this.source = source; }
}
