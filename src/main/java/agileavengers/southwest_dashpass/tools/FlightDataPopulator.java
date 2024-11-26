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
import java.time.LocalTime;
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
        AIRPORTS.put("BOS", "Logan International Airport");
        AIRPORTS.put("CLT", "Charlotte Douglas International Airport");
        AIRPORTS.put("DEN", "Denver International Airport");
        AIRPORTS.put("HNL", "Daniel K. Inouye International Airport");
        AIRPORTS.put("PHX", "Phoenix Sky Harbor International Airport");
        AIRPORTS.put("SLC", "Salt Lake City International Airport");
        AIRPORTS.put("BNA", "Nashville International Airport");
        AIRPORTS.put("TPA", "Tampa International Airport");
        AIRPORTS.put("PDX", "Portland International Airport");
        AIRPORTS.put("DAL", "Dallas Love Field");
        AIRPORTS.put("DCA", "Washington D.C. National Airport");
        AIRPORTS.put("RDU", "Raleigh-Durham International Airport");
        AIRPORTS.put("MDW", "Chicago Midway International Airport");
        AIRPORTS.put("CLE", "Cleveland Hopkins International Airport");
        AIRPORTS.put("MSP", "Minneapolis-Saint Paul International Airport");
        AIRPORTS.put("ANC", "Ted Stevens Anchorage International Airport");
        AIRPORTS.put("HOU", "William P. Hobby Airport");
        AIRPORTS.put("ONT", "Ontario International Airport");
        AIRPORTS.put("PIT", "Pittsburgh International Airport");
    }

    public void populateFlights(int count) {
        Random random = new Random();
        List<String> airportCodes = new ArrayList<>(AIRPORTS.keySet());
        TripType[] tripTypes = {TripType.ONE_WAY, TripType.ROUND_TRIP};

        for (int i = 0; i < count; i++) {
            String tripId = UUID.randomUUID().toString();
            String departureCode = airportCodes.get(random.nextInt(airportCodes.size()));
            String arrivalCode;

            // Ensure departure and arrival codes are different
            do {
                arrivalCode = airportCodes.get(random.nextInt(airportCodes.size()));
            } while (departureCode.equals(arrivalCode));

            boolean isRoundTrip = random.nextBoolean();
            TripType tripType = isRoundTrip ? TripType.ROUND_TRIP : TripType.ONE_WAY;

            // Generate a random date within the next 30 days for outbound flights
            LocalDate outboundDate = LocalDate.now().plusDays(random.nextInt(30));
            int outboundFlightsOnSameDay = random.nextInt(4) + 2; // Generate 2-5 outbound flights for the same day

            for (int j = 0; j < outboundFlightsOnSameDay; j++) {
                String flightNumber = "FL" + (1000 + random.nextInt(9000)); // Generate unique flight number
                LocalDateTime departureTime = outboundDate.atTime(random.nextInt(12) + 6, random.nextInt(60)); // Random time between 6:00 AM and 6:59 PM
                int durationMinutes = 60 + random.nextInt(240); // Duration between 1 to 4 hours
                LocalDateTime arrivalTime = departureTime.plusMinutes(durationMinutes);

                int maxSeats = 50 + random.nextInt(151); // Random seats between 50 and 200
                int seatsSold = random.nextInt(maxSeats); // Random seats sold
                int dashPassesAvailable = random.nextInt(16); // Random DashPass availability (0–15)

                // Create and save the outbound flight
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
                outboundFlight.setPrice(100 + (random.nextDouble() * 400)); // Price between $100 and $500
                outboundFlight.setAvailableSeats(maxSeats);
                outboundFlight.setSeatsSold(seatsSold);
                outboundFlight.setNumberOfSeatsAvailable(maxSeats - seatsSold);
                outboundFlight.setMaxNumberOfDashPassesForFlight(15);
                outboundFlight.setNumberOfDashPassesAvailable(dashPassesAvailable);
                outboundFlight.setCanAddNewDashPass(dashPassesAvailable > 0);
                outboundFlight.setCanUseExistingDashPass(dashPassesAvailable > 0);

                flightRepository.save(outboundFlight);

                // Handle return flights if round trip
                if (isRoundTrip) {
                    LocalDate returnDate = outboundDate.plusDays(1 + random.nextInt(7)); // Return 1–7 days after outbound
                    int returnFlightsOnSameDay = random.nextInt(4) + 2; // Generate 2-5 return flights on the same day

                    for (int k = 0; k < returnFlightsOnSameDay; k++) {
                        String returnFlightNumber = "FL" + (1000 + random.nextInt(9000)) + "R"; // Unique return flight number
                        LocalDateTime returnDepartureTime = returnDate.atTime(random.nextInt(12) + 6, random.nextInt(60)); // Random time between 6:00 AM and 6:59 PM
                        int returnDurationMinutes = 60 + random.nextInt(240); // Duration between 1 to 4 hours
                        LocalDateTime returnArrivalTime = returnDepartureTime.plusMinutes(returnDurationMinutes);

                        maxSeats = 50 + random.nextInt(151); // New random seats for return flight
                        seatsSold = random.nextInt(maxSeats); // Random seats sold
                        dashPassesAvailable = random.nextInt(16); // DashPass availability (0–15)

                        // Create and save the return flight
                        Flight returnFlight = new Flight();
                        returnFlight.setFlightNumber(returnFlightNumber);
                        returnFlight.setDepartureAirportCode(arrivalCode);
                        returnFlight.setArrivalAirportCode(departureCode);
                        returnFlight.setDepartureDate(returnDepartureTime.toLocalDate());
                        returnFlight.setArrivalDate(returnArrivalTime.toLocalDate());
                        returnFlight.setDepartureTime(returnDepartureTime.toLocalTime());
                        returnFlight.setArrivalTime(returnArrivalTime.toLocalTime());
                        returnFlight.setDirection("RETURN");
                        returnFlight.setTripId(tripId);
                        returnFlight.setTripType(TripType.ROUND_TRIP);
                        returnFlight.setPrice(100 + (random.nextDouble() * 400)); // Price between $100 and $500
                        returnFlight.setAvailableSeats(maxSeats);
                        returnFlight.setSeatsSold(seatsSold);
                        returnFlight.setNumberOfSeatsAvailable(maxSeats - seatsSold);
                        returnFlight.setMaxNumberOfDashPassesForFlight(15);
                        returnFlight.setNumberOfDashPassesAvailable(dashPassesAvailable);
                        returnFlight.setCanAddNewDashPass(dashPassesAvailable > 0);
                        returnFlight.setCanUseExistingDashPass(dashPassesAvailable > 0);

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
            populateFlights(60);
        }
    }
}
