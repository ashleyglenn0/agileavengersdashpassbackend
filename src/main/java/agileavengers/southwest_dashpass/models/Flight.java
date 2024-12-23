package agileavengers.southwest_dashpass.models;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "flight")
public class Flight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "flight_id")
    private Long flightID;

    @Column(name="flight_number")
    private String flightNumber;

    @Column(name="departure_date")
    private LocalDate departureDate;
    @Column(name="arrival_date")
    private LocalDate arrivalDate;
    @Column(name="departure_time")
    private LocalTime departureTime;
    @Column(name="arrival_time")
    private LocalTime arrivalTime;
    @Column(name="price")
    private double price;

    @Column(name="departure_airport_code")
    private String departureAirportCode; // Use code instead of ID

    @Column(name="arrival_airport_code")
    private String arrivalAirportCode; // Use code instead of ID

    @Column(name="available_seats")
    private Integer availableSeats;

    @Column(name="can_add_new_dash_pass")
    private Boolean canAddNewDashPass;

    @Column(name="can_use_existing_dash_pass")
    private Boolean canUseExistingDashPass;

    @Column(name="max_number_of_dash_passes_for_flight")
    private Integer maxNumberOfDashPassesForFlight;

    @Column(name="number_of_dash_passes_available")
    private Integer numberOfDashPassesAvailable;

    @Column(name="number_of_seats_available")
    private Integer numberOfSeatsAvailable;

    @Column(name="seats_sold")
    private Integer seatsSold;

    @Column(name="return_date")
    private LocalDate returnDate;

    @OneToOne
    @JoinColumn(name = "return_flight_id")
    private Flight returnFlight;

    @ManyToOne
    @JoinColumn(name = "reservation_id", referencedColumnName = "reservation_id") // Use appropriate foreign key column name
    private Reservation reservation;  // Add this field
    @OneToMany(mappedBy = "flight")
    private List<DashPassReservation> dashPassReservations;

    @OneToMany(mappedBy = "flight", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Bag> bags = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name="trip_type")
    private TripType tripType;  // New trip type field

    @Column(name = "trip_id", nullable = false)
    private String tripId;
    @Column(name = "direction", nullable = false)
    private String direction;

    public Flight(){
        //empty for hibernate
    }

    public Flight(String flightNumber,
                  LocalDate departureDate,
                  LocalDate arrivalDate,
                  LocalDateTime departureTime,
                  LocalDateTime arrivalTime,
                  double price,
                  String departureAirportCode,
                  String arrivalAirportCode,
                  Integer numberOfSeatsAvailable,
                  Integer numberOfSeatsSold,
                  Boolean canAddDashPass,
                  Integer maxNumberOfDashPassesForFlight,
                  Integer numberOfDashPassesAvailable,
                  TripType tripType) {

        this.flightNumber = flightNumber;
        this.departureDate = departureDate;
        this.arrivalDate = arrivalDate;
        this.departureTime = LocalTime.from(departureTime);
        this.arrivalTime = LocalTime.from(arrivalTime);
        this.price = price;
        this.departureAirportCode = departureAirportCode;
        this.arrivalAirportCode = arrivalAirportCode;
        this.numberOfSeatsAvailable = numberOfSeatsAvailable;
        this.seatsSold = numberOfSeatsSold;
        this.canAddNewDashPass = canAddDashPass;
        this.maxNumberOfDashPassesForFlight = maxNumberOfDashPassesForFlight;
        this.numberOfDashPassesAvailable = numberOfDashPassesAvailable;
        this.tripType = tripType;
    }

    public Long getFlightID() {
        return flightID;
    }

    public void setFlightID(Long flightID) {
        this.flightID = flightID;
    }

    public String getFlightNumber() {
        return flightNumber;
    }

    public void setFlightNumber(String flightNumber) {
        this.flightNumber = flightNumber;
    }

    public LocalDate getDepartureDate() {
        return departureDate;
    }

    public void setDepartureDate(LocalDate departureDate) {
        this.departureDate = departureDate;
    }

    public LocalDate getArrivalDate() {
        return arrivalDate;
    }

    public void setArrivalDate(LocalDate arrivalDate) {
        this.arrivalDate = arrivalDate;
    }

    public LocalTime getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(LocalTime departureTime) {
        this.departureTime = departureTime;
    }

    public LocalTime getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(LocalTime arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getDepartureAirportCode() {
        return departureAirportCode;
    }

    public void setDepartureAirportCode(String departureAirportCode) {
        this.departureAirportCode = departureAirportCode;
    }

    public String getArrivalAirportCode() {
        return arrivalAirportCode;
    }

    public void setArrivalAirportCode(String arrivalAirportCode) {
        this.arrivalAirportCode = arrivalAirportCode;
    }

    public Integer getAvailableSeats() {
        return availableSeats;
    }

    public void setAvailableSeats(Integer availableSeats) {
        this.availableSeats = availableSeats;
    }

    public Boolean isCanAddNewDashPass() {
        return canAddNewDashPass;
    }

    public void setCanAddNewDashPass(Boolean canAddDashPass) {
        this.canAddNewDashPass = canAddDashPass;
    }

    public Boolean isCanUseExistingDashPass() {
        return canUseExistingDashPass;
    }

    public void setCanUseExistingDashPass(Boolean canUseExistingDashPass) {
        this.canUseExistingDashPass = canUseExistingDashPass;
    }

    public Integer getMaxNumberOfDashPassesForFlight() {
        return maxNumberOfDashPassesForFlight;
    }

    public void setMaxNumberOfDashPassesForFlight(Integer maxNumberOfDashPassesForFlight) {
        this.maxNumberOfDashPassesForFlight = maxNumberOfDashPassesForFlight;
    }

    public Integer getNumberOfDashPassesAvailable() {
        return numberOfDashPassesAvailable;
    }

    public void setNumberOfDashPassesAvailable(Integer numberOfDashPassesAvailable) {
        this.numberOfDashPassesAvailable = numberOfDashPassesAvailable;
    }

    public Integer getNumberOfSeatsAvailable() {
        return numberOfSeatsAvailable;
    }

    public void setNumberOfSeatsAvailable(Integer numberOfSeatsAvailable) {
        this.numberOfSeatsAvailable = numberOfSeatsAvailable;
    }

    public Integer getSeatsSold() {
        return seatsSold;
    }

    public void setSeatsSold(Integer seatsSold) {
        this.seatsSold = seatsSold;
    }

    public Integer getSeatsRemaining() {
        return this.numberOfSeatsAvailable - this.seatsSold;
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

    public TripType getTripType() {
        return tripType;
    }

    public void setTripType(TripType tripType) {
        this.tripType = tripType;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    public Flight getReturnFlight() {
        return returnFlight;
    }

    public void setReturnFlightID(Flight returnFlight) {
        this.returnFlight = returnFlight;
    }

    public void setReturnFlight(Flight returnFlight) {
        this.returnFlight = returnFlight;
    }

    public String getTripId() {
        return tripId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public boolean canAddExistingDashPass() {
        // Only allow using an existing DashPass if there are available DashPasses on the flight
        return numberOfDashPassesAvailable > 0 && canUseExistingDashPass;
    }

    public boolean canAddNewDashPass() {
        // Only allow purchasing a new DashPass if there are available DashPasses on the flight
        return numberOfDashPassesAvailable > 0 && canAddNewDashPass;
    }

    public List<Bag> getBags() {
        return bags;
    }

    public void setBags(List<Bag> bags) {
        this.bags = bags;
    }
}

