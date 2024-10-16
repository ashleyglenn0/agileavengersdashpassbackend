package agileavengers.southwest_dashpass.models;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name="dashpass_reservation")
public class DashPassReservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="ID")
    private Long id;
    @OneToOne
    @JoinColumn(name = "dashpassID", referencedColumnName = "dashPassID", nullable = false)
    private DashPass dashPass;
    @ManyToOne
    @JoinColumn(name = "customerID", referencedColumnName = "ID", nullable = false)
    private Customer customer;
    @Column(name="bookingDate")
    private LocalDate bookingDate;

    @ManyToOne
    @JoinColumn(name = "flight_id")
    private Flight flight;

    // Many-to-One relationship with Reservation
    @ManyToOne
    @JoinColumn(name = "reservation_id")
    private Reservation reservation;

    // Other fields and methods...

    // Getter and setter for Reservation


    public DashPassReservation(){
        this.bookingDate = LocalDate.now();
    }

    public DashPassReservation(Customer customer, DashPass dashpass, Flight flight, LocalDate bookingDate){
        this.customer = customer;
        this.dashPass = dashpass;
        this.bookingDate = bookingDate;
        this.flight = flight;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setDashPass(DashPass dashPass) {
        this.dashPass = dashPass;
    }

    public Flight getFlight() {
        return flight;
    }

    public void setFlight(Flight flight) {
        this.flight = flight;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public LocalDate getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(LocalDate bookingDate) {
        this.bookingDate = bookingDate;
    }

    public DashPass getDashPass() {
        return dashPass;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public void setReservation(Reservation reservation) {
        this.reservation = reservation;
    }

}


