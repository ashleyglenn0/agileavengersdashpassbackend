package agileavengers.southwest_dashpass.services;

import agileavengers.southwest_dashpass.models.*;
import agileavengers.southwest_dashpass.repository.CustomerRepository;
import agileavengers.southwest_dashpass.repository.DashPassRepository;
import agileavengers.southwest_dashpass.repository.DashPassReservationRepository;
import agileavengers.southwest_dashpass.utils.ConfirmationNumberGenerator;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class DashPassService {
    private DashPassRepository dashPassRepository;
    private CustomerService customerService;
    private CustomerRepository customerRepository;
    private DashPassReservationRepository dashPassReservationRepository;
    private ConfirmationNumberGenerator confirmationNumberGenerator;

    private static final Double DASH_PASS_PRICE = 50.0;

    public Double getDashPassPrice() {
        return DASH_PASS_PRICE;
    }

    @Autowired
    public DashPassService(DashPassRepository dashPassRepository, CustomerService customerService, CustomerRepository customerRepository,
                           DashPassReservationRepository dashPassReservationRepository,
                           ConfirmationNumberGenerator confirmationNumberGenerator){
        this.dashPassRepository = dashPassRepository;
        this.customerService = customerService;
        this.customerRepository = customerRepository;
        this.dashPassReservationRepository = dashPassReservationRepository;
        this.confirmationNumberGenerator = confirmationNumberGenerator;
    }

    @Transactional
    public String purchaseDashPass(Customer customer, Reservation reservation, int dashPassQuantity, PaymentStatus paymentStatus) {
        if (paymentStatus != PaymentStatus.PAID) {
            throw new RuntimeException("Payment was not successful. DashPass purchase cannot be processed.");
        }

        String confirmationNumber = "";

        // Loop over DashPass quantity
        for (int i = 0; i < dashPassQuantity; i++) {
            // Check if the customer has reached their max number of DashPasses
            if (customer.getMaxNumberOfDashPasses() == customer.getTotalDashPassesCustomerHas()) {
                throw new RuntimeException("Customer has reached the maximum number of DashPasses.");
            }

            // Handle potential null value for totalDashPassesForUse
            if (customer.getTotalDashPassesForUse() == null) {
                customer.setTotalDashPassesForUse(0); // Set a default value if it's null
            }

            // Create a new DashPass
            DashPass dashPass = new DashPass();
            dashPass.setDateOfPurchase(LocalDate.now());
            dashPass.setRedeemed(false); // Set redeemed to false since it hasn't been used yet

            // Generate a confirmation number and set it for the DashPass
            confirmationNumber = confirmationNumberGenerator.generateConfirmationNumber();
            dashPass.setConfirmationNumber(confirmationNumber); // Assuming you added confirmationNumber to DashPass

            // Add the DashPass to the customer's DashPasses array if not linked to a reservation
            if (reservation == null) {
                customer.addDashPass(dashPass);  // Use the method to manually add to list and set the relationship
                System.out.println("DashPass added to customer list");
            }

            // If attaching to a reservation, check if the flight allows more DashPasses
            if (reservation != null) {
                for (Flight flight : reservation.getFlights()) {
                    if (flight.getDashPassReservations().size() >= flight.getMaxNumberOfDashPassesForFlight()) {
                        throw new RuntimeException("No more DashPasses can be added to flight: " + flight.getFlightNumber());
                    }

                    // Attach the DashPass to the reservation
                    DashPassReservation dashPassReservation = new DashPassReservation();
                    dashPassReservation.setDashPass(dashPass);
                    dashPassReservation.setReservation(reservation);
                    dashPassReservation.setBookingDate(LocalDate.now()); // Set the booking date for the reservation
                    customer.addDashPassReservation(dashPassReservation); // Attach the reservation

                    // Save the DashPassReservation entity
                    dashPassReservationRepository.save(dashPassReservation);
                }

                // Update the DashPass counters for the customer
                customer.setNumberOfDashPassesUsed(customer.getNumberOfDashPassesUsed() + 1);
            } else {
                // If the DashPass is not attached to a reservation, make it available for future use
                customer.setTotalDashPassesForUse(customer.getTotalDashPassesForUse() + 1);
            }

            // Update the DashPass count for the customer
            customer.setTotalDashPassesCustomerHas(customer.getTotalDashPassesCustomerHas() + 1);

            // Save the updated customer and their DashPasses
            dashPassRepository.save(dashPass);
            customerRepository.save(customer);
        }

        return confirmationNumber;
    }



    @Transactional
    public DashPass createAndAssignDashPassDuringPurchase(Customer customer, Flight flight) {
        DashPass newDashPass = new DashPass();
        newDashPass.setRedeemed(true);
        newDashPass.setCustomer(customer);

        // Save the DashPass first to persist it in the database
        dashPassRepository.save(newDashPass);

        // Now associate the DashPass with the DashPassReservation
        DashPassReservation reservation = new DashPassReservation();
        reservation.setDashPass(newDashPass);
        reservation.setFlight(flight);
        reservation.setCustomer(customer);

        // Save the DashPassReservation after the DashPass has been persisted
        dashPassReservationRepository.save(reservation);

        return newDashPass;
    }


    public DashPass save(DashPass dashPass) {
        return dashPassRepository.save(dashPass);  // This is where repository interaction happens
    }

    public DashPass findDashPassById(Long dashPassId) {
        // Optional is used to avoid exceptions if the DashPass doesn't exist
        return dashPassRepository.findById(dashPassId)
                .orElseThrow(() -> new RuntimeException("DashPass not found for id: " + dashPassId));
    }

    public Optional<DashPass> findAvailableDashPass(Customer customer) {
        // Fetch the list of DashPasses for the customer and filter those that are not redeemed
        return customer.getDashPasses()
                .stream()
                .filter(dashPass -> !dashPass.isRedeemed()) // Find DashPasses that are not redeemed
                .findFirst(); // Return the first available DashPass (if any)
    }

    public DashPass findAvailableDashPassForCustomer(Customer customer) {
        // Iterate through the list of DashPasses for the customer
        for (DashPass dashPass : customer.getDashPasses()) {
            // Check if the DashPass is not redeemed
            if (!dashPass.isRedeemed()) {
                return dashPass; // Return the first available (non-redeemed) DashPass
            }
        }
        // Return null if no available DashPass is found
        return null;
    }

    private void incrementDashPassesInUse(Customer customer) {
        customer.setNumberOfDashPassesUsed(customer.getNumberOfDashPassesUsed() + 1);
    }

    private void incrementTotalDashPassesOwned(Customer customer) {
        customer.setTotalDashPassesCustomerHas(customer.getTotalDashPassesCustomerHas() + 1);
    }

    private void incrementDashPassesAvailableForPurchase(Customer customer) {
        int availableForPurchase = customer.getMaxNumberOfDashPasses() - customer.getTotalDashPassesCustomerHas();
        customer.setNumberOfDashPassesAvailableForPurchase(availableForPurchase);
    }


    // Check if customer has available DashPasses
    private boolean customerHasAvailableDashPasses(Customer customer) {
        return customer.getNumberOfDashPassesAvailableForPurchase() < customer.getMaxNumberOfDashPasses();
    }

}
