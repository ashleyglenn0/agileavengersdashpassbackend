package agileavengers.southwest_dashpass.models;

import jakarta.persistence.*;



@Entity
@Table(name="EMPLOYEE")
public class Employee extends User {
    @Enumerated(EnumType.STRING)
    @Column(name="role")
    private Role role;
    @Column(name="canSellDashPass")
    private boolean canSellDashPass;
    @Column(name="canAddCustomerFlight")
    private boolean canAddCustomerFlight;
    @Column(name="canAddCustomer")
    private boolean canAddCustomer;
    @Column(name="canRedeemDashPass")
    private boolean canRedeemDashPass;
    @Column(name="canRemoveDashPass")
    private boolean canRemoveDashPass;
    @Column(name="canEditFlightInformation")
    private boolean canEditFlightInformation;

    // Constructor with fields
    public Employee(String fname, String lname, String uname, String mail, String pword, Role role) {
        super(fname, lname, uname, mail, pword);
        this.role = role;
        this.setUserType(UserType.EMPLOYEE); // Set the user type as EMPLOYEE
        this.canSellDashPass = false; // Default value, adjust as needed
        this.canRedeemDashPass = false; // Default value, adjust as needed
    }

    // Default constructor
    public Employee() {
        super(); // Calls the User default constructor
        this.setUserType(UserType.EMPLOYEE); // Set the user type to EMPLOYEE
        this.setRole(Role.SALES);
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
