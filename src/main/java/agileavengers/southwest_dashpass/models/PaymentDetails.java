package agileavengers.southwest_dashpass.models;

import jakarta.persistence.*;
import agileavengers.southwest_dashpass.utils.EncryptionUtils;

@Entity
public class PaymentDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Column(name = "encrypted_card_number")
    private String encryptedCardNumber;

    @Column(name = "encrypted_expiration_date")
    private String encryptedExpirationDate;

    @Column(name = "encrypted_cvv")
    private String encryptedCVV;

    @Column(name = "encrypted_billing_zip")
    private String encryptedBillingZip;

    @Column(name = "encrypted_name_on_card")
    private String encryptedNameOnCard;

    // Constructors, getters, setters
    public PaymentDetails() {}

    public PaymentDetails(Customer customer, String cardNumber, String expirationDate, String cvv, String billingZip, String nameOnCard) {
        this.customer = customer;
        this.encryptedCardNumber = EncryptionUtils.encrypt(cardNumber);
        this.encryptedExpirationDate = EncryptionUtils.encrypt(expirationDate);
        this.encryptedCVV = EncryptionUtils.encrypt(cvv);
        this.encryptedBillingZip = EncryptionUtils.encrypt(billingZip);
        this.encryptedNameOnCard = EncryptionUtils.encrypt(nameOnCard);
    }

    // Getters and setters for the encrypted fields
    public Long getId() {
        return id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public String getEncryptedCardNumber() {
        return encryptedCardNumber;
    }

    public void setEncryptedCardNumber(String encryptedCardNumber) {
        this.encryptedCardNumber = encryptedCardNumber;
    }

    public String getEncryptedExpirationDate() {
        return encryptedExpirationDate;
    }

    public void setEncryptedExpirationDate(String encryptedExpirationDate) {
        this.encryptedExpirationDate = encryptedExpirationDate;
    }

    public String getEncryptedCVV() {
        return encryptedCVV;
    }

    public void setEncryptedCVV(String encryptedCVV) {
        this.encryptedCVV = encryptedCVV;
    }

    public String getEncryptedBillingZip() {
        return encryptedBillingZip;
    }

    public void setEncryptedBillingZip(String encryptedBillingZip) {
        this.encryptedBillingZip = encryptedBillingZip;
    }

    public String getEncryptedNameOnCard() {
        return encryptedNameOnCard;
    }

    public void setEncryptedNameOnCard(String encryptedNameOnCard) {
        this.encryptedNameOnCard = encryptedNameOnCard;
    }
}
