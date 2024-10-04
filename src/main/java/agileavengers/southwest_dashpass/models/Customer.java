package agileavengers.southwest_dashpass.models;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name="customer")
public class Customer extends User {
    @Column(name="numberOfDashPassesAvailable")
    private int numberOfDashPassesAvailable;

    @Column(name="totalDashPasses")
    private int totalDashPasses;
    @Column(name="numberOfDashPassesUsed")
    private int numberOfDashPassesUsed;

    @Column(name="canPurchaseDashPass")
    private boolean canPurchaseDashPass;

    @Column(name="canFly")
    private boolean canFly;
    @Column(name="canPurchaseFlight")
    private boolean canPurchaseFlight;

    @OneToMany(mappedBy="customer", cascade = CascadeType.ALL)
    private List<DashPass> dashPasses = new ArrayList<>();

    // Consider mapping this properly if flights are associated entities
    @OneToMany(mappedBy="customer", cascade = CascadeType.ALL)
    private List<Reservation> reservations = new ArrayList<>();

    @OneToMany(mappedBy="customer", cascade = CascadeType.ALL)
    private List<DashPassReservation> dashPassReservations = new ArrayList<>();

    // Default constructor
    public Customer() {
        super();
        this.numberOfDashPassesAvailable = 0; // Default initialization
        this.numberOfDashPassesUsed = 0; // Default initialization
        this.canPurchaseDashPass = false; // Default value, adjust as needed
        this.dashPasses = new ArrayList<>();  // Initialize lists to avoid null pointers
        this.reservations = new ArrayList<>();  // Initialize lists
        this.dashPassReservations = new ArrayList<>();  // Initialize lists
    }

    // Constructor with fields
    public Customer(String fname, String lname, String uname, String mail, String pword) {
        super(fname, lname, uname, mail, pword);
        this.numberOfDashPassesAvailable = 0; // Default value, adjust as needed
        this.numberOfDashPassesUsed = 0; // Default value, adjust as needed
        this.canPurchaseDashPass = true; // Default value, adjust as needed
        this.dashPasses = new ArrayList<>();  // Initialize lists to avoid null pointers
        this.reservations = new ArrayList<>();  // Initialize lists
        this.dashPassReservations = new ArrayList<>();  // Initialize lists
    }


    // Getters and setters
    public int getTotalDashPasses() {
        return totalDashPasses;
    }

    public void setTotalDashPasses(int totalDashPasses) {
        this.totalDashPasses = totalDashPasses;
    }

    public boolean isCanFly() {
        return canFly;
    }

    public void setCanFly(boolean canFly) {
        this.canFly = canFly;
    }

    public boolean isCanPurchaseFlight() {
        return canPurchaseFlight;
    }

    public void setCanPurchaseFlight(boolean canPurchaseFlight) {
        this.canPurchaseFlight = canPurchaseFlight;
    }

    public List<DashPass> getDashPasses() {
        return dashPasses;
    }

    public void setDashPass(List<DashPass> dashPasses) {
        this.dashPasses = dashPasses;
    }

    public List<DashPassReservation> getDashPassReservations() {
        return dashPassReservations;
    }

    public void setDashPassReservations(List<DashPassReservation> dashPassReservations) {
        this.dashPassReservations = dashPassReservations;
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

    public List<Reservation> getReservations() {
        return reservations;
    }

    public void setReservations(List<Reservation> reservations) {
        this.reservations = reservations;
    }
}
