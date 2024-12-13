package agileavengers.southwest_dashpass.models;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Table(name="reservation")
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reservation_id")  // Explicitly map the column name
    private Long reservationId;
    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name="customerID", referencedColumnName = "ID", nullable = false)
    private Customer customer;

    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Flight> flights = new ArrayList<>();

    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DashPassReservation> dashPassReservations = new ArrayList<>();
    @Column(name="dateBooked")
    LocalDate dateBooked = LocalDate.now();

    @Column(name="airportCode")
    private String airportCode;

    @Column(name="total_price")
    private double totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(name="trip_type")
    private TripType tripType;  // New trip type field

    @Column(name = "flight_departure_date")
    private LocalDate flightDepartureDate;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    @Enumerated(EnumType.STRING)
    private ReservationStatus status = ReservationStatus.VALID; // Default to VALID
    @Column(name = "confirmation_number")
    private String confirmationNumber;

    @ElementCollection
    private Map<Long, String> flightTerminals = new HashMap<>(); // Map to store flight ID and terminal info
    @ElementCollection
    private Map<Long, String> flightGates = new HashMap<>(); // Map to store flight ID and gate info

    @Column(name = "isValidated")
    private Boolean isValidated;

    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Bag> bags = new ArrayList<>();

    private int bagQuantity; // Total number of bags
    private double bagCost;  // Total cost for the bags


    public Reservation(Customer customer, LocalDate bookingDate){
        this.customer = customer;
        this.dateBooked = bookingDate;
        this.flights = new ArrayList<>();
    }

    public Reservation(){
        this.customer = new Customer();
        this.dateBooked = LocalDate.now();
    }

    public Long getReservationId() {
        return reservationId;
    }

    public void setReservationId(Long reservationId) {
        this.reservationId = reservationId;
    }

    public void setDateBooked(LocalDate dateBooked) {
        this.dateBooked = dateBooked;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public LocalDate getDateBooked() {
        return dateBooked;
    }

    public List<Flight> getFlights() {
        return flights;
    }

    public void setFlights(List<Flight> flights) {
        this.flights = flights;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public TripType getTripType() {
        return tripType;
    }

    public void setTripType(TripType tripType) {
        this.tripType = tripType;
    }

    public LocalDate getFlightDepartureDate() {
        return flightDepartureDate;
    }

    public void setFlightDepartureDate(LocalDate flightDepartureDate) {
        this.flightDepartureDate = flightDepartureDate;
    }

    // Getter and setter
    public List<DashPassReservation> getDashPassReservations() {
        return dashPassReservations;
    }

    public void setDashPassReservations(List<DashPassReservation> dashPassReservations) {
        this.dashPassReservations = dashPassReservations;
    }

    public String getAirportCode() {
        return airportCode;
    }

    public void setAirportCode(String airportCode) {
        this.airportCode = airportCode;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public void setStatus(ReservationStatus status) {
        this.status = status;
    }

    public String getConfirmationNumber() {
        return confirmationNumber;
    }

    public void setConfirmationNumber(String confirmationNumber) {
        this.confirmationNumber = confirmationNumber;
    }

    public boolean hasDashPass() {
        return dashPassReservations != null && !dashPassReservations.isEmpty();
    }

    // Getters and setters for flightTerminals and flightGates
    public void setFlightTerminal(Long flightId, String terminal) {
        flightTerminals.put(flightId, terminal);
    }

    public String getFlightTerminal(Long flightId) {
        return flightTerminals.get(flightId);
    }

    public void setFlightGate(Long flightId, String gate) {
        flightGates.put(flightId, gate);
    }

    public String getFlightGate(Long flightId) {
        return flightGates.get(flightId);
    }

    public Boolean getValidated() {
        return isValidated;
    }

    public void setValidated(Boolean validated) {
        isValidated = validated;
    }

    public List<Bag> getBags() {
        return bags;
    }

    public void setBags(List<Bag> bags) {
        this.bags = bags;
    }

    public int getBagQuantity() {
        return bagQuantity;
    }

    public void setBagQuantity(int bagQuantity) {
        this.bagQuantity = bagQuantity;
    }

    public double getBagCost() {
        return bagCost;
    }

    public void setBagCost(double bagCost) {
        this.bagCost = bagCost;
    }
}


