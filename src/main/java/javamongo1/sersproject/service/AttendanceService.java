package javamongo1.sersproject.service;

import javamongo1.sersproject.model.Attendance;
import javamongo1.sersproject.repository.AttendanceRepository;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class AttendanceService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("M/d/yyyy");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("h:mm:ss a");

    private final AttendanceRepository attendanceRepository = new AttendanceRepository();

    public List<Attendance> findByEventId(int eventId) {
        return attendanceRepository.findByEventId(eventId);
    }

    public boolean saveChanges(int eventId, List<Attendance> attendanceList, List<String> deletedIds) {
        return attendanceRepository.saveChanges(eventId, attendanceList, deletedIds);
    }

    public List<Attendance> importCsv(Path path, int eventId, List<Attendance> currentAttendance) throws IOException {
        List<Attendance> mergedAttendance = new ArrayList<>(currentAttendance);
        
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line;
            boolean header = true;
            
            while ((line = reader.readLine()) != null) {
                if (header) {
                    header = false;
                    continue;
                }
                if (line.isBlank()) {
                    continue;
                }

                List<String> columns = parseCsvLine(line);
                
                if (columns.size() < 6) {
                    throw new IOException("A CSV row does not contain all six expected columns.");
                }

                Attendance imported = parseAttendance(columns, eventId);
                imported.setSource("Imported");
                
                int existingIndex = findByStudentId(mergedAttendance, imported.getStudentId());
                
                    if (existingIndex >= 0) {
                        imported.setId(mergedAttendance.get(existingIndex).getId());
                        mergedAttendance.set(existingIndex, imported);
                    } else {
                        mergedAttendance.add(imported);
                    }
            }
        }
        return mergedAttendance;
    }

    private Attendance parseAttendance(List<String> columns, int eventId) throws IOException {
        String[] identity = columns.get(0).trim().split("\\s+-\\s+");
        
        if (identity.length < 3) {
            throw new IOException("Could not read name, student ID, and course from a NAME value.");
        }

        try {
            LocalDate checkInDate = LocalDate.parse(columns.get(1).trim(), DATE_FORMAT);
            LocalTime checkInTime = LocalTime.parse(columns.get(2).trim(), TIME_FORMAT);
            
            LocalDate checkOutDate = columns.get(3).isBlank()
                    ? null : LocalDate.parse(columns.get(3).trim(), DATE_FORMAT);
            
            LocalTime checkOutTime = columns.get(4).isBlank()
                    ? null : LocalTime.parse(columns.get(4).trim(), TIME_FORMAT);

            LocalDateTime checkIn = LocalDateTime.of(checkInDate, checkInTime);
            LocalDateTime checkOut = checkOutDate == null || checkOutTime == null
                    ? null : LocalDateTime.of(checkOutDate, checkOutTime);

            StringBuilder name = new StringBuilder(identity[0]);
            
                for (int i = 1; i < identity.length - 2; i++) {
                    name.append(" - ").append(identity[i]);
                }
                
            return new Attendance(eventId, name.toString().trim(), identity[identity.length - 2].trim(),
                    identity[identity.length - 1].trim(), checkIn, checkOut, columns.get(5).trim());
            
        } catch (RuntimeException e) {
            throw new IOException("Could not read a date or time value from an attendance row.", e);
        }
    }

    private int findByStudentId(List<Attendance> attendanceList, String studentId) {
        for (int i = 0; i < attendanceList.size(); i++) {
            if (attendanceList.get(i).getStudentId().equalsIgnoreCase(studentId)) {
                return i;
            }
        }
        return -1;
    }

    private List<String> parseCsvLine(String line) throws IOException {
        
        List<String> values = new ArrayList<>();
        StringBuilder value = new StringBuilder();
        
        boolean quoted = false;
        
            for (int i = 0; i < line.length(); i++) {
                char character = line.charAt(i);
                if (character == '"') {
                    if (quoted && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        value.append('"');
                        i++;
                    } else {
                        quoted = !quoted;
                    }
                } else if (character == ',' && !quoted) {
                    values.add(value.toString());
                    value.setLength(0);
                } else {
                    value.append(character);
                }
            }
            
        if (quoted) {
            throw new IOException("A CSV row contains an unclosed quoted value.");
        }
        values.add(value.toString());
        return values;
    }
}
