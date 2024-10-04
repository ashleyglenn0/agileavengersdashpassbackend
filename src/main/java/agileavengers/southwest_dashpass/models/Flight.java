package agileavengers.southwest_dashpass.models;

import jakarta.persistence.*;

@Entity
@Table(name="flight")
public class Flight {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="flightID")
    private Long flightId;
    @ManyToOne
    @JoinColumn(name = "flightDepartureAirport", referencedColumnName = "airportID")
    private Airport flightDepartureAirport;
    @ManyToOne
    @JoinColumn(name = "flightArrivalAirport", referencedColumnName = "airportID")
    private Airport flightArrivalAirport;
    @Column(name="numberOfSeatsAvailable")
    private int numberOfSeatsAvailable;
    @Column(name="numberOfSeatsSold")
    private int numberOfSeatsSold;
    @Column(name="numberOfSeatsRemaining")
    private int numberOfSeatsRemaining;
    @Column(name="maxNumberOfDashPassesForFlight")
    private int maxNumberOfDashPassesForFlight;
    @Column(name="numberOfDashPassesAvailable")
    private int numberOfDashPassesAvailable;
    @Column(name="DashPassAllowed")
    private boolean isDashPassAllowed;
    @ManyToOne
    @JoinColumn(name = "reservationId", referencedColumnName = "reservationID")
    private Reservation reservation;
    @Column(name="canUseDashPassForFlight")
    private boolean canUseDashPassForFlight;
    // Many flights can belong to one FlightReservation

    public Flight() {
        this.numberOfSeatsAvailable = 50;
        this.numberOfSeatsSold = 0;
        this.numberOfSeatsRemaining = 50;
        this.canUseDashPassForFlight = true;
    }

    public Flight(Airport dAirport, Airport aAirport, int numSeatsAvailable, int numSeatsSold, boolean canUsePass) {
        this.flightDepartureAirport = dAirport;
        this.flightArrivalAirport = aAirport;
        this.numberOfSeatsAvailable = numSeatsAvailable;
        this.numberOfSeatsSold = numSeatsSold;
        this.canUseDashPassForFlight = canUsePass;
    }

    public Long getFlightId() {
        return flightId;
    }

    public void setFlightId(Long flightId) {
        this.flightId = flightId;
    }

    public Airport getFlightDepartureAirport() {
        return flightDepartureAirport;
    }

    public void setFlightDepartureAirport(Airport flightDepartureAirport) {
        this.flightDepartureAirport = flightDepartureAirport;
    }

    public Airport getFlightArrivalAirport() {
        return flightArrivalAirport;
    }

    public void setFlightArrivalAirport(Airport flightArrivalAirport) {
        this.flightArrivalAirport = flightArrivalAirport;
    }

    public int getNumberOfSeatsAvailable() {
        return numberOfSeatsAvailable;
    }

    public void setNumberOfSeatsAvailable(int numberOfSeatsAvailable) {
        this.numberOfSeatsAvailable = numberOfSeatsAvailable;
    }

    public int getNumberOfSeatsSold() {
        return numberOfSeatsSold;
    }

    public void setNumberOfSeatsSold(int numberOfSeatsSold) {
        this.numberOfSeatsSold = numberOfSeatsSold;
    }

    public int getNumberOfSeatsRemaining() {
        // Optionally, calculate dynamically instead of having a setter
        return this.numberOfSeatsAvailable - this.numberOfSeatsSold;
    }

    public boolean isCanUseDashPassForFlight() {
        return canUseDashPassForFlight;
    }

    public void setCanUseDashPassForFlight(boolean canUseDashPassForFlight) {
        this.canUseDashPassForFlight = canUseDashPassForFlight;
    }
}
