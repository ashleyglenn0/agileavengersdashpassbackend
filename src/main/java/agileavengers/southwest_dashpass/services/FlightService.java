package agileavengers.southwest_dashpass.services;

import agileavengers.southwest_dashpass.models.*;
import agileavengers.southwest_dashpass.repository.AirportRepository;
import agileavengers.southwest_dashpass.repository.FlightRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
public class FlightService {
    private final FlightRepository flightRepository;
    private final AirportRepository airportRepository;

    private final CustomerService customerService;
    private final ReservationService reservationService;
    private final DashPassService dashPassService;


    @Autowired
    private FlightService(FlightRepository flightRepository, AirportRepository airportRepository,
                          CustomerService customerService, ReservationService reservationService,
                          DashPassService dashPassService) {
        this.flightRepository = flightRepository;
        this.airportRepository = airportRepository;
        this.reservationService = reservationService;
        this.customerService = customerService;
        this.dashPassService = dashPassService;


    }

    public Flight findFlightById(Long flightID) {
        return flightRepository.findById(flightID)
                .orElseThrow(() -> new EntityNotFoundException("Flight not found with ID: " + flightID));
    }

    public FlightSearchResponse findFlights(String departureAirportCode,
                                            String arrivalAirportCode,
                                            LocalDate selectedDate,
                                            LocalDate returnDate,
                                            TripType tripType,
                                            Customer customer) {
        FlightSearchResponse flightResponse = new FlightSearchResponse();

        if (tripType == TripType.ONE_WAY) {
            // Fetch one-way flights
            List<Flight> flights = flightRepository.findByDepartureAirportCodeAndArrivalAirportCodeAndDepartureDateAndTripType(
                    departureAirportCode, arrivalAirportCode, selectedDate, tripType
            );
            flights.forEach(flight -> {
                flight.setCanUseExistingDashPass(checkCustomerDashPasses(customer, flight));
                flight.setCanAddNewDashPass(checkIfCanAddDashPass(flight));
            });
            flightResponse.setOutboundFlights(flights);
            flightResponse.setReturnFlights(Collections.emptyList()); // No return flights for one-way

        } else if (tripType == TripType.ROUND_TRIP) {
            // Fetch both outbound and return flights using the date range
            List<Flight> roundTripFlights = flightRepository.findFlightsForRoundTrip(
                    departureAirportCode, arrivalAirportCode, selectedDate, returnDate
            );

            // Separate the outbound and return flights from the result
            List<Flight> departingFlights = new ArrayList<>();
            List<Flight> returningFlights = new ArrayList<>();

            for (Flight flight : roundTripFlights) {
                if (flight.getDepartureDate().equals(selectedDate)) {
                    departingFlights.add(flight); // Outbound flight
                } else if (flight.getDepartureDate().isAfter(selectedDate) && flight.getDepartureDate().isBefore(returnDate.plusDays(1))) {
                    returningFlights.add(flight); // Return flight
                }
            }

            departingFlights.forEach(flight -> {
                flight.setCanUseExistingDashPass(checkCustomerDashPasses(customer, flight));
                flight.setCanAddNewDashPass(checkIfCanAddDashPass(flight));
            });

            returningFlights.forEach(flight -> {
                flight.setCanUseExistingDashPass(checkCustomerDashPasses(customer, flight));
                flight.setCanAddNewDashPass(checkIfCanAddDashPass(flight));
            });

            // Set the flights in the response object
            flightResponse.setOutboundFlights(departingFlights);
            flightResponse.setReturnFlights(returningFlights);
        }

        return flightResponse;
    }

    // Helper method to check if the customer can use an existing DashPass
    private boolean checkCustomerDashPasses(Customer customer, Flight flight) {
        return customer.getDashPasses().stream().anyMatch(dashPass -> !dashPass.isRedeemed());
    }

    // Helper method to check if new DashPasses can be added to the flight
    private boolean checkIfCanAddDashPass(Flight flight) {
        return flight.getNumberOfDashPassesAvailable() > 0;
    }

    public List<Flight> getRoundTripFlights(String departureAirport, String arrivalAirport, LocalDate departureDate, LocalDate returnDate) {
        return flightRepository.findFlightsForRoundTrip(departureAirport, arrivalAirport, departureDate, returnDate);
    }

    public Reservation confirmFlightForCustomer(Long flightID, Long customerID, String dashPassOption) {
        // Fetch flight and customer
        Flight flight = flightRepository.findById(flightID)
                .orElseThrow(() -> new EntityNotFoundException("Flight not found with ID: " + flightID));

        Customer customer = customerService.findCustomerById(customerID);

        // Create a reservation object
        Reservation reservation = new Reservation();
        reservation.setCustomer(customer);
        reservation.getFlights().add(flight); // Add flight to reservation

        DashPassReservation dashPassReservation = null;

        // Handle DashPass logic
        if ("existing".equals(dashPassOption)) {
            Optional<DashPass> optionalDashPass = dashPassService.findAvailableDashPass(customer);
            if (optionalDashPass.isPresent()) {
                DashPass dashPass = optionalDashPass.get();
                dashPass.setRedeemed(true); // Mark as redeemed
                dashPassService.save(dashPass); // Save redeemed DashPass

                dashPassReservation = new DashPassReservation(customer, dashPass, flight, LocalDate.now());
            } else {
                throw new RuntimeException("No available DashPass found for the customer.");
            }
        } else if ("purchase".equals(dashPassOption)) {
            DashPass newDashPass = dashPassService.createAndAssignDashPassDuringPurchase(customer, flight);
            dashPassReservation = new DashPassReservation(customer, newDashPass, flight, LocalDate.now());
        } else {
            throw new IllegalArgumentException("Invalid DashPass option: " + dashPassOption);
        }

        // Link DashPassReservation to the reservation
        if (dashPassReservation != null) {
            dashPassReservation.setReservation(reservation);
            flight.getDashPassReservations().add(dashPassReservation);
        }

        // Save the reservation and return
        reservationService.save(reservation);
        flightRepository.save(flight); // Save updated flight with DashPassReservation

        return reservation;
    }


    public void save(Flight flight) {
        flightRepository.save(flight);
    }


}

