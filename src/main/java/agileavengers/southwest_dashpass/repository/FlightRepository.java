package agileavengers.southwest_dashpass.repository;

import agileavengers.southwest_dashpass.dtos.RoundTripFlightDTO;
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


    // For one-way and outbound flights
    List<Flight> findByDepartureAirportCodeAndArrivalAirportCodeAndDepartureDateAndTripType(
            String departureAirportCode,
            String arrivalAirportCode,
            LocalDate departureDate,
            TripType tripType
    );


    @Query("SELECT f FROM Flight f WHERE " +
            "(f.departureAirportCode = :departureAirport AND f.arrivalAirportCode = :arrivalAirport AND f.departureDate = :departureDate) " +
            "OR (f.departureAirportCode = :arrivalAirport AND f.arrivalAirportCode = :departureAirport AND f.departureDate BETWEEN :departureDate AND :maxReturnDate)")
    List<Flight> findFlightsForRoundTrip(@Param("departureAirport") String departureAirport,
                                         @Param("arrivalAirport") String arrivalAirport,
                                         @Param("departureDate") LocalDate departureDate,
                                         @Param("maxReturnDate") LocalDate maxReturnDate);


//    @Query("SELECT f FROM Flight f WHERE f.departureAirportCode = :departureAirportCode " +
//            "AND f.arrivalAirportCode = :arrivalAirportCode " +
//            "AND f.departureDate BETWEEN :startDate AND :endDate")
//    List<Flight> findFlights(@Param("departureAirportCode") String departureAirportCode,
//                             @Param("arrivalAirportCode") String arrivalAirportCode,
//                             @Param("startDate") LocalDate startDate,
//                             @Param("endDate") LocalDate endDate);


    // For one-way flights
//    @Query("SELECT f FROM Flight f WHERE f.departureAirportCode = :departureCode " +
//            "AND f.arrivalAirportCode = :arrivalCode " +
//            "AND f.departureDate BETWEEN :startDate AND :endDate " +
//            "AND f.direction = 'OUTBOUND'")
//    List<Flight> findOneWayFlights(
//            @Param("departureCode") String departureCode,
//            @Param("arrivalCode") String arrivalCode,
//            @Param("startDate") LocalDate startDate,
//            @Param("endDate") LocalDate endDate);

    // For round-trip flights (outbound and matching return flights)
//    @Query("SELECT new agileavengers.southwest_dashpass.dtos.RoundTripFlightDTO(" +
//            "outbound.flightID, outbound.flightNumber, outbound.departureAirportCode, outbound.arrivalAirportCode, " +
//            "outbound.departureDate, outbound.departureTime, outbound.arrivalTime, outbound.price, " +
//            "returnFlight.flightID, returnFlight.flightNumber, returnFlight.departureAirportCode, returnFlight.arrivalAirportCode, " +
//            "returnFlight.departureDate, returnFlight.departureTime, returnFlight.arrivalTime, returnFlight.price) " +
//            "FROM Flight outbound JOIN Flight returnFlight ON outbound.tripId = returnFlight.tripId " +
//            "WHERE outbound.direction = 'OUTBOUND' AND returnFlight.direction = 'RETURN' " +
//            "AND outbound.departureAirportCode = :departureCode " +
//            "AND outbound.arrivalAirportCode = :arrivalCode " +
//            "AND outbound.departureDate BETWEEN :startDate AND :endDate " +
//            "AND returnFlight.departureDate BETWEEN :returnStartDate AND :returnEndDate")
//    List<RoundTripFlightDTO> findRoundTripFlights(
//            @Param("departureCode") String departureCode,
//            @Param("arrivalCode") String arrivalCode,
//            @Param("startDate") LocalDate startDate,
//            @Param("endDate") LocalDate endDate,
//            @Param("returnStartDate") LocalDate returnStartDate,
//            @Param("returnEndDate") LocalDate returnEndDate);


    @Query("SELECT f FROM Flight f WHERE f.departureAirportCode = :departureAirportCode " +
            "AND f.arrivalAirportCode = :arrivalAirportCode " +
            "AND f.departureDate BETWEEN :startDate AND :endDate " +
            "AND f.tripType = :tripType")
    List<Flight> findFlights(@Param("departureAirportCode") String departureAirportCode,
                             @Param("arrivalAirportCode") String arrivalAirportCode,
                             @Param("startDate") LocalDate startDate,
                             @Param("endDate") LocalDate endDate,
                             @Param("tripType") TripType tripType);

    @Query("SELECT f FROM Flight f " +
            "WHERE (f.departureAirportCode = :departureAirportCode AND f.arrivalAirportCode = :arrivalAirportCode " +
            "AND f.departureDate BETWEEN :departureDateRangeStart AND :departureDateRangeEnd AND f.direction = 'OUTBOUND') " +
            "OR (f.departureAirportCode = :arrivalAirportCode AND f.arrivalAirportCode = :departureAirportCode " +
            "AND f.departureDate BETWEEN :returnDateRangeStart AND :returnDateRangeEnd AND f.direction = 'RETURN')")
    List<Flight> findRoundTripFlightsByCriteria(
            @Param("departureAirportCode") String departureAirportCode,
            @Param("arrivalAirportCode") String arrivalAirportCode,
            @Param("departureDateRangeStart") LocalDate departureDateRangeStart,
            @Param("departureDateRangeEnd") LocalDate departureDateRangeEnd,
            @Param("returnDateRangeStart") LocalDate returnDateRangeStart,
            @Param("returnDateRangeEnd") LocalDate returnDateRangeEnd);



}


