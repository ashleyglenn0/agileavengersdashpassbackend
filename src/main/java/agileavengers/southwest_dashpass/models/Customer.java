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

    @OneToMany(mappedBy = "customer", cascade = CascadeType.PERSIST)
    private List<DashPass> dashPasses = new ArrayList<>();

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL)
    private List<Reservation> reservations = new ArrayList<>();

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL)
    private List<DashPassReservation> dashPassReservations = new ArrayList<>();

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Bag> bags = new ArrayList<>();

    @OneToOne(fetch = FetchType.EAGER)
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
        return availableDashPassCount != null ? availableDashPassCount : 0;
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
        System.out.println("Updating DashPass summary...");
        System.out.println("DashPasses Before Update: " + dashPasses.stream()
                .map(DashPass::getDashpassId)
                .collect(Collectors.toList()));
        System.out.println("DashPassReservations Before Update: " + dashPassReservations.stream()
                .map(DashPassReservation::getId)
                .collect(Collectors.toList()));

        // Count unattached DashPasses
        int unattachedDashPassCount = (int) dashPasses.stream()
                .filter(dp -> !dp.isPendingValidation())
                .count();

        // Count validated DashPassReservations
        this.dashPassInUseCount = (int) dashPassReservations.stream()
                .filter(DashPassReservation::getValidated)
                .count();

        // Include DashPasses pending validation in the total
        int pendingDashPassCount = (int) dashPasses.stream()
                .filter(DashPass::isPendingValidation)
                .count();

        // Calculate total and available counts
        this.totalDashPassCount = unattachedDashPassCount + dashPassInUseCount + pendingDashPassCount;
        this.availableDashPassCount = MAX_DASHPASSES - totalDashPassCount;

        System.out.println("Updated Counts -> Total: " + totalDashPassCount +
                ", Available: " + availableDashPassCount + ", In Use: " + dashPassInUseCount +
                ", Pending Validation: " + pendingDashPassCount);
    }


    // Method to get list of past DashPass reservations
    public List<DashPassReservation> getPreviousDashPassReservations() {
        LocalDate currentDate = LocalDate.now();
        return dashPassReservations.stream()
                .filter(dashPassReservation -> dashPassReservation.getBookingDate().isBefore(currentDate))
                .collect(Collectors.toList());
    }

    public void addDashPass(DashPass dashPass) {
        if (!dashPasses.contains(dashPass) && !dashPass.isPendingValidation()) {
            dashPasses.add(dashPass);
            dashPass.setCustomer(this); // Maintain bidirectional relationship
        }
    }



    public void addDashPassReservation(DashPassReservation dashPassReservation) {
        System.out.println("Adding DashPassReservation with ID: " + dashPassReservation.getId());
        dashPassReservations.add(dashPassReservation);
        dashPassReservation.setCustomer(this);

        // Only increase in-use count if the reservation is validated
        if (dashPassReservation.getValidated()) {
            dashPassInUseCount++;
        }

        System.out.println("DashPassReservations After Add: " + dashPassReservations.stream()
                .map(DashPassReservation::getId)
                .collect(Collectors.toList()));
    }

    public void incrementDashPassInUseCount() {
        this.dashPassInUseCount += 1;
    }

    public List<Bag> getBags() {
        return bags;
    }

    public void setBags(List<Bag> bags) {
        this.bags = bags;
    }

//    public List<DashPass> getValidDashPasses() {
//        return this.dashPasses.stream()
//                .filter(dashPass -> dashPass.getStatus() != DashPassStatus.UNTRACKED)
//                .collect(Collectors.toList());
//    }

}
