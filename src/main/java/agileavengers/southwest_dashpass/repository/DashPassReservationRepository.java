package agileavengers.southwest_dashpass.repository;

import agileavengers.southwest_dashpass.models.Customer;
import agileavengers.southwest_dashpass.models.DashPassReservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface DashPassReservationRepository extends JpaRepository<DashPassReservation, Long> {
    List<DashPassReservation> findByCustomerAndReservation_DateBookedBefore(Customer customer, LocalDate date);
}
