package agileavengers.southwest_dashpass.services;

import agileavengers.southwest_dashpass.models.Customer;
import agileavengers.southwest_dashpass.models.DashPass;
import agileavengers.southwest_dashpass.models.DashPassReservation;
import agileavengers.southwest_dashpass.models.Flight;
import agileavengers.southwest_dashpass.repository.DashPassReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;


@Service
public class DashPassReservationService {

    @Autowired
    private DashPassReservationRepository dashPassReservationRepository;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private FlightService flightService;

    @Autowired
    private DashPassService dashPassService;

    public DashPassReservation createAndSaveDashPassReservation(Long customerId, Flight flight, Long dashPassId) {
        Customer customer = customerService.findCustomerById(customerId);
        DashPass dashPass = dashPassService.findDashPassById(dashPassId);
        LocalDate bookingDate = LocalDate.now();

        DashPassReservation reservation = new DashPassReservation(customer, dashPass, flight, bookingDate);
        dashPass.setRedeemed(true);  // Mark DashPass as redeemed

        // Save the reservation
        return dashPassReservationRepository.save(reservation);
    }

    public DashPassReservation createAndAssignDashPassReservation(Customer customer, Flight flight, DashPass dashPass) {
        dashPass.setRedeemed(true);

        // Create the reservation
        DashPassReservation reservation = new DashPassReservation();
        reservation.setCustomer(customer);
        reservation.setFlight(flight);
        reservation.setDashPass(dashPass);
        reservation.setBookingDate(LocalDate.now());  // Set current date as booking date

        // Add the reservation to the flight's dashPassReservations
        flight.getDashPassReservations().add(reservation);

        // Add the reservation to the customer's dashPassReservations
        customer.getDashPassReservations().add(reservation);

        // Save the reservation and persist the changes
        dashPassReservationRepository.save(reservation);

        return reservation;
    }

    public DashPassReservation save(DashPassReservation dashPassReservation){
        return dashPassReservationRepository.save(dashPassReservation);
    }

}

