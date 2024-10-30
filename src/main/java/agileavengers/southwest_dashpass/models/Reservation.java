package agileavengers.southwest_dashpass.models;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="reservation")
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
}


