package agileavengers.southwest_dashpass.services;

import agileavengers.southwest_dashpass.models.*;
import agileavengers.southwest_dashpass.repository.DashPassReservationRepository;
import agileavengers.southwest_dashpass.repository.ReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class ReservationService {
    private ReservationRepository reservationRepository;
    private DashPassReservationRepository dashPassReservationRepository;

    @Autowired
    public ReservationService(ReservationRepository reservationRepository, DashPassReservationRepository dashPassReservationRepository) {
        this.reservationRepository = reservationRepository;
        this.dashPassReservationRepository = dashPassReservationRepository;
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





}
