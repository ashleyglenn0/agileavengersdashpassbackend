package agileavengers.southwest_dashpass.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.util.ArrayList;

@Entity
@Table(name = "Customer")
public class Customer extends User {
    private int numberOfDashPassesAvailable, numberOfDashPassesUsed;
    private int customerId = 0;
    private boolean canPurchaseDashPass;
    private boolean canRedeemDashPass;

    private ArrayList<String> flights = new ArrayList<>();

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

    public Customer(String fname, String lname, String uname, String mail, String pword) {
        super(fname, lname, uname, mail, pword);
        this.setUserType(UserType.CUSTOMER);
        this.customerId = this.customerId + 1;
    }
}
