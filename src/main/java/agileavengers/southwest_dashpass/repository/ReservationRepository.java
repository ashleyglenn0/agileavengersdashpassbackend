package agileavengers.southwest_dashpass.repository;

import agileavengers.southwest_dashpass.models.Customer;
import agileavengers.southwest_dashpass.models.PaymentStatus;
import agileavengers.southwest_dashpass.models.Reservation;
import agileavengers.southwest_dashpass.models.ReservationStatus;
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

    @Query("SELECT r FROM Reservation r LEFT JOIN FETCH r.bags WHERE r.customer = :customer AND r.paymentStatus = :paymentStatus ORDER BY r.dateBooked DESC")
    Reservation findTopByCustomerAndPaymentStatusWithBagsOrderByDateBookedDesc(
            @Param("customer") Customer customer,
            @Param("paymentStatus") PaymentStatus paymentStatus);

    // Query to fetch only valid reservations
    List<Reservation> findByCustomerIdAndStatus(Long customerId, ReservationStatus status);

    List<Reservation> findByCustomerIdAndDashPassReservationsIsNull(Long customerId);

    List<Reservation> findByCustomerAndFlights_DepartureDateBefore(Customer customer, LocalDate date);


}
