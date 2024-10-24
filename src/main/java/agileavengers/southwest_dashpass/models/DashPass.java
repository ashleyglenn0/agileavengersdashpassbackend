package agileavengers.southwest_dashpass.models;

import jakarta.persistence.*;

import java.time.LocalDate;

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
    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "customerID", referencedColumnName = "ID")
    private Customer customer;
    @OneToOne(mappedBy = "dashPass", cascade = CascadeType.ALL, optional = true)
    private DashPassReservation dashPassReservation;
    @Column(name = "confirmation_number")
    private String confirmationNumber;

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
}


