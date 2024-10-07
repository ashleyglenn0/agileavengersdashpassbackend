package agileavengers.southwest_dashpass.models;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name="customer")
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // Add an ID field for the Customer

    @Column(name="number_of_dash_passes_available_for_purchase")
    private Integer numberOfDashPassesAvailableForPurchase;

    @Column(name="max_number_of_dashpasses")
    private Integer maxNumberOfDashPasses = 5;

    @Column(name="total_dash_passes_customer_has")
    private Integer totalDashPassesCustomerHas = 0;
    @Column(name="number_of_dash_passes_used")
    private Integer numberOfDashPassesUsed;

    @Column(name="can_purchase_dash_pass")
    private boolean canPurchaseDashPass;

    @Column(name="can_fly")
    private boolean canFly;
    @Column(name="can_purchase_flight")
    private boolean canPurchaseFlight;

    @OneToMany(mappedBy="customer", cascade = CascadeType.ALL)
    private List<DashPass> dashPasses = new ArrayList<>();

    // Consider mapping this properly if flights are associated entities
    @OneToMany(mappedBy="customer", cascade = CascadeType.ALL)
    private List<Reservation> reservations = new ArrayList<>();

    public void setDashPasses(List<DashPass> dashPasses) {
        this.dashPasses = dashPasses;
    }
    @OneToMany(mappedBy="customer", cascade = CascadeType.ALL)
    private List<DashPassReservation> dashPassReservations = new ArrayList<>();

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    // Default constructor
    public Customer() {
        super();
        this.numberOfDashPassesAvailableForPurchase = 5; // Default initialization
        this.numberOfDashPassesUsed = 0; // Default initialization
        this.canPurchaseDashPass = false; // Default value, adjust as needed
        this.dashPasses = new ArrayList<>();  // Initialize lists to avoid null pointers
        this.reservations = new ArrayList<>();  // Initialize lists
        this.dashPassReservations = new ArrayList<>();  // Initialize lists
    }

    // Getters and setters
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public Integer getTotalDashPassesCustomerHas() {
        return totalDashPassesCustomerHas;
    }

    public void setTotalDashPassesCustomerHas(int totalDashPasses) {
        this.totalDashPassesCustomerHas = totalDashPasses;
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

    public void setNumberOfDashPassesAvailableForPurchase(int numberOfDashPassesAvailableForPurchase) {
        this.numberOfDashPassesAvailableForPurchase = numberOfDashPassesAvailableForPurchase;
    }

    public Integer getMaxNumberOfDashPasses() {
        return maxNumberOfDashPasses;
    }

    public void setMaxNumberOfDashPasses(int maxNumberOfDashPasses) {
        this.maxNumberOfDashPasses = maxNumberOfDashPasses;
    }

    public Integer getNumberOfDashPassesAvailableForPurchase() {
        return numberOfDashPassesAvailableForPurchase;
    }

    public void setNumberOfDashPassesAvailableForPurchase() {
        this.numberOfDashPassesAvailableForPurchase = totalDashPassesCustomerHas - getNumberOfDashPassesUsed();
    }

    public Integer getNumberOfDashPassesUsed() {
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
