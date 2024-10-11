package agileavengers.southwest_dashpass.tools;

import agileavengers.southwest_dashpass.models.Airport;
import agileavengers.southwest_dashpass.models.Flight;
import agileavengers.southwest_dashpass.models.TripType;
import agileavengers.southwest_dashpass.repository.AirportRepository;
import agileavengers.southwest_dashpass.repository.FlightRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Component
public class FlightDataPopulator {
    NumberFormat currency = NumberFormat.getCurrencyInstance();

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

            // Pick a random future date for the outbound flight
            LocalDate randomDate = LocalDate.now().plusDays(random.nextInt(30));

            // Generate multiple flights for the same day with varying departure times
            int flightsOnSameDay = random.nextInt(4) + 2; // 2-5 flights on the same day

            for (int j = 0; j < flightsOnSameDay; j++) {
                // Generate a random and unique flight number
                String flightNumber = generateUniqueFlightNumber(random);

                // Generate a random departure time for the selected date
                LocalDateTime departureTime = randomDate.atStartOfDay()
                        .plusHours(random.nextInt(12) + 6) // Random hour between 6 AM and 6 PM
                        .plusMinutes(random.nextInt(60));  // Random minute within the hour

                // Random flight duration between 1 and 5 hours
                int durationMinutes = 60 + random.nextInt(240);
                LocalDateTime arrivalTime = departureTime.plusMinutes(durationMinutes);

                // Generate random price between $100 and $500
                double price = 100 + (random.nextDouble() * (500 - 100));

                // Create a one-way flight (outbound)
                Flight outboundFlight = new Flight();
                outboundFlight.setFlightNumber(flightNumber);
                outboundFlight.setDepartureAirportCode(departureCode);
                outboundFlight.setArrivalAirportCode(arrivalCode);

                // Set the date and time
                outboundFlight.setDepartureDate(departureTime.toLocalDate());
                outboundFlight.setArrivalDate(arrivalTime.toLocalDate());
                outboundFlight.setDepartureTime(departureTime.toLocalTime());
                outboundFlight.setArrivalTime(arrivalTime.toLocalTime());

                outboundFlight.setNumberOfSeatsAvailable(50);
                outboundFlight.setNumberOfDashPassesAvailable(20);
                outboundFlight.setMaxNumberOfDashPassesForFlight(20);
                outboundFlight.setSeatsSold(0);
                outboundFlight.setTripType(TripType.ONE_WAY); // This is the outbound flight (one-way)
                outboundFlight.setPrice(price);

                flightRepository.save(outboundFlight);
                System.out.println("Added ONE-WAY flight on " + randomDate + " from " + departureCode + " to " + arrivalCode + " at " + departureTime.toLocalTime());

                // Check if we want to generate round-trip flights
                if (random.nextBoolean()) {
                    // Pick a random return date (1-7 days after outbound flight)
                    LocalDate returnDate = randomDate.plusDays(1 + random.nextInt(7));

                    // Generate multiple return flights for the return date
                    int returnFlightsOnSameDay = random.nextInt(4) + 2; // 2-5 return flights on the same day

                    for (int k = 0; k < returnFlightsOnSameDay; k++) {
                        // Generate a random departure time for the return flight
                        LocalDateTime returnDepartureTime = returnDate.atStartOfDay()
                                .plusHours(random.nextInt(12) + 6) // Random hour between 6 AM and 6 PM
                                .plusMinutes(random.nextInt(60));  // Random minute within the hour

                        // Random return flight duration
                        int returnDurationMinutes = 60 + random.nextInt(240);
                        LocalDateTime returnArrivalTime = returnDepartureTime.plusMinutes(returnDurationMinutes);

                        // Create a return flight (round-trip)
                        Flight returnFlight = new Flight();
                        returnFlight.setFlightNumber(generateUniqueFlightNumber(random));
                        returnFlight.setDepartureAirportCode(arrivalCode); // Arrival airport is now the departure
                        returnFlight.setArrivalAirportCode(departureCode); // Departure airport is now the arrival

                        returnFlight.setDepartureDate(returnDepartureTime.toLocalDate());
                        returnFlight.setArrivalDate(returnArrivalTime.toLocalDate());
                        returnFlight.setDepartureTime(returnDepartureTime.toLocalTime());
                        returnFlight.setArrivalTime(returnArrivalTime.toLocalTime());

                        returnFlight.setNumberOfSeatsAvailable(50);
                        returnFlight.setSeatsSold(0);
                        returnFlight.setNumberOfDashPassesAvailable(20);
                        returnFlight.setMaxNumberOfDashPassesForFlight(20);
                        returnFlight.setTripType(TripType.ROUND_TRIP); // This is the return flight
                        returnFlight.setPrice(price); // Same price as outbound or randomize

                        flightRepository.save(returnFlight);
                        System.out.println("Added ROUND-TRIP flight on " + returnDate + " from " + arrivalCode + " to " + departureCode + " at " + returnDepartureTime.toLocalTime());

                        // Now that both flights have been saved, link the outbound and return flights
                        if (!outboundFlight.equals(returnFlight)) {
                            outboundFlight.setReturnFlightID(returnFlight);
                            returnFlight.setReturnFlightID(outboundFlight);

                            // Update the outbound and return flights
                            flightRepository.save(outboundFlight);
                            flightRepository.save(returnFlight);
                        } else {
                            System.out.println("Avoiding self-referencing flight as return flight.");
                        }
                    }
                }
            }
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
            populateFlights(200); // Only populate if no flights exist
        } else {
            System.out.println("Flights already populated. Skipping flight population.");
        }
    }

    private String generateUniqueFlightNumber(Random random) {
        String flightNumber;
        boolean isUnique = false;

        // Loop until a unique flight number is found
        do {
            // Generate a random flight number
            flightNumber = "FL" + (random.nextInt(9000) + 1000); // Generates numbers between FL1000 to FL9999

            // Check if flight number already exists in the database
            isUnique = !flightRepository.existsByFlightNumber(flightNumber);

        } while (!isUnique);

        return flightNumber;
    }



    private String getRandomAirlineCode(Random random) {
        // List of airline codes (You can customize this)
        String[] airlineCodes = {"LAX", "JFK", "ORD", "ATL", "SFO", "MIA", "DFW", "SEA", "STL", "IAD", "MSY", "LAS", "DTW", "AUS", "IAH", "MEM", "FLL" ,"MCO", "PHL", "SAN"};
        return airlineCodes[random.nextInt(airlineCodes.length)];
    }
}