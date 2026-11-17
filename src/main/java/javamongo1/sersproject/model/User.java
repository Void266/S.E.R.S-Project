package javamongo1.sersproject.model;

public class User {

    // Internal S.E.R.S. account ID
    private int userId;

    // Physical school ID: String avoids numeric size limits
    private String studentId;

    private String name;
    private String password;
    private String role;

    public User() {
    }

    public User(String studentId, String name,String password, String role) {
        this.studentId = studentId;
        this.name = name;
        this.password = password;
        this.role = role;
    }

    // Getters

    public int getId() {
        return userId;
    }

    public String getStudentId() {
        return studentId;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public String getRole() {
        return role;
    }

    // Setters

    public void setId(int userId) {
        this.userId = userId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setRole(String role) {
        this.role = role;
    }
}