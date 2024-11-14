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
    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private AuditTrailService auditTrailService;
    @Autowired
    private SalesRecordService salesRecordService;

    public DashPassReservation createNewDashPassAndSaveNewDashPassReservation(Customer customer, Reservation flightReservation, Flight flight) {
        // Create a new DashPass, mark it as redeemed, and associate it with the customer
        DashPass newDashPass = new DashPass();
        newDashPass.setRedeemed(true);

        // Add the new DashPass to the customer's dashPasses list temporarily
        customer.addDashPass(newDashPass);
        dashPassRepository.save(newDashPass); // Save the new DashPass in the database

        // Remove it from the dashPasses list and create a DashPassReservation for it
        customer.getDashPasses().remove(newDashPass); // Remove from unattached list as it's now being attached

        // Create the DashPassReservation and associate it with the flight and reservation
        DashPassReservation dashPassReservation = new DashPassReservation(customer, newDashPass, flightReservation, flight, LocalDate.now());
        dashPassReservation.setValidated(false);

        // Add the DashPassReservation to the customer's dashPassReservations
        customer.addDashPassReservation(dashPassReservation);

        // Update flight availability
        flight.getDashPassReservations().add(dashPassReservation);
        flight.setNumberOfDashPassesAvailable(flight.getNumberOfDashPassesAvailable() - 1);
        flightRepository.save(flight); // Save updated flight with adjusted DashPass availability

        // Save the DashPassReservation in the database
        dashPassReservationRepository.save(dashPassReservation);

        // Update the customer's DashPass summary
        customer.updateDashPassSummary();
        customerService.save(customer); // Save the customer with updated counts

        return dashPassReservation;
    }


    public DashPassReservation createNewDashPassReservationAndAssignExistingDashPass(Customer customer, Reservation flightReservation, Flight flight) {
        List<DashPass> availableDashPasses = customer.getDashPasses().stream()
                .filter(dashPass -> !dashPass.isRedeemed())
                .collect(Collectors.toList());

        if (availableDashPasses.isEmpty()) {
            throw new RuntimeException("Customer has no available (non-redeemed) DashPasses for reservation.");
        }

        DashPass dashPassToAssign = availableDashPasses.get(0);
        dashPassToAssign.setRedeemed(true); // Mark the DashPass as redeemed

        // Remove from unattached DashPass list
        customer.getDashPasses().remove(dashPassToAssign);

        DashPassReservation reservation = new DashPassReservation();
        reservation.setCustomer(customer);
        reservation.setReservation(flightReservation);
        reservation.setFlight(flight);
        reservation.setDashPass(dashPassToAssign);
        reservation.setBookingDate(LocalDate.now());
        reservation.setValidated(false);

        flight.getDashPassReservations().add(reservation);
        flight.setNumberOfDashPassesAvailable(flight.getNumberOfDashPassesAvailable() - 1);
        flightRepository.save(flight);

        customer.addDashPassReservation(reservation);
        customer.updateDashPassSummary();
        dashPassReservationRepository.save(reservation);
        customerService.save(customer);

        return reservation;
    }


    @Transactional
    public DashPassReservation save(DashPassReservation dashPassReservation){
        return dashPassReservationRepository.save(dashPassReservation);
    }


    public void redeemDashPass(Customer customer, Reservation reservation, DashPass dashPass, Employee employeeId) throws Exception {
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

        // Remove the DashPass from the customer's unattached DashPasses list
        customer.getDashPasses().remove(dashPass);

        // Create a new DashPassReservation linking the reservation, customer, and DashPass
        DashPassReservation dashPassReservation = new DashPassReservation();
        dashPassReservation.setCustomer(customer);
        dashPassReservation.setReservation(reservation);
        dashPassReservation.setDashPass(dashPass);
        dashPassReservation.setBookingDate(LocalDate.now());
        dashPassReservation.setValidated(false);

        // Add the DashPassReservation to the customer's list of reservations
        customer.addDashPassReservation(dashPassReservation);

        // Save the updated DashPass and DashPassReservation
        dashPassService.save(dashPass);
        dashPassReservationRepository.save(dashPassReservation);

        // Update DashPass summary counts in the Customer
        customer.updateDashPassSummary();
        customerService.save(customer); // Persist the updated counts

        if (employeeId != null) {
            Employee employee = employeeService.findEmployeeById(employeeId.getId());
            salesRecordService.logDashPassSaleByEmployee(dashPass, customer, employee);
        } else {
            salesRecordService.logCustomerDashPassSale(dashPass, customer);
        }
        auditTrailService.logAction("Existing DashPass linked to reservation.", dashPass.getDashpassId(), customer.getId(), employeeId.getId());
    }

    public List<DashPassReservation> findPastDashPassReservations(Customer customer) {
        LocalDate today = LocalDate.now();
        return dashPassReservationRepository.findByCustomerAndReservation_DateBookedBefore(customer, today);
    }
}