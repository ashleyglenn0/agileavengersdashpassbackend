package agileavengers.southwest_dashpass.services;

import agileavengers.southwest_dashpass.dtos.PaymentDetailsDTO;
import agileavengers.southwest_dashpass.exceptions.PaymentFailedException;
import agileavengers.southwest_dashpass.models.*;
import agileavengers.southwest_dashpass.repository.ReservationRepository;
import agileavengers.southwest_dashpass.utils.MockPaymentProcessor;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

@Service
public class BookingService {

    private final FlightService flightService;
    private final DashPassService dashPassService;
    private final ReservationService reservationService;
    private final DashPassReservationService dashPassReservationService;
    private final MockPaymentProcessor mockPaymentProcessor;
    private final ReservationRepository reservationRepository;

    @Autowired
    public BookingService(FlightService flightService, DashPassService dashPassService, ReservationService reservationService,
                          DashPassReservationService dashPassReservationService, MockPaymentProcessor mockPaymentProcessor,
                          ReservationRepository reservationRepository) {
        this.flightService = flightService;
        this.dashPassService = dashPassService;
        this.reservationService = reservationService;
        this.dashPassReservationService = dashPassReservationService;
        this.mockPaymentProcessor = mockPaymentProcessor;
        this.reservationRepository = reservationRepository;
    }


    @Async
    public CompletableFuture<Reservation> purchaseFlightAsync(Customer currentCustomer, Long outboundFlightId, Long returnFlightId,
                                                              String dashPassOption, String tripType, double totalCost,
                                                              PaymentDetailsDTO paymentDetails, String userSelectedStatus)
            throws InterruptedException {
        if (currentCustomer == null) {
            throw new IllegalArgumentException("Customer cannot be null");
        }

        CompletableFuture<PaymentStatus> paymentStatusFuture = mockPaymentProcessor.processPaymentAsync(paymentDetails, userSelectedStatus);

        return paymentStatusFuture.thenApply(paymentStatus -> {
            System.out.println("Payment Status for Customer ID " + currentCustomer.getId() + ": " + paymentStatus);
            if (paymentStatus == PaymentStatus.PAID) {
                System.out.println("Creating reservation for Customer ID " + currentCustomer.getId());
                Reservation reservation = new Reservation();
                reservation.setCustomer(currentCustomer);
                reservation.setTripType(TripType.valueOf(tripType));
                reservation.setPaymentStatus(PaymentStatus.PAID);
                reservation.setStatus(ReservationStatus.VALID);
                reservation.setValidated(false);

                // Populate reservation with flight details
                Flight outboundFlight = flightService.findFlightById(outboundFlightId);
                reservation.setFlights(new ArrayList<>());
                reservation.getFlights().add(outboundFlight);
                outboundFlight.setReservation(reservation);

                if (outboundFlight.getAvailableSeats() > 0) {
                    outboundFlight.setAvailableSeats(outboundFlight.getSeatsRemaining() - 1);
                    outboundFlight.setSeatsSold(outboundFlight.getSeatsSold() + 1);
                } else {
                    throw new IllegalStateException("No available seats for outbound flight");
                }

                double finalPrice = outboundFlight.getPrice();

                if ("ROUND_TRIP".equals(tripType) && returnFlightId != null) {
                    Flight returnFlight = flightService.findFlightById(returnFlightId);
                    returnFlight.setReservation(reservation);
                    reservation.getFlights().add(returnFlight);
                    finalPrice += returnFlight.getPrice();

                    if (returnFlight.getAvailableSeats() > 0) {
                        returnFlight.setAvailableSeats(outboundFlight.getSeatsRemaining() - 1);
                        returnFlight.setSeatsSold(returnFlight.getSeatsSold() + 1);
                    } else {
                        throw new IllegalStateException("No available seats for outbound flight");
                    }
                }
                System.out.println("Final price for reservation: $" + finalPrice);
                Random random = new Random();
                for (Flight flight : reservation.getFlights()) {
                    // Generate terminal and gate information
                    String terminal = generateTerminal(flight.getDepartureAirportCode());
                    String gate = "Gate " + (random.nextInt(20) + 1);

                    System.out.println("Assigned Terminal for Flight " + flight.getFlightID() + ": " + terminal);
                    System.out.println("Assigned Gate for Flight " + flight.getFlightID() + ": " + gate);

                    // Set terminal and gate for the flight
                    reservation.setFlightTerminal(flight.getFlightID(), terminal);
                    reservation.setFlightGate(flight.getFlightID(), gate);
                }

                boolean isNewPurchase = "new".equals(dashPassOption);
                reservation.setTotalPrice(isNewPurchase ? finalPrice + 50.0 : finalPrice);

                reservationService.save(reservation);
                System.out.println("Reservation saved successfully for Customer ID " + currentCustomer.getId());

                // Process DashPass based on selection
                if (isNewPurchase) {
                    DashPassReservation dashPassReservation = dashPassReservationService.createNewDashPassAndSaveNewDashPassReservation(
                            currentCustomer, reservation, outboundFlight // Link the DashPassReservation to the entire reservation, not just the flight
                    );
                    reservation.getDashPassReservations().add(dashPassReservation); // Link to reservation
                    System.out.println("New DashPass created and linked to reservation.");
                } else if ("existing".equals(dashPassOption)) {
                    DashPassReservation dashPassReservation = dashPassReservationService.createNewDashPassReservationAndAssignExistingDashPass(
                            currentCustomer, reservation, outboundFlight
                    );
                    reservation.getDashPassReservations().add(dashPassReservation); // Link to reservation
                    System.out.println("Existing DashPass linked to reservation.");
                }

                return reservation;
            } else {
                System.out.println("Payment failed for Customer ID " + currentCustomer.getId() + " with status " + paymentStatus);
                throw new PaymentFailedException("Payment failed.");
            }
        });
    }

    // Helper method to generate terminal based on airport
    private String generateTerminal(String airportCode) {
        if ("ATL".equals(airportCode)) {
            return "Terminal 6";
        }
        Random random = new Random();
        int terminalNumber = random.nextInt(5) + 1;
        char terminalLetter = (char) ('A' + random.nextInt(6));
        return "Terminal " + terminalNumber + terminalLetter;
    }

}