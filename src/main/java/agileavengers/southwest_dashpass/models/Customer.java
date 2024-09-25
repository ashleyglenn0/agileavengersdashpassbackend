package agileavengers.southwest_dashpass.models;

public class Customer extends User {
    private int customerId, numberOfDashPassesAvailable, numberOfDashPassesUsed;
    private boolean canPurchaseDashPass;
    private boolean canRedeemDashPass;

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public int getNumberOfDashPassesAvailable() {
        return numberOfDashPassesAvailable;
    }

    public void setNumberOfDashPassesAvailable(int numberOfDashPassesAvailable) {
        this.numberOfDashPassesAvailable = numberOfDashPassesAvailable;
    }

    public int getNumberOfDashPassesUsed() {
        return numberOfDashPassesUsed;
    }

    public void setNumberOfDashPassesUsed(int numberOfDashPassesUsed) {
        this.numberOfDashPassesUsed = numberOfDashPassesUsed;
    }

    public Customer(String fname, String lname, String mail, String pword) {
        super(fname, lname, mail, pword);
        this.setUserType(UserType.CUSTOMER);
    }
}
