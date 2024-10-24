package agileavengers.southwest_dashpass.repository;

import agileavengers.southwest_dashpass.models.Airport;
import agileavengers.southwest_dashpass.models.Flight;
import agileavengers.southwest_dashpass.models.Reservation;
import agileavengers.southwest_dashpass.models.TripType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface FlightRepository extends JpaRepository<Flight, Long> {
    Optional<Flight> findById(Long flightID);

    Optional<Flight> findByFlightNumber(String flightNumber);

    boolean existsByFlightNumber(String flightNumber);

    List<Flight> findFlightsByDepartureDateBefore(LocalDate departureDate);


    List<Flight> findByDepartureAirportCodeAndArrivalAirportCode(Airport departure, Airport arrival);

    List<Flight> findByDepartureTimeBetween(LocalDateTime start, LocalDateTime end);

    List<Flight> findByArrivalTimeAfter(LocalDateTime arrivalTime);

    List<Flight> findByNumberOfSeatsAvailableGreaterThan(int remainingSeats);

    List<Flight> findByCanUseExistingDashPassTrue();

    List<Flight> findByNumberOfDashPassesAvailableGreaterThan(int dashPasses);

    List<Flight> findByReservation(Reservation reservation);

    // For one-way and outbound flights
    List<Flight> findByDepartureAirportCodeAndArrivalAirportCodeAndDepartureDateAndTripType(
            String departureAirportCode,
            String arrivalAirportCode,
            LocalDate departureDate,
            TripType tripType
    );

    // For round-trip return flights
    List<Flight> findByArrivalAirportCodeAndDepartureAirportCodeAndDepartureDateAndTripType(
            String arrivalAirportCode,
            String departureAirportCode,
            LocalDate returnDate,
            TripType tripType
    );


    @Query("SELECT f FROM Flight f WHERE " +
            "(f.departureAirportCode = :departureAirport AND f.arrivalAirportCode = :arrivalAirport AND f.departureDate = :departureDate) " +
            "OR (f.departureAirportCode = :arrivalAirport AND f.arrivalAirportCode = :departureAirport AND f.departureDate BETWEEN :departureDate AND :maxReturnDate)")
    List<Flight> findFlightsForRoundTrip(@Param("departureAirport") String departureAirport,
                                         @Param("arrivalAirport") String arrivalAirport,
                                         @Param("departureDate") LocalDate departureDate,
                                         @Param("maxReturnDate") LocalDate maxReturnDate);


    @Query("SELECT f FROM Flight f WHERE f.departureAirportCode = :departureAirportCode " +
            "AND f.arrivalAirportCode = :arrivalAirportCode " +
            "AND f.departureDate BETWEEN :startDate AND :endDate")
    List<Flight> findFlights(@Param("departureAirportCode") String departureAirportCode,
                             @Param("arrivalAirportCode") String arrivalAirportCode,
                             @Param("startDate") LocalDate startDate,
                             @Param("endDate") LocalDate endDate);

    @Query("SELECT f FROM Flight f WHERE f.departureAirportCode = :departure AND f.arrivalAirportCode = :arrival AND f.departureDate = :departureDate")
    List<Flight> findFlightsByDepartureAndArrivalAndDate(@Param("departure") String departure,
                                                         @Param("arrival") String arrival,
                                                         @Param("departureDate") LocalDate departureDate);

}
