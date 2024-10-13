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

        // Initialize the list of flights in the reservation if it doesn't exist
        if (reservation.getFlights() == null) {
            reservation.setFlights(new ArrayList<>());
        }

        // Associate the flight with the reservation
        outboundFlight.setReservation(reservation);  // Add this line to associate the flight with the reservation
        reservation.getFlights().add(outboundFlight); // Add outbound flight to the list

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
            dashPassService.save(newDashPass);

            DashPassReservation dashPassReservation = new DashPassReservation();
            dashPassReservation.setDashPass(newDashPass);
            dashPassReservation.setReservation(reservation);
            dashPassReservationService.save(dashPassReservation);

            finalPrice += 50.0; // Add DashPass price (adjust as needed)
        } else if (dashPassOption.equals("existing")) {
            DashPass existingDashPass = dashPassService.findAvailableDashPassForCustomer(currentCustomer);
            DashPassReservation dashPassReservation = new DashPassReservation();
            dashPassReservation.setDashPass(existingDashPass);
            dashPassReservation.setReservation(reservation);
            dashPassReservationService.save(dashPassReservation);
        }

        // Set the final price for the reservation
        reservation.setTotalPrice(finalPrice);

        // Save the reservation, this will cascade and save the associated flights as well
        reservationService.save(reservation);

        return reservation;
    }

}

