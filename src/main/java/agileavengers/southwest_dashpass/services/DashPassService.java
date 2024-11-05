package agileavengers.southwest_dashpass.services;

import agileavengers.southwest_dashpass.dtos.DashPassSummary;
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
            if (customer.getTotalDashPasses() >= customer.getMaxDashPasses()) {
                throw new RuntimeException("Customer has reached the maximum number of DashPasses.");
            }

            // Create a new DashPass
            DashPass dashPass = new DashPass();
            dashPass.setDateOfPurchase(LocalDate.now());
            dashPass.setRedeemed(false); // Set redeemed to false since it hasn't been used yet

            // Generate a confirmation number and set it for the DashPass
            confirmationNumber = confirmationNumberGenerator.generateConfirmationNumber();
            dashPass.setConfirmationNumber(confirmationNumber);

            // Add the DashPass to the customer's DashPasses list if not linked to a reservation
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
                    customer.addDashPassReservation(dashPassReservation);

                    // Save the DashPassReservation entity
                    dashPassReservationRepository.save(dashPassReservation);
                }
            }

            // If the DashPass is not attached to a reservation, make it available for future use
            if (reservation == null) {
                customer.setDashPassesForPurchase(customer.getDashPassesForPurchase() - 1);
            }

            // Save the updated customer and their DashPasses
            dashPassRepository.save(dashPass);
            customerRepository.save(customer);
        }

        return confirmationNumber;
    }


    public DashPass save(DashPass dashPass) {
        return dashPassRepository.save(dashPass);  // This is where repository interaction happens
    }

    public DashPass findDashPassById(Long dashpassId) {
        // Optional is used to avoid exceptions if the DashPass doesn't exist
        return dashPassRepository.findById(dashpassId)
                .orElseThrow(() -> new RuntimeException("DashPass not found for id: " + dashpassId));
    }

    public DashPass findDashPassByIdWithReservation(Long dashPassId) {
        DashPass dashPass = dashPassRepository.findById(dashPassId).orElse(null);

        if (dashPass != null && dashPass.getDashPassReservation() != null) {
            // Access a field on reservation to trigger initialization if it’s lazy-loaded
            dashPass.getDashPassReservation().getId();
        }

        return dashPass;  // Returns the DashPass with initialized reservation (if it exists)
    }
    public DashPass findDashPassByIdWithCustomerUserAndReservation(Long dashPassId) {
        return dashPassRepository.findDashPassByIdWithCustomerUserAndReservation(dashPassId);
    }

    public DashPassSummary calculateDashPassSummary(Customer customer) {
        // Get all DashPasses the customer owns
        List<DashPass> dashPasses = dashPassRepository.findByCustomer(customer);

        // Count DashPasses currently attached to active reservations
        long inUseDashPasses = dashPassReservationRepository.findByCustomerAndIsActive(customer, true)
                .stream()
                .filter(reservation -> reservation.getDashPass() != null && !reservation.getBookingDate().isBefore(LocalDate.now()))
                .count();

        // Calculate the total DashPasses in the customer’s possession
        int totalDashPasses = dashPasses.size() + (int) inUseDashPasses;

        // Calculate how many DashPasses are still available for purchase
        int availableForPurchase = customer.getMaxDashPasses() - totalDashPasses;

        return new DashPassSummary(totalDashPasses, (int) inUseDashPasses, availableForPurchase);
    }

}
