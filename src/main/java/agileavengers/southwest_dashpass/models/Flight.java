package agileavengers.southwest_dashpass.models;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

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

    @Column(name="flightNumber")
    private String flightNumber;

    @ManyToOne
    @JoinColumn(name = "reservation_id") // Use appropriate foreign key column name
    private Reservation reservation;  // Add this field

    public String getFlightNumber() {
        return flightNumber;
    }

    public void setFlightNumber(String flightNumber) {
        this.flightNumber = flightNumber;
    }

    @Column(name="numberOfSeatsRemaining")
    private int numberOfSeatsRemaining;
    @Column(name="maxNumberOfDashPassesForFlight")
    private int maxNumberOfDashPassesForFlight;
    @Column(name="numberOfDashPassesAvailable")
    private int numberOfDashPassesAvailable;
    @Column(name="DashPassAllowed")
    private boolean isDashPassAllowed;
    @OneToMany(mappedBy = "flight", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DashPassReservation> dashPassReservations;
    @Column(name="canUseDashPassForFlight")
    private boolean canUseDashPassForFlight;
    @Column(name="flightDepartureTime")
    private LocalDateTime flightDepartureTime;
    @Column(name="flightArrivalTime")
    private LocalDateTime flightArrivalTime;

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
        this.numberOfSeatsRemaining = numSeatsAvailable - numSeatsSold; // Automatically calculate remaining seats
        this.isDashPassAllowed = true; // Default value (can be updated later)
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
    public Reservation getReservation() {
        return reservation;
    }

    public void setReservation(Reservation reservation) {
        this.reservation = reservation;
    }

    public List<DashPassReservation> getDashPassReservations() {
        return dashPassReservations;
    }

    public void setDashPassReservations(List<DashPassReservation> dashPassReservations) {
        this.dashPassReservations = dashPassReservations;
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
        return numberOfSeatsRemaining;
    }

    public void setNumberOfSeatsRemaining(int numberOfSeatsRemaining) {
        this.numberOfSeatsRemaining = numberOfSeatsRemaining;
    }

    public int getMaxNumberOfDashPassesForFlight() {
        return maxNumberOfDashPassesForFlight;
    }

    public void setMaxNumberOfDashPassesForFlight(int maxNumberOfDashPassesForFlight) {
        this.maxNumberOfDashPassesForFlight = maxNumberOfDashPassesForFlight;
    }

    public int getNumberOfDashPassesAvailable() {
        return numberOfDashPassesAvailable;
    }

    public void setNumberOfDashPassesAvailable(int numberOfDashPassesAvailable) {
        this.numberOfDashPassesAvailable = numberOfDashPassesAvailable;
    }

    public boolean isDashPassAllowed() {
        return isDashPassAllowed;
    }

    public void setDashPassAllowed(boolean dashPassAllowed) {
        isDashPassAllowed = dashPassAllowed;
    }


    public boolean isCanUseDashPassForFlight() {
        return canUseDashPassForFlight;
    }

    public void setCanUseDashPassForFlight(boolean canUseDashPassForFlight) {
        this.canUseDashPassForFlight = canUseDashPassForFlight;
    }

    public LocalDateTime getFlightDepartureTime() {
        return flightDepartureTime;
    }

    public void setFlightDepartureTime(LocalDateTime flightDepartureTime) {
        this.flightDepartureTime = flightDepartureTime;
    }

    public LocalDateTime getFlightArrivalTime() {
        return flightArrivalTime;
    }

    public void setFlightArrivalTime(LocalDateTime flightArrivalTime) {
        this.flightArrivalTime = flightArrivalTime;
    }
    public void updateSeatsRemaining() {
        this.numberOfSeatsRemaining = this.numberOfSeatsAvailable - this.numberOfSeatsSold;
    }
    public boolean canIssueDashPass() {
        return this.numberOfDashPassesAvailable > 0 && this.isDashPassAllowed;
    }


}
