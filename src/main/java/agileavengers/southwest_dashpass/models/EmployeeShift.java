package agileavengers.southwest_dashpass.models;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class EmployeeShift {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee; // Links this shift to a specific employee

    @Column(nullable = false)
    private LocalDateTime shiftStart; // Start time of the shift

    @Column(nullable = false)
    private LocalDateTime shiftEnd; // End time of the shift

    // Constructors
    public EmployeeShift() {}

    public EmployeeShift(Employee employee, LocalDateTime shiftStart, LocalDateTime shiftEnd) {
        this.employee = employee;
        this.shiftStart = shiftStart;
        this.shiftEnd = shiftEnd;
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

    public LocalDateTime getShiftStart() {
        return shiftStart;
    }

    public void setShiftStart(LocalDateTime shiftStart) {
        this.shiftStart = shiftStart;
    }

    public LocalDateTime getShiftEnd() {
        return shiftEnd;
    }

    public void setShiftEnd(LocalDateTime shiftEnd) {
        this.shiftEnd = shiftEnd;
    }
}
