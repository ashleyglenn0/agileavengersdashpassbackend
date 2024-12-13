package agileavengers.southwest_dashpass.repository;

import agileavengers.southwest_dashpass.models.Customer;
import agileavengers.southwest_dashpass.models.DashPassReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface DashPassReservationRepository extends JpaRepository<DashPassReservation, Long> {
    List<DashPassReservation> findByCustomerAndReservation_DateBookedBefore(Customer customer, LocalDate date);

    @Query("SELECT r FROM DashPassReservation r WHERE r.customer = :customer AND r.isActive = :isActive")
    List<DashPassReservation> findByCustomerAndIsActive(@Param("customer") Customer customer, @Param("isActive") boolean isActive);
}
