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

    @Column(name="max_number_of_dashpasses")
    private Integer maxNumberOfDashPasses = 5;

    @Column(name="total_dash_passes_customer_has")
    private Integer totalDashPassesCustomerHas = 0;
    @Column(name="number_of_dash_passes_used")
    private Integer numberOfDashPassesUsed = 0;

    @Column(name="total_dash_passes_for_use")
    private Integer totalDashPassesForUse = 0;

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

    @OneToMany(mappedBy="customer", cascade = CascadeType.ALL)
    private List<DashPassReservation> dashPassReservations = new ArrayList<>();

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    // Default constructor
    public Customer() {
        super();
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
    public void setMaxNumberOfDashPasses(Integer maxNumberOfDashPasses) {
        this.maxNumberOfDashPasses = maxNumberOfDashPasses;
    }

    public void setTotalDashPassesCustomerHas(Integer totalDashPassesCustomerHas) {
        this.totalDashPassesCustomerHas = totalDashPassesCustomerHas;
    }

    public void setNumberOfDashPassesUsed(Integer numberOfDashPassesUsed) {
        this.numberOfDashPassesUsed = numberOfDashPassesUsed;
    }

    public Integer getTotalDashPassesForUse() {
        return totalDashPassesForUse;
    }

    public void setTotalDashPassesForUse(Integer totalDashPassesForUse) {
        this.totalDashPassesForUse = totalDashPassesForUse;
    }

    public Integer getTotalDashPassesCustomerHas() {
        return totalDashPassesCustomerHas;
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

    public int getNumberOfDashPasses(){
        return dashPasses.size();
    }


    public List<DashPassReservation> getDashPassReservations() {
        return dashPassReservations;
    }

    public void setDashPassReservations(List<DashPassReservation> dashPassReservations) {
        this.dashPassReservations = dashPassReservations;
    }



    public Integer getMaxNumberOfDashPasses() {
        return maxNumberOfDashPasses;
    }

    public void setMaxNumberOfDashPasses(int maxNumberOfDashPasses) {
        this.maxNumberOfDashPasses = maxNumberOfDashPasses;
    }

    public Integer getNumberOfDashPassesAvailableForPurchase() {
        return maxNumberOfDashPasses - totalDashPassesCustomerHas;
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

    // Add method to handle adding DashPassReservation
    public void addDashPassReservation(DashPassReservation dashPassReservation) {
        dashPassReservations.add(dashPassReservation);
        dashPassReservation.setCustomer(this);  // Set the customer reference in DashPassReservation
    }

    public void addDashPass(DashPass dashPass) {
        dashPasses.add(dashPass);
        dashPass.setCustomer(this);
    }
}
