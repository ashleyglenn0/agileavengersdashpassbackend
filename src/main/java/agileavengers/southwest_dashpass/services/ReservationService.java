package agileavengers.southwest_dashpass.services;

import agileavengers.southwest_dashpass.models.*;
import agileavengers.southwest_dashpass.repository.DashPassReservationRepository;
import agileavengers.southwest_dashpass.repository.ReservationRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class ReservationService {
    private ReservationRepository reservationRepository;
    private DashPassReservationRepository dashPassReservationRepository;

    private CustomerService customerService;

    @Autowired
    public ReservationService(ReservationRepository reservationRepository, DashPassReservationRepository dashPassReservationRepository,
                              CustomerService customerService) {
        this.reservationRepository = reservationRepository;
        this.dashPassReservationRepository = dashPassReservationRepository;
        this.customerService = customerService;
    }

    public List<Reservation> getReservationsForCustomer(Long customerId) {
        // Call the repository method to retrieve reservations for the customer
        return reservationRepository.findByCustomerId(customerId);
    }

    public Reservation findById(Long id) {
        Optional<Reservation> reservation = reservationRepository.findById(id);
        // Handle case where reservation is not found
        return reservation.orElseThrow(() -> new RuntimeException("Reservation not found with id: " + id));
    }

    public Reservation createReservation(Customer customer, Flight flight, Optional<DashPass> optionalDashPass, double totalCost) {
        Reservation reservation = new Reservation();

        // Set customer and flight details
        reservation.setCustomer(customer);
        reservation.setFlights((List<Flight>) flight);
        reservation.setDateBooked(LocalDate.now()); // Set booking date to current date

        // If DashPass is present, associate it with the reservation
        if (optionalDashPass.isPresent()) {
            DashPass dashPass = optionalDashPass.get();
            DashPassReservation dashPassReservation = new DashPassReservation();
            dashPassReservation.setDashPass(dashPass);
            dashPassReservation.setReservation(reservation);

            // Save the DashPassReservation
            dashPassReservationRepository.save(dashPassReservation);
        }

        // Save the reservation in the database
        reservationRepository.save(reservation);

        return reservation;
    }

    public Reservation save(Reservation reservation){
        return reservationRepository.save(reservation);
    }

    public List<Reservation> getUpcomingReservationsForCustomer(Long customerId) {
        // Ensure this returns a List, not a single Reservation
        return reservationRepository.findByCustomer_Id(customerId);
    }

    @Transactional
    public void deleteReservation(Reservation reservation) {
        reservationRepository.delete(reservation);
    }

    public Reservation findLatestPaidReservationForCustomer(Customer customer) {
        return reservationRepository.findTopByCustomerAndPaymentStatusOrderByDateBookedDesc(customer, PaymentStatus.PAID);
    }

    public List<Reservation> getValidReservationsForCustomer(Long customerId) {
        // Fetch reservations that are only VALID
        return reservationRepository.findByCustomerIdAndStatus(customerId, ReservationStatus.VALID);
    }

    public Reservation findSoonestUpcomingReservationForCustomer(Long customerId) {
        Customer customer = customerService.findCustomerById(customerId);
        LocalDate today = LocalDate.now();

        return customer.getReservations().stream()
                .filter(reservation -> reservation.getStatus() == ReservationStatus.VALID) // Only valid reservations
                .flatMap(reservation -> reservation.getFlights().stream()) // Flattening flights
                .filter(flight -> !flight.getDepartureDate().isBefore(today)) // Only upcoming flights
                .sorted(Comparator.comparing(Flight::getDepartureDate)) // Sorting by soonest departure date
                .map(Flight::getReservation) // Mapping back to the reservation
                .findFirst() // Get the first upcoming reservation
                .orElse(null); // Return null if no upcoming reservation is found
    }

    public List<Reservation> findReservationsWithoutDashPass(Long customerId) {
        List<Reservation> reservations = reservationRepository.findByCustomerIdAndDashPassReservationsIsNull(customerId);
        if (reservations.isEmpty()) {
            System.out.println("No reservations without DashPass found for customer: " + customerId);
        } else {
            System.out.println("Reservations without DashPass for customer " + customerId + ": " + reservations.size());
        }
        return reservations;
    }

    public List<Reservation> findPastReservations(Customer customer) {
        LocalDate today = LocalDate.now();
        return reservationRepository.findByCustomerAndFlights_DepartureDateBefore(customer, today);
    }

    public void deleteReservation(Long reservationId) {
        Optional<Reservation> reservation = reservationRepository.findById(reservationId);

        if (reservation.isPresent()) {
            reservationRepository.delete(reservation.get());
        } else {
            throw new IllegalArgumentException("Reservation not found for ID: " + reservationId);
        }
    }

}
