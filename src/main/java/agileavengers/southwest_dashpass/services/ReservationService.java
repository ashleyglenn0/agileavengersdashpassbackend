package agileavengers.southwest_dashpass.services;

import agileavengers.southwest_dashpass.models.Reservation;
import agileavengers.southwest_dashpass.repository.ReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ReservationService {
    private ReservationRepository reservationRepository;

    @Autowired
    public ReservationService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
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
}
