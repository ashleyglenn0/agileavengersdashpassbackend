package agileavengers.southwest_dashpass.models;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class TimeOffRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee; // Link to the Employee requesting time off

    @Column(nullable = false)
    private LocalDate startDate; // Start date of the time-off

    @Column(nullable = false)
    private LocalDate endDate; // End date of the time-off

    @Column(length = 255)
    private String reason; // Reason for the time-off (e.g., "Vacation")

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TimeOffRequestStatus status; // Status of the request (e.g., PENDING, APPROVED, DECLINED)

    // Constructors
    public TimeOffRequest() {}

    public TimeOffRequest(Employee employee, LocalDate startDate, LocalDate endDate, String reason, TimeOffRequestStatus status) {
        this.employee = employee;
        this.startDate = startDate;
        this.endDate = endDate;
        this.reason = reason;
        this.status = status;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public TimeOffRequestStatus getStatus() {
        return status;
    }

    public void setStatus(TimeOffRequestStatus status) {
        this.status = status;
    }
}
