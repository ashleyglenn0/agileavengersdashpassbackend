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
        Random random = new Random();

        // Fetch existing flight numbers once and store them in a Set for fast lookup
        Set<String> existingFlightNumbers = new HashSet<>(flightRepository.findAllFlightNumbers());

        for (int i = 0; i < count; i++) {
            String tripId = UUID.randomUUID().toString();
            String departureCode = getRandomAirportCode(random);
            String arrivalCode = getRandomAirportCode(random);

            while (departureCode.equals(arrivalCode)) {
                arrivalCode = getRandomAirportCode(random);
            }

            // Fetch or create departure and arrival airports
            Airport departureAirport = createOrFetchAirport(departureCode);
            Airport arrivalAirport = createOrFetchAirport(arrivalCode);

            boolean isRoundTrip = random.nextBoolean();
            TripType tripType = isRoundTrip ? TripType.ROUND_TRIP : TripType.ONE_WAY;

            LocalDate outboundDate = LocalDate.now().plusDays(random.nextInt(30));
            int outboundFlightsOnSameDay = random.nextInt(4) + 2;

            for (int j = 0; j < outboundFlightsOnSameDay; j++) {
                String flightNumber = generateUniqueFlightNumber(existingFlightNumbers, random);
                LocalDateTime departureTime = outboundDate.atStartOfDay()
                        .plusHours(random.nextInt(12) + 6)
                        .plusMinutes(random.nextInt(60));
                int durationMinutes = 60 + random.nextInt(240);
                LocalDateTime arrivalTime = departureTime.plusMinutes(durationMinutes);

                int maxSeats = 50 + random.nextInt(151); // Random seats between 50 and 200
                int seatsSold = random.nextInt(maxSeats);
                int dashPassesAvailable = random.nextInt(16); // DashPass availability between 0 and 15
                boolean dashPassAvailable = dashPassesAvailable > 0;

                // Create Outbound Flight
                Flight outboundFlight = new Flight();
                outboundFlight.setFlightNumber(flightNumber);
                outboundFlight.setDepartureAirportCode(departureAirport.getAirportCode());
                outboundFlight.setArrivalAirportCode(arrivalAirport.getAirportCode());
                outboundFlight.setDepartureDate(departureTime.toLocalDate());
                outboundFlight.setArrivalDate(arrivalTime.toLocalDate());
                outboundFlight.setDepartureTime(departureTime.toLocalTime());
                outboundFlight.setArrivalTime(arrivalTime.toLocalTime());
                outboundFlight.setDirection("OUTBOUND");
                outboundFlight.setTripId(tripId);
                outboundFlight.setTripType(tripType);
                outboundFlight.setPrice(100 + (random.nextDouble() * (500 - 100)));
                outboundFlight.setAvailableSeats(maxSeats);
                outboundFlight.setSeatsSold(seatsSold);
                outboundFlight.setNumberOfSeatsAvailable(maxSeats - seatsSold);
                outboundFlight.setMaxNumberOfDashPassesForFlight(15);
                outboundFlight.setNumberOfDashPassesAvailable(dashPassesAvailable);
                outboundFlight.setCanAddNewDashPass(dashPassAvailable);
                outboundFlight.setCanUseExistingDashPass(dashPassAvailable);

                flightRepository.save(outboundFlight);

                if (isRoundTrip) {
                    LocalDate returnDate = outboundDate.plusDays(1 + random.nextInt(7));
                    int returnFlightsOnSameDay = random.nextInt(4) + 2;

                    for (int k = 0; k < returnFlightsOnSameDay; k++) {
                        LocalDateTime returnDepartureTime = returnDate.atStartOfDay()
                                .plusHours(random.nextInt(12) + 6)
                                .plusMinutes(random.nextInt(60));
                        LocalDateTime returnArrivalTime = returnDepartureTime.plusMinutes(60 + random.nextInt(240));

                        maxSeats = 50 + random.nextInt(151); // New random seats for return flight
                        seatsSold = random.nextInt(maxSeats);
                        dashPassesAvailable = random.nextInt(16);
                        dashPassAvailable = dashPassesAvailable > 0;

                        // Create Return Flight
                        Flight returnFlight = new Flight();
                        returnFlight.setFlightNumber(generateUniqueFlightNumber(existingFlightNumbers, random));
                        returnFlight.setDepartureAirportCode(arrivalAirport.getAirportCode());
                        returnFlight.setArrivalAirportCode(departureAirport.getAirportCode());
                        returnFlight.setDepartureDate(returnDepartureTime.toLocalDate());
                        returnFlight.setArrivalDate(returnArrivalTime.toLocalDate());
                        returnFlight.setDepartureTime(returnDepartureTime.toLocalTime());
                        returnFlight.setArrivalTime(returnArrivalTime.toLocalTime());
                        returnFlight.setDirection("RETURN");
                        returnFlight.setTripId(tripId);
                        returnFlight.setTripType(TripType.ROUND_TRIP);
                        returnFlight.setReturnDate(returnDate);
                        returnFlight.setPrice(100 + (random.nextDouble() * (500 - 100)));
                        returnFlight.setAvailableSeats(maxSeats);
                        returnFlight.setSeatsSold(seatsSold);
                        returnFlight.setNumberOfSeatsAvailable(maxSeats - seatsSold);
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
