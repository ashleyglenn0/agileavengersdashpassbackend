package agileavengers.southwest_dashpass.models;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name="dashPass")
public class DashPass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="dashPassID")
    Long dashpassId;
    @Column(name="dateOfPurchase")
    LocalDate dateOfPurchase = LocalDate.now();
    @Column(name="expirationDate")
    LocalDate expirationDate;
    @Column(name="isRedeemed", nullable = false)
    boolean isRedeemed;
    @ManyToOne
    @JoinColumn(name = "customerID", referencedColumnName = "ID")
    private Customer customer;
    @OneToOne(mappedBy = "dashPass", cascade = CascadeType.ALL, optional = true, fetch = FetchType.EAGER)
    private DashPassReservation dashPassReservation;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private DashPassStatus status = DashPassStatus.VALID;

    @Column(name = "confirmation_number")
    private String confirmationNumber;

    private boolean isPendingValidation;


    public DashPass() {
        this.dashpassId = 0L;
        this.dateOfPurchase = LocalDate.now();
        this.expirationDate = dateOfPurchase.plusYears(1);
        this.isRedeemed = false;
    }

    public DashPass(Customer customer, LocalDate dop, LocalDate expDate, boolean redeemed) {
        this.customer = customer;
        this.dateOfPurchase = dop;
        this.expirationDate = expDate;
        this.isRedeemed = redeemed;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
        // Only add to dashPasses if NOT linked to a reservation
        if (customer != null && !customer.getDashPasses().contains(this) && this.dashPassReservation == null) {
            customer.getDashPasses().add(this);
        }
    }

    public Long getDashpassId() {
        return dashpassId;
    }

    public void setDashpassId(Long dashpassId) {
        this.dashpassId = dashpassId;
    }

    public LocalDate getDateOfPurchase() {
        return dateOfPurchase;
    }

    public void setDateOfPurchase(LocalDate dateOfPurchase) {
        this.dateOfPurchase = dateOfPurchase;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
    }

    public boolean isRedeemed() {
        return isRedeemed;
    }

    public void setRedeemed(boolean redeemed) {
        isRedeemed = redeemed;
    }

    public String getConfirmationNumber() {
        return confirmationNumber;
    }

    public void setConfirmationNumber(String confirmationNumber) {
        this.confirmationNumber = confirmationNumber;
    }
    public boolean isRedeemable() {
        return !isRedeemed && expirationDate.isAfter(LocalDate.now());
    }

    public DashPassReservation getDashPassReservation() {
        return dashPassReservation;
    }

    public void setDashPassReservation(DashPassReservation dashPassReservation) {
        this.dashPassReservation = dashPassReservation;
    }
    public boolean isExpired() {
        return expirationDate != null && LocalDate.now().isAfter(expirationDate);
    }

    public boolean isPendingValidation() {
        return isPendingValidation;
    }

    public void setPendingValidation(boolean pendingValidation) {
        isPendingValidation = pendingValidation;
    }

    public DashPassStatus getStatus() {
        return status;
    }

    public void setStatus(DashPassStatus status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        System.out.println("Equals called!");
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DashPass dashPass = (DashPass) o;
        return Objects.equals(dashpassId, dashPass.dashpassId);
    }

    @Override
    public int hashCode() {
        System.out.println("HashCode called!");
        return Objects.hash(dashpassId);
    }
}


