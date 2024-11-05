package agileavengers.southwest_dashpass.dtos;

public class DashPassSummary {
    private int totalDashPasses;
    private int inUseDashPasses;
    private int availableForPurchase;

    public DashPassSummary(int totalDashPasses, int inUseDashPasses, int availableForPurchase) {
        this.totalDashPasses = totalDashPasses;
        this.inUseDashPasses = inUseDashPasses;
        this.availableForPurchase = availableForPurchase;
    }

    // Getters and setters...

    public int getTotalDashPasses() {
        return totalDashPasses;
    }

    public void setTotalDashPasses(int totalDashPasses) {
        this.totalDashPasses = totalDashPasses;
    }

    public int getInUseDashPasses() {
        return inUseDashPasses;
    }

    public void setInUseDashPasses(int inUseDashPasses) {
        this.inUseDashPasses = inUseDashPasses;
    }

    public int getAvailableForPurchase() {
        return availableForPurchase;
    }

    public void setAvailableForPurchase(int availableForPurchase) {
        this.availableForPurchase = availableForPurchase;
    }
}
