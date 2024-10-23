package agileavengers.southwest_dashpass.services;

import agileavengers.southwest_dashpass.models.Customer;
import agileavengers.southwest_dashpass.models.DashPass;
import agileavengers.southwest_dashpass.models.DashPassReservation;
import agileavengers.southwest_dashpass.models.Flight;
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
        // Create a new DashPass and mark it as redeemed
        DashPass newDashPass = new DashPass();
        newDashPass.setRedeemed(true);
        newDashPass.setCustomer(customer);

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

        // Save the updated customer information (without updating DashPass counts)
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
            throw new RuntimeException("No available DashPasses for the customer.");
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

        // Save the reservation and persist the changes
        dashPassReservationRepository.save(reservation);

        // Save the updated customer information
        customerService.save(customer);

        return reservation;
    }

    @Transactional
    public DashPassReservation save(DashPassReservation dashPassReservation){
        return dashPassReservationRepository.save(dashPassReservation);
    }

    @Transactional
    public void handleDashPassOnSuccessfulPayment(Customer customer, DashPass dashPass, boolean isNewPurchase, Flight flight) {
        if (isNewPurchase) {
            // Add the new DashPass to the customer's list and mark it as redeemed
            customer.getDashPasses().add(dashPass);
            dashPass.setRedeemed(true);

            // Increment the total DashPasses the customer has
            customer.setTotalDashPassesCustomerHas(customer.getTotalDashPassesCustomerHas() + 1);
        }

        // Increment the number of DashPasses in use
        customer.setNumberOfDashPassesUsed(customer.getNumberOfDashPassesUsed() + 1);

        // Decrease available DashPasses for purchase only if the DashPass is not assigned to a reservation
        if (isNewPurchase) {
            customer.setTotalDashPassesForUse(customer.getTotalDashPassesForUse() + 1);
        }

        // Save the updated customer
        customerService.save(customer);

        // Also save the DashPassReservation if necessary
        DashPassReservation reservation = new DashPassReservation(customer, dashPass, flight, LocalDate.now());
        dashPassReservationRepository.save(reservation);
    }


    @Transactional
    public void rollbackDashPassChangesOnFailedPayment(Customer customer, DashPass dashPass, boolean isNewPurchase) {
        if (isNewPurchase) {
            // Remove the newly added DashPass (if applicable)
            customer.getDashPasses().remove(dashPass);

            // Rollback the DashPass-related counts
            customer.setNumberOfDashPassesUsed(customer.getNumberOfDashPassesUsed() - 1);
            customer.setTotalDashPassesCustomerHas(customer.getTotalDashPassesCustomerHas() - 1);
        } else {
            // If the existing DashPass was used, rollback its usage
            customer.setNumberOfDashPassesUsed(customer.getNumberOfDashPassesUsed() - 1);
            customer.setNumberOfDashPassesAvailableForPurchase(customer.getNumberOfDashPassesAvailableForPurchase() - 1);
        }

        // Save changes to the customer entity
        customerService.save(customer);
    }


}

