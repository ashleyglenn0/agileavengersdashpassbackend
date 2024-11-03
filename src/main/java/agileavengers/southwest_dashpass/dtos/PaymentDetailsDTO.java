package agileavengers.southwest_dashpass.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class PaymentDetailsDTO {

    @NotBlank(message = "Card number is required")
    @Size(min = 13, max = 19, message = "Card number must be between 13 and 19 digits")
    @Pattern(regexp = "\\d{13,19}", message = "Card number must be numeric")
    private String cardNumber;

    @NotBlank(message = "CVV is required")
    @Size(min = 3, max = 4, message = "CVV must be 3 or 4 digits")
    @Pattern(regexp = "\\d{3,4}", message = "CVV must be numeric")
    private String cvv;

    @NotBlank(message = "Expiration date is required")
    @Pattern(regexp = "(0[1-9]|1[0-2])/\\d{2}", message = "Expiration date must be in MM/YY format")
    private String expirationDate;

    @NotBlank(message = "Name on card is required")
    @Size(min = 2, max = 50, message = "Name on card must be between 2 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z ]+$", message = "Name on card must contain only letters and spaces")
    private String cardName;

    @NotBlank(message = "ZIP code is required")
    @Pattern(regexp = "\\d{5}", message = "ZIP code must be a 5-digit number")
    private String zipCode;

    private boolean savePaymentDetails;

    private String displayCardNumber;

    // Constructors, getters, and setters


    // Getters and Setters

    public String getDisplayCardNumber() {
        return displayCardNumber;
    }

    public void setDisplayCardNumber(String displayCardNumber) {
        this.displayCardNumber = displayCardNumber;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getCvv() {
        return cvv;
    }

    public void setCvv(String cvv) {
        this.cvv = cvv;
    }

    public String getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(String expirationDate) {
        this.expirationDate = expirationDate;
    }

    public String getCardName() {
        return cardName;
    }

    public void setCardName(String cardName) {
        this.cardName = cardName;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public boolean isSavePaymentDetails() {
        return savePaymentDetails;
    }

    public void setSavePaymentDetails(boolean savePaymentDetails) {
        this.savePaymentDetails = savePaymentDetails;
    }
}
