package agileavengers.southwest_dashpass.models;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
public class SalesRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "dash_pass_id")
    private DashPass dashPass;

    @ManyToOne
    @JoinColumn(name = "flight_id")
    private Flight flight;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = true) // Nullable if sold directly by the customer
    private Employee employee;

    private LocalDate saleDate;

    // Constructor, getters, and setters

    // Constructor for DashPass and flight sale
    public SalesRecord(DashPass dashPass, Flight flight, Customer customer, Employee employee, LocalDate saleDate) {
        this.dashPass = dashPass;
        this.flight = flight;
        this.customer = customer;
        this.employee = employee;
        this.saleDate = saleDate;
    }
    // Constructor for DashPass sale
    public SalesRecord(DashPass dashPass, Customer customer, Employee employee, LocalDate saleDate) {
        this.dashPass = dashPass;
        this.customer = customer;
        this.employee = employee;
        this.saleDate = saleDate;
    }

    // Constructor for Flight sale
    public SalesRecord(Flight flight, Customer customer, Employee employee, LocalDate saleDate) {
        this.flight = flight;
        this.customer = customer;
        this.employee = employee;
        this.saleDate = saleDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public DashPass getDashPass() {
        return dashPass;
    }

    public void setDashPass(DashPass dashPass) {
        this.dashPass = dashPass;
    }

    public Flight getFlight() {
        return flight;
    }

    public void setFlight(Flight flight) {
        this.flight = flight;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public LocalDate getSaleDate() {
        return saleDate;
    }

    public void setSaleDate(LocalDate saleDate) {
        this.saleDate = saleDate;
    }
}

