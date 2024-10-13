package agileavengers.southwest_dashpass.repository;

import agileavengers.southwest_dashpass.models.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByCustomerId(Long customerId);

    Optional<Reservation> findTopByCustomerIdAndFlightDepartureDateAfterOrderByFlightDepartureDateAsc(Long customerId, LocalDate departureDate);

    List<Reservation> findByCustomer_Id(Long customerId);

}
