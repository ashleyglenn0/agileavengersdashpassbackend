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
import java.util.*;

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
            String tripId = UUID.randomUUID().toString();
            String departureCode = getRandomAirportCode(random);
            String arrivalCode = getRandomAirportCode(random);

            while (departureCode.equals(arrivalCode)) {
                arrivalCode = getRandomAirportCode(random);
            }

            // Decide if this trip will be a round trip or one way
            boolean isRoundTrip = random.nextBoolean();
            TripType tripType = isRoundTrip ? TripType.ROUND_TRIP : TripType.ONE_WAY;

            // Generate outbound flights
            LocalDate outboundDate = LocalDate.now().plusDays(random.nextInt(30));
            int outboundFlightsOnSameDay = random.nextInt(4) + 2;

            for (int j = 0; j < outboundFlightsOnSameDay; j++) {
                String flightNumber = generateUniqueFlightNumber(random);
                LocalDateTime departureTime = outboundDate.atStartOfDay()
                        .plusHours(random.nextInt(12) + 6)
                        .plusMinutes(random.nextInt(60));
                int durationMinutes = 60 + random.nextInt(240);
                LocalDateTime arrivalTime = departureTime.plusMinutes(durationMinutes);

                // DashPass details
                int dashPassesAvailable = random.nextInt(16); // 0 to 15
                boolean dashPassAvailable = dashPassesAvailable > 0;

                // Create Outbound Flight
                Flight outboundFlight = new Flight();
                outboundFlight.setFlightNumber(flightNumber);
                outboundFlight.setDepartureAirportCode(departureCode);
                outboundFlight.setArrivalAirportCode(arrivalCode);
                outboundFlight.setDepartureDate(departureTime.toLocalDate());
                outboundFlight.setArrivalDate(arrivalTime.toLocalDate());
                outboundFlight.setDepartureTime(departureTime.toLocalTime());
                outboundFlight.setArrivalTime(arrivalTime.toLocalTime());
                outboundFlight.setDirection("OUTBOUND");
                outboundFlight.setTripId(tripId);
                outboundFlight.setTripType(tripType);
                outboundFlight.setPrice(100 + (random.nextDouble() * (500 - 100)));
                outboundFlight.setNumberOfSeatsAvailable(60);
                outboundFlight.setSeatsSold(60 - (20 + random.nextInt(31)));
                outboundFlight.setMaxNumberOfDashPassesForFlight(15);
                outboundFlight.setNumberOfDashPassesAvailable(dashPassesAvailable);
                outboundFlight.setCanAddNewDashPass(dashPassAvailable);
                outboundFlight.setCanUseExistingDashPass(dashPassAvailable);

                flightRepository.save(outboundFlight);

                // Generate return flights if this is a round trip
                if (isRoundTrip) {
                    LocalDate returnDate = outboundDate.plusDays(1 + random.nextInt(7));
                    int returnFlightsOnSameDay = random.nextInt(4) + 2;

                    for (int k = 0; k < returnFlightsOnSameDay; k++) {
                        // Generate return departure and arrival times based on returnDate
                        LocalDateTime returnDepartureTime = returnDate.atStartOfDay()
                                .plusHours(random.nextInt(12) + 6)
                                .plusMinutes(random.nextInt(60));
                        LocalDateTime returnArrivalTime = returnDepartureTime.plusMinutes(60 + random.nextInt(240));

                        // Randomize DashPass availability for return flights
                        dashPassesAvailable = random.nextInt(16); // 0 to 15
                        dashPassAvailable = dashPassesAvailable > 0;

                        // Create Return Flight
                        Flight returnFlight = new Flight();
                        returnFlight.setFlightNumber(generateUniqueFlightNumber(random));
                        returnFlight.setDepartureAirportCode(arrivalCode);
                        returnFlight.setArrivalAirportCode(departureCode);
                        returnFlight.setDepartureDate(returnDepartureTime.toLocalDate());
                        returnFlight.setArrivalDate(returnArrivalTime.toLocalDate());
                        returnFlight.setDepartureTime(returnDepartureTime.toLocalTime());
                        returnFlight.setArrivalTime(returnArrivalTime.toLocalTime());
                        returnFlight.setDirection("RETURN");
                        returnFlight.setTripId(tripId);
                        returnFlight.setTripType(TripType.ROUND_TRIP);
                        returnFlight.setReturnDate(returnDate);
                        returnFlight.setPrice(100 + (random.nextDouble() * (500 - 100)));
                        returnFlight.setNumberOfSeatsAvailable(60);
                        returnFlight.setSeatsSold(60 - (20 + random.nextInt(31)));
                        returnFlight.setMaxNumberOfDashPassesForFlight(15);
                        returnFlight.setNumberOfDashPassesAvailable(dashPassesAvailable);
                        returnFlight.setCanAddNewDashPass(dashPassAvailable);
                        returnFlight.setCanUseExistingDashPass(dashPassAvailable);

                        flightRepository.save(returnFlight);
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
}