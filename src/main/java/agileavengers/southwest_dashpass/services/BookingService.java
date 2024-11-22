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
    private final EmployeeService employeeService;
    private final AuditTrailService auditTrailService;
    private final SalesRecordService salesRecordService;

    @Autowired
    public BookingService(FlightService flightService, DashPassService dashPassService, ReservationService reservationService,
                          DashPassReservationService dashPassReservationService, MockPaymentProcessor mockPaymentProcessor,
                          ReservationRepository reservationRepository, SalesRecordService salesRecordService,
                          AuditTrailService auditTrailService, EmployeeService employeeService) {
        this.flightService = flightService;
        this.dashPassService = dashPassService;
        this.reservationService = reservationService;
        this.dashPassReservationService = dashPassReservationService;
        this.mockPaymentProcessor = mockPaymentProcessor;
        this.reservationRepository = reservationRepository;
        this.salesRecordService = salesRecordService;
        this.employeeService = employeeService;
        this.auditTrailService = auditTrailService;
    }


    @Async
    public CompletableFuture<Reservation> purchaseFlightAsync(Customer currentCustomer, Long outboundFlightId, Long returnFlightId,
                                                              String dashPassOption, String tripType, double totalCost,
                                                              PaymentDetailsDTO paymentDetails, String userSelectedStatus,
                                                              Long employeeId, int bagQuantity)
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

                if ("ROUND_TRIP".equals(tripType) && returnFlightId != null) {
                    Flight returnFlight = flightService.findFlightById(returnFlightId);
                    returnFlight.setReservation(reservation);
                    reservation.getFlights().add(returnFlight);

                    if (returnFlight.getAvailableSeats() > 0) {
                        returnFlight.setAvailableSeats(returnFlight.getSeatsRemaining() - 1);
                        returnFlight.setSeatsSold(returnFlight.getSeatsSold() + 1);
                    } else {
                        throw new IllegalStateException("No available seats for return flight");
                    }
                }

                // Assign terminal and gate information
                Random random = new Random();
                for (Flight flight : reservation.getFlights()) {
                    String terminal = generateTerminal(flight.getDepartureAirportCode());
                    String gate = "Gate " + (random.nextInt(20) + 1);

                    reservation.setFlightTerminal(flight.getFlightID(), terminal);
                    reservation.setFlightGate(flight.getFlightID(), gate);
                }

                // Add bags to reservation, customer, and flights
                List<Bag> bags = new ArrayList<>();
                for (int i = 0; i < bagQuantity; i++) {
                    Bag bag = new Bag();
                    bag.setStatus(BagStatus.PRE_CHECK_IN); // Default status
                    bag.setReservation(reservation);
                    bag.setCustomer(currentCustomer);
                    bag.setFlight(outboundFlight); // Explicitly set the flight
                    bags.add(bag);
                }
                currentCustomer.getBags().addAll(bags); // Associate bags with the customer
                outboundFlight.getBags().addAll(bags); // Associate bags with outbound flight
                if ("ROUND_TRIP".equals(tripType) && returnFlightId != null) {
                    Flight returnFlight = flightService.findFlightById(returnFlightId);

                    // For round trips, split bags between outbound and return flights if necessary
                    for (Bag bag : bags) {
                        Bag returnBag = new Bag();
                        returnBag.setStatus(BagStatus.PRE_CHECK_IN);
                        returnBag.setReservation(reservation);
                        returnBag.setCustomer(currentCustomer);
                        returnBag.setFlight(returnFlight); // Assign to the return flight
                        currentCustomer.getBags().add(returnBag);
                        returnFlight.getBags().add(returnBag);
                    }
                }

                // Use pre-calculated total price from controller
                reservation.setTotalPrice(totalCost);

                reservationService.save(reservation);
                System.out.println("Reservation saved successfully for Customer ID " + currentCustomer.getId());

                // Log flight sales
                if (employeeId != null) {
                    Employee employee = employeeService.findEmployeeById(employeeId);
                    salesRecordService.logFlightSaleByEmployee(outboundFlight, currentCustomer, employee);
                } else {
                    salesRecordService.logCustomerFlightSale(outboundFlight, currentCustomer);
                }
                auditTrailService.logAction("Flight linked to reservation.", outboundFlight.getFlightID(), currentCustomer.getId(), employeeId);

                // Process DashPass based on selection
                boolean isNewPurchase = "new".equals(dashPassOption);
                if (isNewPurchase) {
                    DashPassReservation dashPassReservation = dashPassReservationService.createNewDashPassAndSaveNewDashPassReservation(
                            currentCustomer, reservation, outboundFlight
                    );
                    reservation.getDashPassReservations().add(dashPassReservation);
                    System.out.println("New DashPass created and linked to reservation.");
                    if (employeeId != null) {
                        Employee employee = employeeService.findEmployeeById(employeeId);
                        salesRecordService.logDashPassSaleByEmployee(dashPassReservation.getDashPass(), currentCustomer, employee);
                    } else {
                        salesRecordService.logCustomerDashPassSale(dashPassReservation.getDashPass(), currentCustomer);
                    }
                    auditTrailService.logAction("New DashPass linked to reservation.", dashPassReservation.getId(), currentCustomer.getId(), employeeId);
                } else if ("existing".equals(dashPassOption)) {
                    DashPassReservation dashPassReservation = dashPassReservationService.createNewDashPassReservationAndAssignExistingDashPass(
                            currentCustomer, reservation, outboundFlight
                    );
                    reservation.getDashPassReservations().add(dashPassReservation);
                    System.out.println("Existing DashPass linked to reservation.");
                    if (employeeId != null) {
                        Employee employee = employeeService.findEmployeeById(employeeId);
                        salesRecordService.logDashPassSaleByEmployee(dashPassReservation.getDashPass(), currentCustomer, employee);
                    } else {
                        salesRecordService.logCustomerDashPassSale(dashPassReservation.getDashPass(), currentCustomer);
                    }
                    auditTrailService.logAction("Existing DashPass linked to reservation.", dashPassReservation.getId(), currentCustomer.getId(), employeeId);
                }
                System.out.println("PurchaseFlightAsync - Total Price: " + totalCost);
                System.out.println("Bag Quantity: " + bagQuantity + ", Bags Added: " + currentCustomer.getBags().size());
                System.out.println("Payment Status: " + paymentStatus);


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