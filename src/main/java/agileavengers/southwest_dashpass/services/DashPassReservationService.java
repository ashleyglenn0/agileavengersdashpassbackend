package agileavengers.southwest_dashpass.services;

import agileavengers.southwest_dashpass.models.*;
import agileavengers.southwest_dashpass.repository.DashPassRepository;
import agileavengers.southwest_dashpass.repository.DashPassReservationRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class DashPassReservationService {

    @Autowired
    private DashPassReservationRepository dashPassReservationRepository;

    @Autowired
    private DashPassRepository dashPassRepository;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private FlightService flightService;

    @Autowired
    private DashPassService dashPassService;

    public DashPassReservation createNewDashPassAndSaveNewDashPassReservation(Customer customer, Flight flight) {
        // Create a new DashPass, mark it as redeemed, and associate it with the customer
        DashPass newDashPass = new DashPass();
        newDashPass.setRedeemed(true);
        newDashPass.setCustomer(customer);
        customer.getDashPasses().add(newDashPass);  // Add to customer’s DashPass list

        // Save the new DashPass first to persist it in the database
        dashPassRepository.save(newDashPass);

        // Create the DashPassReservation
        DashPassReservation reservation = new DashPassReservation(customer, newDashPass, flight, LocalDate.now());

        // Add the reservation to the flight's dashPassReservations
        flight.getDashPassReservations().add(reservation);

        // Add the reservation to the customer's dashPassReservations
        customer.getDashPassReservations().add(reservation);

        // Save the reservation and persist the changes
        dashPassReservationRepository.save(reservation);

        // Save the updated customer information
        customerService.save(customer);

        return reservation;
    }


    public DashPassReservation createNewDashPassReservationAndAssignExistingDashPass(Customer customer, Flight flight) {
        // Retrieve the list of available (non-redeemed) DashPasses associated with the customer
        List<DashPass> availableDashPasses = customer.getDashPasses().stream()
                .filter(dashPass -> !dashPass.isRedeemed()) // Get only the non-redeemed DashPasses
                .collect(Collectors.toList());

        // Check if there is at least one available DashPass
        if (availableDashPasses.isEmpty()) {
            throw new RuntimeException("Customer has no available (non-redeemed) DashPasses for reservation.");
        }

        // Assign the first available DashPass
        DashPass dashPassToAssign = availableDashPasses.get(0);
        dashPassToAssign.setRedeemed(true); // Mark the DashPass as redeemed

        // Create the DashPassReservation
        DashPassReservation reservation = new DashPassReservation();
        reservation.setCustomer(customer);
        reservation.setFlight(flight);
        reservation.setDashPass(dashPassToAssign);
        reservation.setBookingDate(LocalDate.now());  // Set current date as booking date

        // Add the reservation to the flight's dashPassReservations
        flight.getDashPassReservations().add(reservation);

        // Add the reservation to the customer's dashPassReservations
        customer.getDashPassReservations().add(reservation);

        // Persist changes in DashPass status and new reservation
        dashPassReservationRepository.save(reservation);
        customerService.save(customer);

        return reservation;
    }


    @Transactional
    public DashPassReservation save(DashPassReservation dashPassReservation){
        return dashPassReservationRepository.save(dashPassReservation);
    }

    @Transactional
    public void handleDashPassOnSuccessfulPayment(Customer customer, DashPass dashPass, boolean isNewPurchase, Flight flight) {
        // Ensure the customer is eligible to purchase the DashPass
        if (!customer.isCanPurchaseDashPass()) {
            throw new IllegalArgumentException("Customer has reached the maximum DashPass limit and cannot purchase more.");
        }

        if (isNewPurchase) {
            // Only add the new DashPass to the customer's list if it's not tied to a reservation
            if (flight == null) {
                customer.addDashPass(dashPass);  // Adds DashPass to the list, effectively increasing totalDashPasses
            } else {
                // If tied to a flight reservation, set the DashPass as redeemed immediately
                dashPass.setRedeemed(true);
            }
        }

        // Update dashPassesForPurchase only if this was a general DashPass purchase (not associated with a reservation)
        if (isNewPurchase && flight == null) {
            customer.setDashPassesForPurchase(customer.getDashPassesForPurchase() - 1);
        }

        // Save the updated customer information
        customerService.save(customer);

        // If there's a flight, create and save the DashPassReservation
        if (flight != null) {
            DashPassReservation reservation = new DashPassReservation(customer, dashPass, flight, LocalDate.now());
            dashPassReservationRepository.save(reservation);
        }
    }

    public void redeemDashPass(Customer customer, Reservation reservation, DashPass dashPass) throws Exception {
        // Check if the reservation already has a DashPass
        if (reservation.hasDashPass()) {
            throw new Exception("This reservation already has a DashPass attached.");
        }

        // Check if the DashPass is redeemable
        if (!dashPass.isRedeemable()) {
            throw new Exception("The selected DashPass is not available for redemption.");
        }

        // Mark the DashPass as redeemed
        dashPass.setRedeemed(true);

        // Create a new DashPassReservation linking the reservation, customer, and DashPass
        DashPassReservation dashPassReservation = new DashPassReservation();
        dashPassReservation.setCustomer(customer);
        dashPassReservation.setReservation(reservation);
        dashPassReservation.setDashPass(dashPass);
        dashPassReservation.setBookingDate(LocalDate.now());

        // Add the DashPassReservation to the customer's list of reservations
        customer.getDashPassReservations().add(dashPassReservation);

        // Save the updated DashPass and DashPassReservation
        dashPassService.save(dashPass);
        dashPassReservationRepository.save(dashPassReservation);

        // Optionally, update the reservation or notify the customer
    }

    public List<DashPassReservation> findPastDashPassReservations(Customer customer) {
        LocalDate today = LocalDate.now();
        return dashPassReservationRepository.findByCustomerAndReservation_DateBookedBefore(customer, today);
    }
}

