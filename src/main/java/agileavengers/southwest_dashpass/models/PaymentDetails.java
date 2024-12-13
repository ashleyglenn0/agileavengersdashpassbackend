package agileavengers.southwest_dashpass.models;

import agileavengers.southwest_dashpass.utils.EncryptionUtils;
import jakarta.persistence.*;

@Entity
@Table(name = "PaymentDetails")  // Explicit table name if needed
public class PaymentDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)  // Ensures every PaymentDetails has a customer
    private Customer customer;

    @Column(name = "encrypted_card_number", nullable = false)  // Mark as non-nullable for essential fields
    private String encryptedCardNumber;

    @Column(name = "encrypted_expiration_date", nullable = false)
    private String encryptedExpirationDate;

    @Column(name = "encrypted_cvv", nullable = false)
    private String encryptedCVV;

    @Column(name = "encrypted_billing_zip", nullable = false)
    private String encryptedBillingZip;

    @Column(name = "encrypted_name_on_card", nullable = false)
    private String encryptedNameOnCard;

    private boolean savePaymentDetails;

    // Transient field for displaying the last 4 digits of the card
    @Transient
    private String displayCardNumber;

    // Default constructor for JPA
    public PaymentDetails() {}

    // Main constructor with encrypted values passed in
    public PaymentDetails(Customer customer, String encryptedCardNumber, String encryptedExpirationDate,
                          String encryptedCVV, String encryptedBillingZip, String encryptedNameOnCard) {
        this.customer = customer;
        this.encryptedCardNumber = encryptedCardNumber;
        this.encryptedExpirationDate = encryptedExpirationDate;
        this.encryptedCVV = encryptedCVV;
        this.encryptedBillingZip = encryptedBillingZip;
        this.encryptedNameOnCard = encryptedNameOnCard;
    }
    // New method to set decrypted details for display
    public void loadDecryptedDetails(EncryptionUtils encryptionUtils) {
        String decryptedCardNumber = encryptionUtils.decrypt(encryptedCardNumber);
        this.displayCardNumber = "**** **** **** " + decryptedCardNumber.substring(decryptedCardNumber.length() - 4);

        // Other decrypted fields can also be loaded here
        this.encryptedExpirationDate = encryptionUtils.decrypt(encryptedExpirationDate);
        this.encryptedNameOnCard = encryptionUtils.decrypt(encryptedNameOnCard);
        this.encryptedBillingZip = encryptionUtils.decrypt(encryptedBillingZip);
        this.encryptedCVV = "***"; // Mask CVV for security
    }

    // Getters
    public Long getId() {
        return id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public String getEncryptedCardNumber() {
        return encryptedCardNumber;
    }

    public String getEncryptedExpirationDate() {
        return encryptedExpirationDate;
    }

    public String getEncryptedCVV() {
        return encryptedCVV;
    }

    public String getEncryptedBillingZip() {
        return encryptedBillingZip;
    }

    public String getEncryptedNameOnCard() {
        return encryptedNameOnCard;
    }

    // Setters
    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public void setEncryptedCardNumber(String encryptedCardNumber) {
        this.encryptedCardNumber = encryptedCardNumber;
    }

    public void setEncryptedExpirationDate(String encryptedExpirationDate) {
        this.encryptedExpirationDate = encryptedExpirationDate;
    }

    public void setEncryptedCVV(String encryptedCVV) {
        this.encryptedCVV = encryptedCVV;
    }

    public void setEncryptedBillingZip(String encryptedBillingZip) {
        this.encryptedBillingZip = encryptedBillingZip;
    }

    public void setEncryptedNameOnCard(String encryptedNameOnCard) {
        this.encryptedNameOnCard = encryptedNameOnCard;
    }

    public void setDisplayCardNumber(String displayCardNumber) {
        this.displayCardNumber = displayCardNumber;
    }
    public boolean isSavePaymentDetails() {
        return savePaymentDetails;
    }

    public void setSavePaymentDetails(boolean savePaymentDetails) {
        this.savePaymentDetails = savePaymentDetails;
    }
}
