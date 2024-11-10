package agileavengers.southwest_dashpass.dtos;

import agileavengers.southwest_dashpass.models.Role;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class EmployeeDTO {

    private Long id;
    private String firstName;
    private String lastName;
    private Role role;
    private String shiftStartTime; // Only time part
    private String shiftEndTime;   // Only time part

    // Constructor for mapping shift data
    public EmployeeDTO(Long id, String firstName, String lastName, Role role, LocalTime shiftStart, LocalTime shiftEnd) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
        this.shiftStartTime= (shiftStart != null) ? shiftStart.format(DateTimeFormatter.ofPattern("HH:mm")) : "N/A";
        this.shiftEndTime = (shiftEnd != null) ? shiftEnd.format(DateTimeFormatter.ofPattern("HH:mm")) : "N/A";
    }

    public EmployeeDTO() {

    }

    public EmployeeDTO(Long id, Role role, LocalDateTime startTime, LocalDateTime endTime) {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getShiftStartTime() {
        return shiftStartTime;
    }

    public void setShiftStartTime(String shiftStart) {
        this.shiftStartTime = shiftStart;
    }

    public String getShiftEndTime() {
        return shiftEndTime;
    }

    public void setShiftEnd(String shiftEnd) {
        this.shiftEndTime = shiftEnd;
    }
}
