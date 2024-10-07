package agileavengers.southwest_dashpass.services;

import agileavengers.southwest_dashpass.models.Reservation;
import agileavengers.southwest_dashpass.repository.ReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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
}
