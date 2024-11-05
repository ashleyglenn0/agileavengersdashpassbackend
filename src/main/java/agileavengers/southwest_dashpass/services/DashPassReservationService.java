package agileavengers.southwest_dashpass.services;

import agileavengers.southwest_dashpass.models.*;
import agileavengers.southwest_dashpass.repository.DashPassRepository;
import agileavengers.southwest_dashpass.repository.DashPassReservationRepository;
import agileavengers.southwest_dashpass.repository.FlightRepository;
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

    @Autowired
    private FlightRepository flightRepository;

    public DashPassReservation createNewDashPassAndSaveNewDashPassReservation(Customer customer, Reservation flightReservation, Flight flight) {
        // Create a new DashPass, mark it as redeemed, and associate it with the customer
        DashPass newDashPass = new DashPass();
        newDashPass.setRedeemed(true);

        // Use addDashPass to add the DashPass to the customer and update summary
        customer.addDashPass(newDashPass);

        // Save the new DashPass in the database
        dashPassRepository.save(newDashPass);

        // Create the DashPassReservation and associate it with the flight and reservation
        DashPassReservation reservation = new DashPassReservation(customer, newDashPass, flightReservation, flight, LocalDate.now());

        // Add the reservation to the flight's DashPassReservations and adjust availability
        flight.getDashPassReservations().add(reservation);
        flight.setNumberOfDashPassesAvailable(flight.getNumberOfDashPassesAvailable() - 1);
        flightRepository.save(flight);

        // Use addDashPassReservation to add to the customer's dashPassReservations and update summary
        customer.addDashPassReservation(reservation);

        // Save the reservation in the database
        dashPassReservationRepository.save(reservation);

        // Save the updated customer with all DashPass and reservation updates
        customerService.save(customer);

        return reservation;
    }



    public DashPassReservation createNewDashPassReservationAndAssignExistingDashPass(Customer customer, Reservation flightReservation, Flight flight) {
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
        reservation.setReservation(flightReservation);
        reservation.setFlight(flight);
        reservation.setDashPass(dashPassToAssign);
        reservation.setBookingDate(LocalDate.now());  // Set current date as booking date

        // Add the reservation to the flight's dashPassReservations
        flight.getDashPassReservations().add(reservation);
        flight.setNumberOfDashPassesAvailable(flight.getNumberOfDashPassesAvailable() - 1);
        flightRepository.save(flight);

        // Use addDashPassReservation to add to the customer's dashPassReservations and update summary
        customer.addDashPassReservation(reservation);

        // Persist changes in DashPass status and new reservation
        dashPassReservationRepository.save(reservation);
        customerService.save(customer);

        return reservation;
    }



    @Transactional
    public DashPassReservation save(DashPassReservation dashPassReservation){
        return dashPassReservationRepository.save(dashPassReservation);
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

