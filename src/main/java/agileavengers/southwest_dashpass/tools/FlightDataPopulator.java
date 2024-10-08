package agileavengers.southwest_dashpass.tools;

import agileavengers.southwest_dashpass.models.Airport;
import agileavengers.southwest_dashpass.models.Flight;
import agileavengers.southwest_dashpass.repository.AirportRepository;
import agileavengers.southwest_dashpass.repository.FlightRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

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
        Random random = new Random();

        for (int i = 0; i < count; i++) {
            // Randomly select departure and arrival airports
            String departureCode = getRandomAirportCode(random);
            String arrivalCode = getRandomAirportCode(random);

            // Ensure departure and arrival airports are different
            while (departureCode.equals(arrivalCode)) {
                arrivalCode = getRandomAirportCode(random);
            }

            // Create or fetch airport entities
            Airport departureAirport = createOrFetchAirport(departureCode);
            Airport arrivalAirport = createOrFetchAirport(arrivalCode);

            // Generate random future departure time
            LocalDateTime departureTime = LocalDateTime.now()
                    .plusDays(random.nextInt(30))
                    .plusHours(random.nextInt(24))
                    .plusMinutes(random.nextInt(60));

            // Random flight duration between 1 and 5 hours
            int durationMinutes = 60 + random.nextInt(240);
            LocalDateTime arrivalTime = departureTime.plusMinutes(durationMinutes);

            // Create a new flight and save to the database
            Flight flight = new Flight();
            flight.setFlightNumber(generateRandomFlightNumber(random));
            flight.setFlightDepartureAirport(departureAirport);
            flight.setFlightArrivalAirport(arrivalAirport);
            flight.setFlightDepartureTime(departureTime);
            flight.setFlightArrivalTime(arrivalTime);
            flight.setNumberOfSeatsAvailable(50); // Example value
            flight.setNumberOfSeatsSold(0); // Example value

            flightRepository.save(flight);
            System.out.println("Added Flight from " + departureCode + " to " + arrivalCode);
        }
    }

    // Helper method to generate a random airport code
    private String getRandomAirportCode(Random random) {
        return AIRPORTS.keySet().toArray(new String[0])[random.nextInt(AIRPORTS.size())];
    }

    // Fetch or create an airport by its code
    private Airport createOrFetchAirport(String airportCode) {
        // Try to find the airport in the database
        Airport airport = airportRepository.findByAirportCode(airportCode);
        if (airport == null) {
            airport = new Airport();
            airport.setAirportCode(airportCode);
            airport.setAirportName(AIRPORTS.get(airportCode)); // Use name from the map
            airportRepository.save(airport); // Save the airport to the database
        }
        return airport;
    }

    // Generate a random flight number (e.g., AB1234)
    private String generateRandomFlightNumber(Random random) {
        return "" + (char) ('A' + random.nextInt(26)) + (char) ('A' + random.nextInt(26)) +
                (1000 + random.nextInt(9000)); // Example: AB1234
    }

    public void populateFlightsIfEmpty() {
        // Check if the flights table is empty
        List<Flight> existingFlights = flightRepository.findAll();
        if (existingFlights.isEmpty()) {
            System.out.println("No flights found. Populating flight data...");
            populateFlights(100); // Only populate if no flights exist
        } else {
            System.out.println("Flights already populated. Skipping flight population.");
        }
    }
}
