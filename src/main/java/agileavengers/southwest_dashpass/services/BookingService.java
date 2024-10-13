package agileavengers.southwest_dashpass.services;

import agileavengers.southwest_dashpass.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class BookingService {

    private final FlightService flightService;
    private final DashPassService dashPassService;
    private final ReservationService reservationService;
    private final DashPassReservationService dashPassReservationService;

    @Autowired
    public BookingService(FlightService flightService, DashPassService dashPassService, ReservationService reservationService,
                          DashPassReservationService dashPassReservationService) {
        this.flightService = flightService;
        this.dashPassService = dashPassService;
        this.reservationService = reservationService;
        this.dashPassReservationService = dashPassReservationService;
    }

    public Reservation purchaseFlight(Customer currentCustomer, Long outboundFlightId, Long returnFlightId, String dashPassOption, String tripType, double totalCost) {
        // Fetch outbound flight details
        Flight outboundFlight = flightService.findFlightById(outboundFlightId);

        // Create a new reservation for the outbound flight
        Reservation reservation = new Reservation();
        reservation.setCustomer(currentCustomer); // Use the passed customer
        reservation.setTripType(TripType.valueOf(tripType));

        // Save the reservation first to persist it in the database
        reservationService.save(reservation);

        // Initialize the list of flights in the reservation if it doesn't exist
        if (reservation.getFlights() == null) {
            reservation.setFlights(new ArrayList<>());
        }

        // Now associate the flight with the saved reservation
        outboundFlight.setReservation(reservation);  // Associate flight with reservation
        reservation.getFlights().add(outboundFlight); // Add outbound flight to the reservation's flight list

        reservation.setFlightDepartureDate(outboundFlight.getDepartureDate());
        reservation.setAirportCode(outboundFlight.getDepartureAirportCode());

        // Initial total price based on the outbound flight
        double finalPrice = outboundFlight.getPrice();

        // Handle return flight for round-trip bookings
        if (tripType.equals("ROUND_TRIP") && returnFlightId != null) {
            Flight returnFlight = flightService.findFlightById(returnFlightId);
            returnFlight.setReservation(reservation);  // Associate return flight with the reservation
            reservation.getFlights().add(returnFlight); // Add return flight to the list

            // Add return flight price to the total price
            finalPrice += returnFlight.getPrice();
        }

        // Handle DashPass logic
        if (dashPassOption.equals("new")) {
            DashPass newDashPass = new DashPass();
            newDashPass.setCustomer(currentCustomer);
            dashPassService.save(newDashPass);  // Save new DashPass before associating

            DashPassReservation dashPassReservation = new DashPassReservation();
            dashPassReservation.setDashPass(newDashPass);
            dashPassReservation.setReservation(reservation);
            dashPassReservationService.save(dashPassReservation);  // Save DashPassReservation

            finalPrice += 50.0; // Add DashPass price (adjust as needed)
        } else if (dashPassOption.equals("existing")) {
            DashPass existingDashPass = dashPassService.findAvailableDashPassForCustomer(currentCustomer);
            DashPassReservation dashPassReservation = new DashPassReservation();
            dashPassReservation.setDashPass(existingDashPass);
            dashPassReservation.setReservation(reservation);
            dashPassReservationService.save(dashPassReservation);  // Save DashPassReservation
        }

        // Set the final price for the reservation
        reservation.setTotalPrice(finalPrice);

        // Save the reservation again to ensure flights and DashPasses are associated
        reservationService.save(reservation);

        return reservation;
    }

}

