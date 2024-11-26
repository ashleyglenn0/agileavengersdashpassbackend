package agileavengers.southwest_dashpass.tools;

import agileavengers.southwest_dashpass.models.Airport;
import agileavengers.southwest_dashpass.models.Flight;
import agileavengers.southwest_dashpass.models.TripType;
import agileavengers.southwest_dashpass.repository.AirportRepository;
import agileavengers.southwest_dashpass.repository.FlightRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Component
public class FlightDataPopulator {
    @Autowired
    private FlightRepository flightRepository;

    @Autowired
    private AirportRepository airportRepository;

    // Map of airport codes to their corresponding names
    private static final Map<String, String> AIRPORTS;

    static {
        AIRPORTS = new HashMap<>();
        AIRPORTS.put("LAX", "Los Angeles International Airport");
        AIRPORTS.put("JFK", "John F. Kennedy International Airport");
        AIRPORTS.put("ORD", "O'Hare International Airport");
        AIRPORTS.put("ATL", "Hartsfield-Jackson Atlanta International Airport");
        AIRPORTS.put("SFO", "San Francisco International Airport");
        AIRPORTS.put("MIA", "Miami International Airport");
        AIRPORTS.put("DFW", "Dallas/Fort Worth International Airport");
        AIRPORTS.put("SEA", "Seattle-Tacoma International Airport");
        AIRPORTS.put("STL", "St. Louis Lambert International Airport");
        AIRPORTS.put("IAD", "Ronald Reagan Washington National Airport");
        AIRPORTS.put("MSY", "Louis Armstrong New Orleans International Airport");
        AIRPORTS.put("LAS", "McCarran International Airport");
        AIRPORTS.put("DTW", "Detroit Metropolitan Wayne County Airport");
        AIRPORTS.put("AUS", "Austin-Bergstrom International Airport");
        AIRPORTS.put("IAH", "George Bush Intercontinental Airport");
        AIRPORTS.put("MEM", "Memphis International Airport");
        AIRPORTS.put("FLL", "Fort Lauderdale - Hollywood International Airport");
        AIRPORTS.put("MCO", "Orlando International Airport");
        AIRPORTS.put("PHL", "Philadelphia International Airport");
        AIRPORTS.put("SAN", "San Diego International Airport");
    }

    public void populateFlights(int count) {
        // Example hardcoded values for testing
        String[] departureCodes = {"LAX", "JFK", "ORD", "ATL", "SFO"};
        String[] arrivalCodes = {"MIA", "DFW", "SEA", "PHL", "MSY"};
        TripType[] tripTypes = {TripType.ONE_WAY, TripType.ROUND_TRIP};

        for (int i = 0; i < count; i++) {
            String tripId = UUID.randomUUID().toString();
            String departureCode = departureCodes[i % departureCodes.length];
            String arrivalCode = arrivalCodes[i % arrivalCodes.length];
            TripType tripType = tripTypes[i % tripTypes.length];

            // Debugging: Log hardcoded data
            System.out.println("Trip ID: " + tripId);
            System.out.println("Departure Code: " + departureCode);
            System.out.println("Arrival Code: " + arrivalCode);
            System.out.println("Trip Type: " + tripType);

            // Outbound flight
            String flightNumber = "FL" + (i + 1000); // Unique flight number
            LocalDate departureDate = LocalDate.now().plusDays(i);
            LocalDateTime departureTime = departureDate.atTime(8 + (i % 10), 30); // Fixed morning times
            LocalDateTime arrivalTime = departureTime.plusHours(3); // Fixed duration
            int maxSeats = 150; // Fixed seat capacity
            int seatsSold = 50; // Fixed seats sold
            int dashPassesAvailable = 10; // Fixed DashPass availability

            Flight outboundFlight = new Flight();
            outboundFlight.setFlightNumber(flightNumber);
            outboundFlight.setDepartureAirportCode(departureCode);
            outboundFlight.setArrivalAirportCode(arrivalCode);
            outboundFlight.setDepartureDate(departureDate);
            outboundFlight.setArrivalDate(arrivalTime.toLocalDate());
            outboundFlight.setDepartureTime(departureTime.toLocalTime());
            outboundFlight.setArrivalTime(arrivalTime.toLocalTime());
            outboundFlight.setDirection("OUTBOUND");
            outboundFlight.setTripId(tripId);
            outboundFlight.setTripType(tripType);
            outboundFlight.setPrice(299.99); // Fixed price
            outboundFlight.setAvailableSeats(maxSeats);
            outboundFlight.setSeatsSold(seatsSold);
            outboundFlight.setNumberOfSeatsAvailable(maxSeats - seatsSold);
            outboundFlight.setMaxNumberOfDashPassesForFlight(15);
            outboundFlight.setNumberOfDashPassesAvailable(dashPassesAvailable);
            outboundFlight.setCanAddNewDashPass(true);
            outboundFlight.setCanUseExistingDashPass(true);

            // Save outbound flight
            flightRepository.save(outboundFlight);
            System.out.println("Saved Outbound Flight: " + outboundFlight.getFlightNumber());

            // Return flight for round trips
            if (tripType == TripType.ROUND_TRIP) {
                LocalDate returnDate = departureDate.plusDays(3); // Return 3 days later
                LocalDateTime returnDepartureTime = returnDate.atTime(10 + (i % 5), 0); // Fixed times
                LocalDateTime returnArrivalTime = returnDepartureTime.plusHours(3); // Fixed duration

                Flight returnFlight = new Flight();
                returnFlight.setFlightNumber(flightNumber + "R");
                returnFlight.setDepartureAirportCode(arrivalCode);
                returnFlight.setArrivalAirportCode(departureCode);
                returnFlight.setDepartureDate(returnDepartureTime.toLocalDate());
                returnFlight.setArrivalDate(returnArrivalTime.toLocalDate());
                returnFlight.setDepartureTime(returnDepartureTime.toLocalTime());
                returnFlight.setArrivalTime(returnArrivalTime.toLocalTime());
                returnFlight.setDirection("RETURN");
                returnFlight.setTripId(tripId);
                returnFlight.setTripType(TripType.ROUND_TRIP);
                returnFlight.setPrice(299.99);
                returnFlight.setAvailableSeats(maxSeats);
                returnFlight.setSeatsSold(seatsSold);
                returnFlight.setNumberOfSeatsAvailable(maxSeats - seatsSold);
                returnFlight.setMaxNumberOfDashPassesForFlight(15);
                returnFlight.setNumberOfDashPassesAvailable(dashPassesAvailable);
                returnFlight.setCanAddNewDashPass(true);
                returnFlight.setCanUseExistingDashPass(true);

                // Save return flight
                flightRepository.save(returnFlight);
                System.out.println("Saved Return Flight: " + returnFlight.getFlightNumber());
            }
        }
    }



    private String getRandomAirportCode(Random random) {
        return AIRPORTS.keySet().toArray(new String[0])[random.nextInt(AIRPORTS.size())];
    }

    private Airport createOrFetchAirport(String airportCode) {
        Airport airport = airportRepository.findByAirportCode(airportCode);
        if (airport == null) {
            airport = new Airport();
            airport.setAirportCode(airportCode);
            airport.setAirportName(AIRPORTS.get(airportCode));
            airportRepository.save(airport);
        }
        return airport;
    }

    private String generateUniqueFlightNumber(Set<String> existingFlightNumbers, Random random) {
        String flightNumber;
        do {
            flightNumber = "FL" + (random.nextInt(9000) + 1000);
        } while (existingFlightNumbers.contains(flightNumber));
        existingFlightNumbers.add(flightNumber); // Add to the set after ensuring uniqueness
        return flightNumber;
    }

    public void populateFlightsIfEmpty() {
        if (flightRepository.findAll().isEmpty()) {
            populateFlights(50);
        }
    }
}
