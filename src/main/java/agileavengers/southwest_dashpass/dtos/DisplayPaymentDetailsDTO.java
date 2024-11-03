package agileavengers.southwest_dashpass.dtos;

public class DisplayPaymentDetailsDTO {
    private Long id;
    private String displayCardNumber;
    private String cardName;
    private String expirationDate;

    public DisplayPaymentDetailsDTO(Long id, String displayCardNumber, String cardName, String expirationDate) {
        this.id = id;
        this.displayCardNumber = displayCardNumber;
        this.cardName = cardName;
        this.expirationDate = expirationDate;
    }

    public Long getId() { return id; }
    public String getDisplayCardNumber() { return displayCardNumber; }
    public String getCardName() { return cardName; }
    public String getExpirationDate() { return expirationDate; }

    public void setId(Long id) {
        this.id = id;
    }

    public void setDisplayCardNumber(String displayCardNumber) {
        this.displayCardNumber = displayCardNumber;
    }

    public void setCardName(String cardName) {
        this.cardName = cardName;
    }

    public void setExpirationDate(String expirationDate) {
        this.expirationDate = expirationDate;
    }
}
