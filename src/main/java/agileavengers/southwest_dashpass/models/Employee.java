package agileavengers.southwest_dashpass.models;

import jakarta.persistence.*;



@Entity
public class Employee extends User {


    private Long employeeId;
    private String role;
    private boolean canSellDashPass;
    private boolean canAddCustomerFlight;
    private boolean canAddCustomer;
    private boolean canRedeemDashPass;
    private boolean canRemoveDashPass;
    private boolean canEditFlightInformation;

    // Constructor with fields
    public Employee(Long id, String fname, String lname, String uname, String mail, String pword, String role) {
        super(fname, lname, uname, mail, pword);
        this.employeeId = id;
        this.role = role;
        this.setUserType(UserType.EMPLOYEE); // Set the user type as EMPLOYEE
        this.canSellDashPass = false; // Default value, adjust as needed
        this.canRedeemDashPass = false; // Default value, adjust as needed
    }

    // Default constructor
    public Employee() {
        super(); // Calls the User default constructor
        this.setUserType(UserType.EMPLOYEE); // Set the user type to EMPLOYEE
    }

    // Getters and setters
    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isCanSellDashPass() {
        return canSellDashPass;
    }

    public boolean isCanAddCustomerFlight() {
        return canAddCustomerFlight;
    }

    public void setCanAddCustomerFlight(boolean canAddCustomerFlight) {
        this.canAddCustomerFlight = canAddCustomerFlight;
    }

    public boolean isCanAddCustomer() {
        return canAddCustomer;
    }

    public void setCanAddCustomer(boolean canAddCustomer) {
        this.canAddCustomer = canAddCustomer;
    }

    public boolean isCanRemoveDashPass() {
        return canRemoveDashPass;
    }

    public void setCanRemoveDashPass(boolean canRemoveDashPass) {
        this.canRemoveDashPass = canRemoveDashPass;
    }

    public boolean isCanEditFlightInformation() {
        return canEditFlightInformation;
    }

    public void setCanEditFlightInformation(boolean canEditFlightInformation) {
        this.canEditFlightInformation = canEditFlightInformation;
    }

    public void setCanSellDashPass(boolean canSellDashPass) {
        this.canSellDashPass = canSellDashPass;
    }

    public boolean isCanRedeemDashPass() {
        return canRedeemDashPass;
    }

    public void setCanRedeemDashPass(boolean canRedeemDashPass) {
        this.canRedeemDashPass = canRedeemDashPass;
    }
}
