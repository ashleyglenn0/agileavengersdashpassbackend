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

    @Transient // Calculated field, not persisted
    private int totalDashPasses;

    @Transient // Calculated field, not persisted
    private int dashPassesForPurchase;

    @Transient // Calculated field, not persisted
    private boolean canPurchaseDashPass;

    private boolean canFly;
    private boolean canPurchaseFlight;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL)
    private List<DashPass> dashPasses = new ArrayList<>();

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL)
    private List<Reservation> reservations = new ArrayList<>();

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL)
    private List<DashPassReservation> dashPassReservations = new ArrayList<>();

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    public Customer() {
        super();
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

    public int getMaxDashPasses() {
        return MAX_DASHPASSES;
    }

    public void setCanPurchaseDashPass(boolean canPurchaseDashPass) {
        this.canPurchaseDashPass = canPurchaseDashPass;
    }

    public int getTotalDashPasses() {
        return dashPasses.size();
    }

    public int getDashPassesForPurchase() {
        return MAX_DASHPASSES - getTotalDashPasses();
    }

    public void setDashPassesForPurchase(int dashPassesForPurchase) {
        this.dashPassesForPurchase = dashPassesForPurchase;
    }

    public boolean isCanPurchaseDashPass() {
        return getTotalDashPasses() < MAX_DASHPASSES;
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

    public List<DashPassReservation> getDashPassReservations() {
        return dashPassReservations;
    }

    public List<Reservation> getReservations() {
        return reservations;
    }

    public void setReservations(List<Reservation> reservations) {
        this.reservations = reservations;
    }

    // Method to get list of past DashPass reservations
    public List<DashPassReservation> getPreviousDashPassReservations() {
        LocalDate currentDate = LocalDate.now();
        return dashPassReservations.stream()
                .filter(dashPassReservation -> dashPassReservation.getBookingDate().isBefore(currentDate))
                .collect(Collectors.toList());
    }

    public void addDashPass(DashPass dashPass) {
        dashPasses.add(dashPass);
        dashPass.setCustomer(this);
    }

    public void addDashPassReservation(DashPassReservation dashPassReservation) {
        dashPassReservations.add(dashPassReservation);
        dashPassReservation.setCustomer(this);
    }

    // Other necessary methods and logic if needed
}
