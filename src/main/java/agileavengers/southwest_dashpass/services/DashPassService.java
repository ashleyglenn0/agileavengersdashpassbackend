package agileavengers.southwest_dashpass.services;

import agileavengers.southwest_dashpass.dtos.DashPassSummary;
import agileavengers.southwest_dashpass.models.*;
import agileavengers.southwest_dashpass.repository.CustomerRepository;
import agileavengers.southwest_dashpass.repository.DashPassRepository;
import agileavengers.southwest_dashpass.repository.DashPassReservationRepository;
import agileavengers.southwest_dashpass.repository.FlightRepository;
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
    private FlightRepository flightRepository;

    private static final Double DASH_PASS_PRICE = 50.0;

    public Double getDashPassPrice() {
        return DASH_PASS_PRICE;
    }

    @Autowired
    public DashPassService(DashPassRepository dashPassRepository, CustomerService customerService, CustomerRepository customerRepository,
                           DashPassReservationRepository dashPassReservationRepository,
                           ConfirmationNumberGenerator confirmationNumberGenerator, FlightRepository flightRepository){
        this.dashPassRepository = dashPassRepository;
        this.customerService = customerService;
        this.customerRepository = customerRepository;
        this.dashPassReservationRepository = dashPassReservationRepository;
        this.confirmationNumberGenerator = confirmationNumberGenerator;
        this.flightRepository = flightRepository;
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
            if (customer.getTotalDashPassCount() >= customer.getMaxDashPasses()) {
                throw new RuntimeException("Customer has reached the maximum number of DashPasses.");
            }

            // Create a new DashPass
            DashPass dashPass = new DashPass();
            dashPass.setDateOfPurchase(LocalDate.now());
            dashPass.setRedeemed(false); // Set redeemed to false since it hasn't been used yet

            // Generate a confirmation number and set it for the DashPass
            confirmationNumber = confirmationNumberGenerator.generateConfirmationNumber();
            dashPass.setConfirmationNumber(confirmationNumber);

            if (reservation == null) {
                // DashPass is not linked to a reservation, add it directly to the customer
                customer.addDashPass(dashPass);
                System.out.println("DashPass added to customer list.");
            } else {
                // Attach the DashPass to the flight(s) within the reservation
                for (Flight flight : reservation.getFlights()) {
                    if (flight.getDashPassReservations().size() >= flight.getMaxNumberOfDashPassesForFlight()) {
                        throw new RuntimeException("No more DashPasses can be added to flight: " + flight.getFlightNumber());
                    }

                    // Create and link a new DashPassReservation
                    DashPassReservation dashPassReservation = new DashPassReservation();
                    dashPassReservation.setDashPass(dashPass);
                    dashPassReservation.setReservation(reservation);
                    dashPassReservation.setFlight(flight);
                    dashPassReservation.setBookingDate(LocalDate.now());

                    // Add the DashPassReservation to customer and flight
                    customer.addDashPassReservation(dashPassReservation);
                    flight.getDashPassReservations().add(dashPassReservation);

                    // Update available DashPass count for the flight
                    flight.setNumberOfDashPassesAvailable(flight.getNumberOfDashPassesAvailable() - 1);

                    // Save the DashPassReservation and flight
                    dashPassReservationRepository.save(dashPassReservation);
                    flightRepository.save(flight);

                    System.out.println("DashPass linked to flight " + flight.getFlightNumber());
                }
            }

            // Save the new DashPass to persist it in the database
            dashPassRepository.save(dashPass);
        }

        // Save the updated customer information
        customerRepository.save(customer);

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

}
