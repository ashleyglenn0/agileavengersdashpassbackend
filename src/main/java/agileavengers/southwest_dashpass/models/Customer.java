package agileavengers.southwest_dashpass.models;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "customer")
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private static final int MAX_DASHPASSES = 5; // Maximum DashPasses allowed

    // Persisted fields to store DashPass details directly in the database
    @Column(name = "total_dash_pass_count")
    private Integer totalDashPassCount;

    @Column(name = "available_dash_pass_count")
    private Integer availableDashPassCount;

    @Column(name = "dash_pass_in_use_count")
    private Integer dashPassInUseCount;

    @Column(name = "can_buy_dash_pass")
    private Boolean canBuyDashPass = true;

    private Boolean canFly = true;
    private Boolean canPurchaseFlight = true;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL)
    private List<DashPass> dashPasses = new ArrayList<>();

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL)
    private List<Reservation> reservations = new ArrayList<>();

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL)
    private List<DashPassReservation> dashPassReservations = new ArrayList<>();

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    public Customer() {
        super();
        // Initialize default values
        this.totalDashPassCount = 0;
        this.availableDashPassCount = MAX_DASHPASSES;
        this.dashPassInUseCount = 0;
        this.canBuyDashPass = true;
    }

    // Getters and Setters
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

    public Integer getMaxDashPasses() {
        return MAX_DASHPASSES;
    }

    public Boolean getCanBuyDashPass() {
        return totalDashPassCount < MAX_DASHPASSES;
    }

    public void setCanBuyDashPass(Boolean canBuyDashPass) {
        this.canBuyDashPass = canBuyDashPass;
    }

    public Integer getTotalDashPassCount() {
        return totalDashPassCount;
    }

    public void setTotalDashPassCount(Integer totalDashPassCount) {
        this.totalDashPassCount = totalDashPassCount;
    }

    public Integer getAvailableDashPassCount() {
        return availableDashPassCount;
    }

    public void setAvailableDashPassCount(Integer availableDashPassCount) {
        this.availableDashPassCount = availableDashPassCount;
    }

    public Boolean getCanFly() {
        return canFly;
    }

    public void setCanFly(Boolean canFly) {
        this.canFly = canFly;
    }

    public Boolean getCanPurchaseFlight() {
        return canPurchaseFlight;
    }

    public void setCanPurchaseFlight(Boolean canPurchaseFlight) {
        this.canPurchaseFlight = canPurchaseFlight;
    }

    public Integer getDashPassInUseCount() {
        return dashPassInUseCount;
    }

    public void setDashPassInUseCount(Integer dashPassInUseCount) {
        this.dashPassInUseCount = dashPassInUseCount;
    }

    public List<DashPass> getDashPasses() {
        return dashPasses;
    }

    public List<DashPassReservation> getDashPassReservations() {
        return dashPassReservations;
    }

    public List<Reservation> getReservations() {
        return reservations;
    }

    public void setReservations(List<Reservation> reservations) {
        this.reservations = reservations;
    }

    // Methods to calculate based on DashPass data
    public void updateDashPassSummary() {
        this.totalDashPassCount = dashPasses.size(); // Only non-attached DashPasses
        this.dashPassInUseCount = (int) dashPassReservations.stream()
                .filter(reservation -> reservation.getBookingDate().isAfter(LocalDate.now().minusDays(1)))
                .count();
        this.availableDashPassCount = MAX_DASHPASSES - (totalDashPassCount + dashPassInUseCount);

        // Debug output to check calculations
        System.out.println("Total DashPass Count (owned, not attached): " + totalDashPassCount);
        System.out.println("DashPass In Use Count: " + dashPassInUseCount);
        System.out.println("Available DashPass Count (Calculated): " + availableDashPassCount);
    }



    // Method to get list of past DashPass reservations
    public List<DashPassReservation> getPreviousDashPassReservations() {
        LocalDate currentDate = LocalDate.now();
        return dashPassReservations.stream()
                .filter(dashPassReservation -> dashPassReservation.getBookingDate().isBefore(currentDate))
                .collect(Collectors.toList());
    }

    public void addDashPass(DashPass dashPass) {
        dashPasses.add(dashPass);       // Add the DashPass to the list
        dashPass.setCustomer(this);      // Link the DashPass to this customer
        updateDashPassSummary();         // Recalculate DashPass counts
    }

    public void addDashPassReservation(DashPassReservation dashPassReservation) {
        dashPassReservations.add(dashPassReservation);   // Add the reservation to the list
        dashPassReservation.setCustomer(this);           // Link the reservation to this customer
        updateDashPassSummary();                         // Recalculate DashPass counts
    }
}
