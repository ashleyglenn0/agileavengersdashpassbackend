package agileavengers.southwest_dashpass.models;

public enum BagStatus {
    PRE_CHECK_IN("Pre Check In"),
    AT_THE_COUNTER("At the Counter"),
    LOADING("Loading"),
    IN_TRANSIT("In Transit"),
    UNLOADING("Unloading"),
    AT_CAROUSEL("At the Carousel"),
    DELAYED("Delayed"),
    PICKED_UP("Picked Up");

    private final String displayName;

    BagStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
