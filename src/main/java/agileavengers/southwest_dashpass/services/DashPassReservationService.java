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

//    public DashPassReservation createNewDashPassAndSaveNewDashPassReservation(Customer customer, Reservation flightReservation, Flight flight) {
//        // Create a new DashPass, mark it as redeemed, and associate it with the customer
//        DashPass newDashPass = new DashPass();
////        newDashPass.setRedeemed(true);
//
//        // Add the new DashPass to the customer's dashPasses list temporarily
//        customer.addDashPass(newDashPass);
//        dashPassRepository.save(newDashPass); // Save the new DashPass in the database
//
//        // Remove it from the dashPasses list and create a DashPassReservation for it
//        //customer.getDashPasses().remove(newDashPass); // Remove from unattached list as it's now being attached
//
//        // Create the DashPassReservation and associate it with the flight and reservation
//        DashPassReservation dashPassReservation = new DashPassReservation(customer, newDashPass, flightReservation, flight, LocalDate.now());
//        dashPassReservation.setValidated(false);
//
//        // Add the DashPassReservation to the customer's dashPassReservations
//        customer.addDashPassReservation(dashPassReservation);
//
//        // Update flight availability
//        flight.getDashPassReservations().add(dashPassReservation);
//        flight.setNumberOfDashPassesAvailable(flight.getNumberOfDashPassesAvailable() - 1);
//        flightRepository.save(flight); // Save updated flight with adjusted DashPass availability
//
//        // Save the DashPassReservation in the database
//        dashPassReservationRepository.save(dashPassReservation);
//
//        // Update the customer's DashPass summary
//        customer.updateDashPassSummary();
//        customerService.save(customer); // Save the customer with updated counts
//
//        return dashPassReservation;
//    }
//public DashPassReservation createNewDashPassAndSaveNewDashPassReservation(Customer customer, Reservation flightReservation, Flight flight) {
//    System.out.println("Starting createNewDashPassAndSaveNewDashPassReservation...");
//
//    // Log initial state
//    System.out.println("Initial DashPass list: " + customer.getDashPasses());
//
//    // Create a new DashPass
//    DashPass newDashPass = new DashPass();
//    newDashPass.setDateOfPurchase(LocalDate.now());
//    newDashPass.setPendingValidation(true);
//    newDashPass.setCustomer(customer);
//    dashPassRepository.save(newDashPass);
//
//    // Log after DashPass save
//    System.out.println("DashPass created and saved. ID: " + newDashPass.getDashpassId());
//    System.out.println("DashPass list after save: " + customer.getDashPasses());
//
//    // Create DashPassReservation
//    DashPassReservation dashPassReservation = new DashPassReservation(customer, newDashPass, flightReservation, flight, LocalDate.now());
//    dashPassReservation.setValidated(false);
//    customer.addDashPassReservation(dashPassReservation);
//
//    // Remove DashPass from unattached list
//    boolean removed = customer.getDashPasses().remove(newDashPass);
//    System.out.println("DashPass removed from unattached list: " + removed);
//    System.out.println("DashPass list after removal: " + customer.getDashPasses());
//
//    // Update flight availability
//    flight.getDashPassReservations().add(dashPassReservation);
//    flight.setNumberOfDashPassesAvailable(flight.getNumberOfDashPassesAvailable() - 1);
//    flightRepository.save(flight);
//
//    // Save DashPassReservation
//    dashPassReservationRepository.save(dashPassReservation);
//
//    // Update customer DashPass summary
//    customer.updateDashPassSummary();
//    customerService.save(customer);
//
//    // Log final state
//    System.out.println("Final DashPass list: " + customer.getDashPasses());
//    System.out.println("DashPassReservations: " + customer.getDashPassReservations());
//
//    return dashPassReservation;
//}

    public DashPassReservation createNewDashPassAndSaveNewDashPassReservation(Customer customer, Reservation flightReservation, Flight flight) {
        System.out.println("Starting createNewDashPassAndSaveNewDashPassReservation...");

        // Log initial state
        System.out.println("Initial DashPass list size: " + customer.getDashPasses().size());
        System.out.println("Initial DashPass list: " + customer.getDashPasses());
        System.out.println("Initial DashPassReservations list: " + customer.getDashPassReservations());

        // Create a new DashPass
        DashPass newDashPass = new DashPass();
        newDashPass.setDateOfPurchase(LocalDate.now());
        newDashPass.setPendingValidation(true);
        newDashPass.setCustomer(customer);

        // Save the new DashPass
        dashPassRepository.save(newDashPass);
        System.out.println("DashPass created and saved. ID: " + newDashPass.getDashpassId());
        System.out.println("New DashPass details: " + newDashPass);

        // Log after DashPass save
        System.out.println("DashPass list after save (size): " + customer.getDashPasses().size());
        System.out.println("DashPass list after save: " + customer.getDashPasses());

        // Create DashPassReservation and associate it with the reservation
        DashPassReservation dashPassReservation = new DashPassReservation(customer, newDashPass, flightReservation, flight, LocalDate.now());
        dashPassReservation.setValidated(false);
        System.out.println("Created DashPassReservation but not saved yet. Details: " + dashPassReservation);

        // Mark DashPass as IN_USE since it is associated with a reservation
        newDashPass.setPendingValidation(false);
        newDashPass.setStatus(DashPassStatus.VALID);
        System.out.println("Updated DashPass status to VALID. DashPass ID: " + newDashPass.getDashpassId());

        // Save the DashPassReservation
        customer.addDashPassReservation(dashPassReservation);
        dashPassReservationRepository.save(dashPassReservation);
        System.out.println("DashPassReservation saved. ID: " + dashPassReservation.getId());
        System.out.println("Updated customer DashPassReservations list: " + customer.getDashPassReservations());

        // Update flight DashPass availability
        flight.getDashPassReservations().add(dashPassReservation);
        flight.setNumberOfDashPassesAvailable(flight.getNumberOfDashPassesAvailable() - 1);
        flightRepository.save(flight);
        System.out.println("Updated flight details. DashPass availability: " + flight.getNumberOfDashPassesAvailable());

        // **Check and clean up the last DashPass entry**
        List<DashPass> dashPassList = customer.getDashPasses();
        System.out.println("DashPass list before cleanup: " + dashPassList);

        if (!dashPassList.isEmpty()) {
            DashPass lastEntry = dashPassList.get(dashPassList.size() - 1);
            System.out.println("Checking last DashPass in the list. ID: " + lastEntry.getDashpassId() + ", Status: " + lastEntry.getStatus());

            // If the last DashPass isn't marked as IN_USE, remove it
            if (lastEntry.getStatus() != DashPassStatus.VALID) {
                dashPassList.remove(lastEntry); // Remove from the list
                dashPassRepository.delete(lastEntry); // Remove from database
                System.out.println("Untracked DashPass removed: ID " + lastEntry.getDashpassId());
            }
        }

        // Update customer DashPass summary and save
        System.out.println("Updating customer's DashPass summary...");
        customer.updateDashPassSummary();
        customerService.save(customer);

        // Log final state
        System.out.println("Final DashPass list size: " + customer.getDashPasses().size());
        System.out.println("Final DashPass list: " + customer.getDashPasses());
        System.out.println("Final DashPassReservations list: " + customer.getDashPassReservations());
        System.out.println("Test");

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
        dashPassReservationRepository.save(reservation);

        customer.updateDashPassSummary();
        customerService.save(customer);

        return reservation;
    }


    @Transactional
    public DashPassReservation save(DashPassReservation dashPassReservation){
        return dashPassReservationRepository.save(dashPassReservation);
    }


    public void addDashPassToExistingFlightReservation(Customer customer, Reservation reservation, DashPass dashPass, Employee employeeId) throws Exception {
        // Check if the reservation already has a DashPass
        if (reservation.hasDashPass()) {
            throw new Exception("This reservation already has a DashPass attached.");
        }

        // Check if the DashPass is redeemable
        if (!dashPass.isRedeemable()) {
            throw new Exception("The selected DashPass is not available for redemption.");
        }

        // Mark the DashPass as redeemed
//        dashPass.setRedeemed(true);

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