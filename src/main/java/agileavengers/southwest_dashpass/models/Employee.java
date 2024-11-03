package agileavengers.southwest_dashpass.models;

import jakarta.persistence.*;

@Entity
@Table(name="EMPLOYEE")
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING)
    @Column(name="role")
    private Role role;
    @Column(name="can_sell_dash_pass")
    private boolean canSellDashPass;
    @Column(name="can_add_customer_flight")
    private boolean canAddCustomerFlight;
    @Column(name="can_add_customer")
    private boolean canAddCustomer;
    @Column(name="can_redeem_dash_pass")
    private boolean canRedeemDashPass;
    @Column(name="can_remove_dash_pass")
    private boolean canRemoveDashPass;
    @Column(name="can_edit_flight_information")
    private boolean canEditFlightInformation;
    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false, unique = true) // This defines the foreign key in the 'customer' table that references 'users'
    private User user;

    // Constructor with fields

    // Default constructor
    public Employee() {
        super(); // Calls the User default constructor
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean isCanSellDashPass() {
        return canSellDashPass;
    }

    public boolean isCanAddCustomerFlight() {
        return canAddCustomerFlight;
    }

    public boolean isCanAddCustomer() {
        return canAddCustomer;
    }

    public boolean isCanRedeemDashPass() {
        return canRedeemDashPass;
    }

    public boolean isCanRemoveDashPass() {
        return canRemoveDashPass;
    }

    public boolean isCanEditFlightInformation() {
        return canEditFlightInformation;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    // Getters and setters
    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public boolean canSellDashPass() {
        return canSellDashPass;
    }

    public boolean canAddCustomerFlight() {
        return canAddCustomerFlight;
    }

    public void setCanAddCustomerFlight(boolean canAddCustomerFlight) {
        this.canAddCustomerFlight = canAddCustomerFlight;
    }

    public boolean canAddCustomer() {
        return canAddCustomer;
    }

    public void setCanAddCustomer(boolean canAddCustomer) {
        this.canAddCustomer = canAddCustomer;
    }

    public boolean canRemoveDashPass() {
        return canRemoveDashPass;
    }

    public void setCanRemoveDashPass(boolean canRemoveDashPass) {
        this.canRemoveDashPass = canRemoveDashPass;
    }

    public boolean canEditFlightInformation() {
        return canEditFlightInformation;
    }

    public void setCanEditFlightInformation(boolean canEditFlightInformation) {
        this.canEditFlightInformation = canEditFlightInformation;
    }

    public void setCanSellDashPass(boolean canSellDashPass) {
        this.canSellDashPass = canSellDashPass;
    }

    public boolean canRedeemDashPass() {
        return canRedeemDashPass;
    }

    public void setCanRedeemDashPass(boolean canRedeemDashPass) {
        this.canRedeemDashPass = canRedeemDashPass;
    }
}
