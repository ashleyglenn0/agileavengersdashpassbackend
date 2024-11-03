package agileavengers.southwest_dashpass.services;

import agileavengers.southwest_dashpass.dtos.RoundTripFlightDTO;
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
import java.util.stream.Collectors;

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


    // Helper method to check if the customer can use an existing DashPass
    private boolean checkCustomerDashPasses(Customer customer, Flight flight) {
        return customer.getDashPasses().stream().anyMatch(dashPass -> !dashPass.isRedeemed());
    }

    // Helper method to check if new DashPasses can be added to the flight
    private boolean checkIfCanAddDashPass(Flight flight) {
        return flight.getNumberOfDashPassesAvailable() > 0;
    }

    public void save(Flight flight) {
        flightRepository.save(flight);
    }

    public List<Flight> searchOutboundFlights(String departureAirportCode, String arrivalAirportCode,
                                              LocalDate startDate, LocalDate endDate, TripType tripType) {
        return flightRepository.findFlights(departureAirportCode, arrivalAirportCode, startDate, endDate, tripType);
    }


    public FlightSearchResponse findFlights(String departureAirportCode,
                                            String arrivalAirportCode,
                                            LocalDate selectedDate,
                                            LocalDate returnDate,
                                            TripType tripType,
                                            Customer customer) {
        FlightSearchResponse flightResponse = new FlightSearchResponse();

        if (tripType == TripType.ONE_WAY) {
            // Use searchOutboundFlights to get outbound flights for one-way trips
            List<Flight> flights = searchOutboundFlights(departureAirportCode, arrivalAirportCode, selectedDate, selectedDate, tripType);

            flights.forEach(flight -> {
                flight.setCanUseExistingDashPass(checkCustomerDashPasses(customer, flight));
                flight.setCanAddNewDashPass(checkIfCanAddDashPass(flight));
            });

            flightResponse.setOutboundFlights(flights);
            flightResponse.setReturnFlights(Collections.emptyList()); // No return flights for one-way

        } else if (tripType == TripType.ROUND_TRIP) {
            // Fetch round-trip flights
            List<Flight> roundTripFlights = flightRepository.findFlightsForRoundTrip(
                    departureAirportCode, arrivalAirportCode, selectedDate, returnDate
            );

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

            flightResponse.setOutboundFlights(departingFlights);
            flightResponse.setReturnFlights(returningFlights);
        }

        return flightResponse;
    }


    // In FlightService.java
    public List<RoundTripFlightDTO> searchRoundTripFlights(String departureAirportCode, String arrivalAirportCode,
                                                           LocalDate departureDateRangeStart, LocalDate departureDateRangeEnd,
                                                           LocalDate returnDateRangeStart, LocalDate returnDateRangeEnd) {
        List<Flight> flights = flightRepository.findRoundTripFlightsByCriteria(
                departureAirportCode, arrivalAirportCode, departureDateRangeStart, departureDateRangeEnd,
                returnDateRangeStart, returnDateRangeEnd);

        Map<String, RoundTripFlightDTO> roundTripMap = new HashMap<>();
        for (Flight flight : flights) {
            roundTripMap.computeIfAbsent(flight.getTripId(), id -> new RoundTripFlightDTO(id))
                    .addFlight(flight);
        }

        List<RoundTripFlightDTO> roundTripFlights = new ArrayList<>(roundTripMap.values());

        // Log to confirm that roundTripFlights contain outbound and return flights
        for (RoundTripFlightDTO trip : roundTripFlights) {
            System.out.println("Trip ID: " + trip.getTripId());
            System.out.println("Outbound Flights Count: " + trip.getOutboundFlights().size());
            System.out.println("Return Flights Count: " + trip.getReturnFlights().size());
        }

        return roundTripFlights;
    }

    public void updateExistingFlights() {
        List<Flight> flights = flightRepository.findAll();
        Random random = new Random();

        for (Flight flight : flights) {
            int maxSeats = 50 + random.nextInt(150);  // Randomly set between 50 and 200 for variation
            flight.setAvailableSeats(maxSeats);
            flight.setNumberOfSeatsAvailable(maxSeats - flight.getSeatsSold());  // Calculate based on seatsSold
            flightRepository.save(flight);
        }

        System.out.println("Existing flights updated with new availableSeats and numberOfAvailableSeats values.");
    }


}

