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
        List<String> airportCodes = new ArrayList<>(AIRPORTS.keySet());
        TripType[] tripTypes = {TripType.ONE_WAY, TripType.ROUND_TRIP};
        Random random = new Random();

        for (int i = 0; i < count; i++) {
            String tripId = UUID.randomUUID().toString();
            String departureCode = airportCodes.get(random.nextInt(airportCodes.size()));
            String arrivalCode;

            // Ensure arrivalCode is not the same as departureCode
            do {
                arrivalCode = airportCodes.get(random.nextInt(airportCodes.size()));
            } while (arrivalCode.equals(departureCode));

            TripType tripType = tripTypes[random.nextInt(tripTypes.length)];

            // Debugging: Log generated data
            System.out.println("Trip ID: " + tripId);
            System.out.println("Departure Code: " + departureCode);
            System.out.println("Arrival Code: " + arrivalCode);
            System.out.println("Trip Type: " + tripType);

            // Outbound flight
            String flightNumber = "FL" + (i + 1000); // Unique flight number
            LocalDate departureDate = LocalDate.now().plusDays(random.nextInt(30)); // Random departure within the next 30 days
            LocalDateTime departureTime = departureDate.atTime(random.nextInt(12) + 6, random.nextInt(60)); // Random time between 6:00 and 17:59
            LocalDateTime arrivalTime = departureTime.plusHours(2 + random.nextInt(3)); // Random duration between 2-4 hours
            int maxSeats = 100 + random.nextInt(101); // Random seat capacity between 100-200
            int seatsSold = random.nextInt(maxSeats); // Random seats sold (can reach maxSeats)
            int dashPassesAvailable = random.nextInt(16); // Random DashPass availability (0–15)
            double price = 100 + (random.nextInt(401) + random.nextDouble()); // Random price between $100–$300

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
            outboundFlight.setPrice(Math.round(price * 100.0) / 100.0); // Rounded to 2 decimals
            outboundFlight.setAvailableSeats(maxSeats);
            outboundFlight.setSeatsSold(seatsSold);
            outboundFlight.setNumberOfSeatsAvailable(maxSeats - seatsSold);
            outboundFlight.setMaxNumberOfDashPassesForFlight(15);
            outboundFlight.setNumberOfDashPassesAvailable(dashPassesAvailable);
            outboundFlight.setCanAddNewDashPass(dashPassesAvailable > 0); // Only allow new DashPass if available
            outboundFlight.setCanUseExistingDashPass(dashPassesAvailable > 0); // Only allow if DashPasses exist

            // Save outbound flight
            flightRepository.save(outboundFlight);
            System.out.println("Saved Outbound Flight: " + outboundFlight.getFlightNumber());

            // Return flight for round trips
            if (tripType == TripType.ROUND_TRIP) {
                LocalDate returnDate = departureDate.plusDays(3 + random.nextInt(4)); // Random return 3-6 days later
                LocalDateTime returnDepartureTime = returnDate.atTime(random.nextInt(12) + 6, random.nextInt(60)); // Random time between 6:00 and 17:59
                LocalDateTime returnArrivalTime = returnDepartureTime.plusHours(2 + random.nextInt(3)); // Random duration between 2-4 hours

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
                returnFlight.setPrice(Math.round(price * 100.0) / 100.0);
                returnFlight.setAvailableSeats(maxSeats);
                returnFlight.setSeatsSold(seatsSold);
                returnFlight.setNumberOfSeatsAvailable(maxSeats - seatsSold);
                returnFlight.setMaxNumberOfDashPassesForFlight(15);
                returnFlight.setNumberOfDashPassesAvailable(dashPassesAvailable);
                returnFlight.setCanAddNewDashPass(dashPassesAvailable > 0);
                returnFlight.setCanUseExistingDashPass(dashPassesAvailable > 0);

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
