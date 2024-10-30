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
                                                              String dashPassOption, String tripType, double totalCost, PaymentDetailsDTO paymentDetails,
                                                              String userSelectedStatus) throws InterruptedException {
        if (currentCustomer == null) {
            throw new IllegalArgumentException("Customer cannot be null");
        }

        // Process payment asynchronously
        CompletableFuture<PaymentStatus> paymentStatusFuture = mockPaymentProcessor.processPaymentAsync(paymentDetails, userSelectedStatus);

        // Initialize final reservation details only after payment confirmation
        return paymentStatusFuture.thenApply(paymentStatus -> {
            // Only create and save the reservation if payment was successful
            if (paymentStatus == PaymentStatus.PAID) {
                Reservation reservation = new Reservation();
                reservation.setCustomer(currentCustomer);
                reservation.setTripType(TripType.valueOf(tripType));
                reservation.setPaymentStatus(PaymentStatus.PAID); // Set payment status to PAID
                reservation.setStatus(ReservationStatus.VALID);

                // Populate reservation with flight details
                Flight outboundFlight = flightService.findFlightById(outboundFlightId);
                reservation.setFlights(new ArrayList<>());
                reservation.getFlights().add(outboundFlight);
                outboundFlight.setReservation(reservation);

                double finalPrice = outboundFlight.getPrice();
                if ("ROUND_TRIP".equals(tripType) && returnFlightId != null) {
                    Flight returnFlight = flightService.findFlightById(returnFlightId);
                    returnFlight.setReservation(reservation);
                    reservation.getFlights().add(returnFlight);
                    finalPrice += returnFlight.getPrice();
                }

                // Adjust price if DashPass is selected
                boolean isNewPurchase = dashPassOption.equals("new");
                reservation.setTotalPrice(isNewPurchase ? finalPrice + 50.0 : finalPrice);

                // Save the reservation now that it is fully populated
                reservationService.save(reservation);

                // Process DashPass after successful payment
                DashPassReservation dashPassReservation;
                if (isNewPurchase) {
                    dashPassReservation = dashPassReservationService.createNewDashPassAndSaveNewDashPassReservation(currentCustomer, outboundFlight);
                } else if (dashPassOption.equals("existing")) {
                    dashPassReservation = dashPassReservationService.createNewDashPassReservationAndAssignExistingDashPass(currentCustomer, outboundFlight);
                }

                return reservation;

            } else {
                // Handle payment failure by creating an invalid reservation if needed
                throw new PaymentFailedException("Payment failed.");
            }
        });
    }



    @Transactional
    public Reservation createReservation(Customer customer, Long outboundFlightId, Long returnFlightId, String dashPassOption,
                                         String tripType, double totalPrice) {
        Flight outboundFlight = flightService.findFlightById(outboundFlightId);
        List<Flight> flights = new ArrayList<>();
        flights.add(outboundFlight);

        if (returnFlightId != null) {
            Flight returnFlight = flightService.findFlightById(returnFlightId);
            flights.add(returnFlight);
        }

        // Create and populate reservation
        Reservation reservation = new Reservation();
        reservation.setFlights(flights);
        reservation.setCustomer(customer);
        reservation.setTotalPrice(totalPrice);
        reservation.setDateBooked(LocalDate.now());

        // Save reservation
        return reservationRepository.save(reservation);
    }


}

//        // Save the reservation again to ensure flights and DashPasses are associated
//        reservationService.save(reservation);
//
//        return reservation;
