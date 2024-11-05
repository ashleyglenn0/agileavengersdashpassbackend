package agileavengers.southwest_dashpass.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class SupportRequest {

    public enum Priority {
        NORMAL, URGENT
    }

    public enum Status {
        OPEN, IN_PROGRESS, CLOSED, ESCALATED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String subject;
    private String message;

    @Enumerated(EnumType.STRING)
    private Priority priority;

    @Enumerated(EnumType.STRING)
    private Status status;

    private LocalDateTime createdDate;

    // Customer who initiated the request
    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    // Employee assigned to handle the request
    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = true)
    private Employee createdBy;

    // Constructor with the specified parameters
    public SupportRequest(String subject, String message, Status status, Priority priority, Employee createdBy) {
        this.subject = subject;
        this.message = message;
        this.status = status;
        this.priority = priority;
        this.createdBy = createdBy;
        this.createdDate = LocalDateTime.now();
    }

    // Default constructor for JPA
    public SupportRequest() {}

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Employee getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Employee createdBy) {
        this.createdBy = createdBy;
    }
}
