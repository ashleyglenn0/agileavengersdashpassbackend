package agileavengers.southwest_dashpass.repository;

import agileavengers.southwest_dashpass.models.Airport;
import agileavengers.southwest_dashpass.models.Flight;
import agileavengers.southwest_dashpass.models.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface FlightRepository extends JpaRepository<Flight, Long> {
    List<Flight> findByFlightDepartureAirportAndFlightArrivalAirport(Airport departure, Airport arrival);

    List<Flight> findByFlightDepartureTimeBetween(LocalDateTime start, LocalDateTime end);

    List<Flight> findByFlightArrivalTimeAfter(LocalDateTime arrivalTime);

    List<Flight> findByNumberOfSeatsRemainingGreaterThan(int remainingSeats);

    List<Flight> findByCanUseDashPassForFlightTrue();

    List<Flight> findByNumberOfDashPassesAvailableGreaterThan(int dashPasses);

    List<Flight> findByReservation(Reservation reservation);




}
