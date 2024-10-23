package agileavengers.southwest_dashpass.services;

import agileavengers.southwest_dashpass.dtos.PaymentDetailsDTO;
import agileavengers.southwest_dashpass.exceptions.PaymentFailedException;
import agileavengers.southwest_dashpass.models.*;
import agileavengers.southwest_dashpass.utils.MockPaymentProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

@Service
public class BookingService {

    private final FlightService flightService;
    private final DashPassService dashPassService;
    private final ReservationService reservationService;
    private final DashPassReservationService dashPassReservationService;
    private final MockPaymentProcessor mockPaymentProcessor;

    @Autowired
    public BookingService(FlightService flightService, DashPassService dashPassService, ReservationService reservationService,
                          DashPassReservationService dashPassReservationService, MockPaymentProcessor mockPaymentProcessor) {
        this.flightService = flightService;
        this.dashPassService = dashPassService;
        this.reservationService = reservationService;
        this.dashPassReservationService = dashPassReservationService;
        this.mockPaymentProcessor = mockPaymentProcessor;
    }

    @Async
    public CompletableFuture<Reservation> purchaseFlightAsync(Customer currentCustomer, Long outboundFlightId, Long returnFlightId,
                                                              String dashPassOption, String tripType, double totalCost, PaymentDetailsDTO paymentDetails,
                                                              String userSelectedStatus) throws InterruptedException {
        if (currentCustomer == null) {
            throw new IllegalArgumentException("Customer cannot be null");
        }

        // Fetch outbound flight details
        Flight outboundFlight = flightService.findFlightById(outboundFlightId);

        // Process payment status asynchronously using the mock payment processor
        CompletableFuture<PaymentStatus> paymentStatusFuture = mockPaymentProcessor.processPaymentAsync(paymentDetails, userSelectedStatus);

        // Create a new reservation
        Reservation reservation = new Reservation();
        reservation.setCustomer(currentCustomer);
        reservation.setTripType(TripType.valueOf(tripType));
        reservation.setPaymentStatus(PaymentStatus.PENDING); // Set initial payment status to PENDING

        // Save the reservation immediately to persist it in the database
        reservationService.save(reservation);

        // Initialize total price with the outbound flight price
        double finalPrice = outboundFlight.getPrice();
        reservation.setFlightDepartureDate(outboundFlight.getDepartureDate());
        reservation.setAirportCode(outboundFlight.getDepartureAirportCode());

        // Initialize the list of flights in the reservation if it doesn't exist
        if (reservation.getFlights() == null) {
            reservation.setFlights(new ArrayList<>());
        }

        // Associate the outbound flight with the saved reservation
        outboundFlight.setReservation(reservation);
        reservation.getFlights().add(outboundFlight);

        // Handle return flight for round-trip bookings
        if (tripType.equals("ROUND_TRIP") && returnFlightId != null) {
            Flight returnFlight = flightService.findFlightById(returnFlightId);
            returnFlight.setReservation(reservation);
            reservation.getFlights().add(returnFlight);
            finalPrice += returnFlight.getPrice();  // Add return flight price to the total price
        }

        boolean isNewPurchase = dashPassOption.equals("new");
        final DashPassReservation[] dashPassReservation = new DashPassReservation[1];
        final DashPass[] dashPass = new DashPass[1];

        // Handle DashPass logic
        if (isNewPurchase) {
            // Only create DashPass on successful payment
            dashPass[0] = null; // Defer creation until payment success
            finalPrice += 50.0; // Add DashPass price (adjust as needed)
        } else if (dashPassOption.equals("existing")) {
            // Only assign existing DashPass on success
            dashPass[0] = null; // Defer assignment until payment success
        }

        // Set the final price for the reservation
        reservation.setTotalPrice(finalPrice);

        // Handle Payment Status
        return paymentStatusFuture.thenApply(paymentStatus -> {
            if (paymentStatus == PaymentStatus.PAID) {
                // Successful payment - update DashPass numbers
                reservation.setStatus(ReservationStatus.VALID);
                reservation.setPaymentStatus(PaymentStatus.PAID);
                reservationService.save(reservation); // Save reservation

                if (isNewPurchase) {
                    // Now create and assign the new DashPass since payment was successful
                    dashPassReservation[0] = dashPassReservationService.createNewDashPassAndSaveNewDashPassReservation(currentCustomer, outboundFlight);
                    dashPass[0] = dashPassReservation[0].getDashPass();
                } else if (dashPassOption.equals("existing")) {
                    // Now assign an existing DashPass since payment was successful
                    dashPassReservation[0] = dashPassReservationService.createNewDashPassReservationAndAssignExistingDashPass(currentCustomer, outboundFlight);
                    dashPass[0] = dashPassReservation[0].getDashPass();
                }

                // Handle DashPass updates on success
                dashPassReservationService.handleDashPassOnSuccessfulPayment(currentCustomer, dashPass[0], isNewPurchase, outboundFlight);

                return reservation;

            } else if (paymentStatus == PaymentStatus.PENDING) {
                // Simulate the pending status
                try {
                    Thread.sleep(5000); // Simulate a delay (5 seconds)
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                PaymentStatus finalStatus;
                try {
                    finalStatus = mockPaymentProcessor.simulatePendingStatus();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                reservation.setPaymentStatus(finalStatus);

                if (finalStatus == PaymentStatus.PAID) {
                    reservation.setStatus(ReservationStatus.VALID);
                    reservationService.save(reservation); // Save reservation after payment success

                    if (isNewPurchase) {
                        dashPassReservation[0]= dashPassReservationService.createNewDashPassAndSaveNewDashPassReservation(currentCustomer, outboundFlight);
                        dashPass[0] = dashPassReservation[0].getDashPass();
                    } else if (dashPassOption.equals("existing")) {
                        dashPassReservation[0] = dashPassReservationService.createNewDashPassReservationAndAssignExistingDashPass(currentCustomer, outboundFlight);
                        dashPass[0] = dashPassReservation[0].getDashPass();
                    }

                    dashPassReservationService.handleDashPassOnSuccessfulPayment(currentCustomer, dashPass[0], isNewPurchase, outboundFlight);

                    return reservation;
                } else {
                    reservation.setStatus(ReservationStatus.INVALID); // Mark as invalid
                    reservationService.save(reservation); // Save reservation as invalid

                    // No need to rollback anything since nothing was updated
                    throw new PaymentFailedException("Payment failed after pending processing.");
                }
            } else {
                // For failed payments
                reservation.setStatus(ReservationStatus.INVALID); // Mark as invalid
                reservationService.save(reservation); // Save reservation as failed

                // No rollback needed
                throw new PaymentFailedException("Payment failed.");
            }
        });
    }

}

//        // Save the reservation again to ensure flights and DashPasses are associated
//        reservationService.save(reservation);
//
//        return reservation;
