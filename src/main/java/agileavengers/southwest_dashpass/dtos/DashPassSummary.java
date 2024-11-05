package agileavengers.southwest_dashpass.dtos;

import agileavengers.southwest_dashpass.models.Customer;

public class DashPassSummary {
    private int totalDashPassCount;
    private int dashPassInUseCount;
    private int availableDashPassCount;

    public DashPassSummary(Customer customer) {
        // Fetch already calculated values
        this.totalDashPassCount = customer.getTotalDashPassCount();
        this.dashPassInUseCount = customer.getDashPassInUseCount();
        this.availableDashPassCount = customer.getAvailableDashPassCount();
    }

    // Getters for the fields
    public int getTotalDashPassCount() {
        return totalDashPassCount;
    }

    public int getDashPassInUseCount() {
        return dashPassInUseCount;
    }

    public int getAvailableDashPassCount() {
        return availableDashPassCount;
    }
}
