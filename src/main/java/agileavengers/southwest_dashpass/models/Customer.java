package agileavengers.southwest_dashpass.models;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;


@Entity
public class Customer extends User {

    private Long customerId;

    private int maxNumberOfDashPasses;

    private int numberOfDashPassesAvailable;

    private int numberOfDashPassesUsed;

    private boolean canPurchaseDashPass;

    private boolean canRedeemDashPass;

    private boolean canFly;
    private boolean canPurchaseFlight;

    // Consider mapping this properly if flights are associated entities
    private List<String> flights = new ArrayList<>();


    // Default constructor
    public Customer() {
        super();
        this.setUserType(UserType.CUSTOMER); // Set the user type to CUSTOMER
        this.numberOfDashPassesAvailable = 0; // Default initialization
        this.numberOfDashPassesUsed = 0; // Default initialization
        this.canPurchaseDashPass = false; // Default value, adjust as needed
        this.canRedeemDashPass = false; // Default value, adjust as needed
    }

    // Constructor with fields
    public Customer(Long id, String fname, String lname, String uname, String mail, String pword) {
        super(fname, lname, uname, mail, pword);
        this.setUserType(UserType.CUSTOMER);
        this.customerId = id;
        this.numberOfDashPassesAvailable = 0; // Default value, adjust as needed
        this.numberOfDashPassesUsed = 0; // Default value, adjust as needed
        this.canPurchaseDashPass = false; // Default value, adjust as needed
        this.canRedeemDashPass = false; // Default value, adjust as needed
    }

    // Getters and setters
    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public int getNumberOfDashPassesAvailable() {
        return numberOfDashPassesAvailable;
    }

    public void setNumberOfDashPassesAvailable(int numberOfDashPassesAvailable) {
        this.numberOfDashPassesAvailable = numberOfDashPassesAvailable;
    }

    public int getNumberOfDashPassesUsed() {
        return numberOfDashPassesUsed;
    }

    public void setNumberOfDashPassesUsed(int numberOfDashPassesUsed) {
        this.numberOfDashPassesUsed = numberOfDashPassesUsed;
    }

    public boolean isCanPurchaseDashPass() {
        return canPurchaseDashPass;
    }

    public void setCanPurchaseDashPass(boolean canPurchaseDashPass) {
        this.canPurchaseDashPass = canPurchaseDashPass;
    }

    public boolean isCanRedeemDashPass() {
        return canRedeemDashPass;
    }

    public void setCanRedeemDashPass(boolean canRedeemDashPass) {
        this.canRedeemDashPass = canRedeemDashPass;
    }

    public List<String> getFlights() {
        return flights;
    }

    public void setFlights(List<String> flights) {
        this.flights = flights;
    }
}
