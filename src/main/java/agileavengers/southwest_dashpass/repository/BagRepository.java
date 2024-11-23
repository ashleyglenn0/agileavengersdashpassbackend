package agileavengers.southwest_dashpass.repository;

import agileavengers.southwest_dashpass.models.Bag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BagRepository extends JpaRepository<Bag, Long> {
    // Method to find bags by reservation ID
    List<Bag> findByReservation_ReservationId(Long reservationId);

    // Method to find bags by customer ID
    List<Bag> findByCustomerId(Long customerId);
}
