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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DashPassService {
    private DashPassRepository dashPassRepository;
    private CustomerService customerService;
    private CustomerRepository customerRepository;
    private DashPassReservationRepository dashPassReservationRepository;
    private ConfirmationNumberGenerator confirmationNumberGenerator;
    private FlightRepository flightRepository;
    private final EmployeeService employeeService;
    private final AuditTrailService auditTrailService;
    private final SalesRecordService salesRecordService;

    private static final Double DASH_PASS_PRICE = 50.0;

    public Double getDashPassPrice() {
        return DASH_PASS_PRICE;
    }

    @Autowired
    public DashPassService(DashPassRepository dashPassRepository, CustomerService customerService, CustomerRepository customerRepository,
                           DashPassReservationRepository dashPassReservationRepository,
                           ConfirmationNumberGenerator confirmationNumberGenerator, FlightRepository flightRepository,
                           EmployeeService employeeService, AuditTrailService auditTrailService, SalesRecordService salesRecordService){
        this.dashPassRepository = dashPassRepository;
        this.customerService = customerService;
        this.customerRepository = customerRepository;
        this.dashPassReservationRepository = dashPassReservationRepository;
        this.confirmationNumberGenerator = confirmationNumberGenerator;
        this.flightRepository = flightRepository;
        this.salesRecordService = salesRecordService;
        this.employeeService = employeeService;
        this.auditTrailService = auditTrailService;
    }

    @Transactional
    public String purchaseDashPass(Customer customer, Reservation reservation, int dashPassQuantity, PaymentStatus paymentStatus, Employee employeeId) {
        System.out.println("purchaseDashPass called for customer: " + customer.getId() + " with DashPassQuantity: " + dashPassQuantity);

        if (paymentStatus != PaymentStatus.PAID) {
            throw new RuntimeException("Payment was not successful. DashPass purchase cannot be processed.");
        }

        String confirmationNumber = "";

        for (int i = 0; i < dashPassQuantity; i++) {
            System.out.println("Iteration: " + (i + 1) + " / " + dashPassQuantity);

            // Check if customer has reached the maximum number of DashPasses
            if (customer.getTotalDashPassCount() >= customer.getMaxDashPasses()) {
                throw new RuntimeException("Customer has reached the maximum number of DashPasses.");
            }

            if (reservation == null) {
                // Create a DashPass for unattached use
                System.out.println("Creating DashPass for unattached use.");
                DashPass dashPass = new DashPass();
                dashPass.setDateOfPurchase(LocalDate.now());
                dashPass.setCustomer(customer); // Set relationship with Customer
                confirmationNumber = confirmationNumberGenerator.generateConfirmationNumber();
                dashPass.setConfirmationNumber(confirmationNumber);

                // Add DashPass to customer's unattached list
                customer.addDashPass(dashPass);
                System.out.println("DashPass added to unattached list. DashPass ID: " + dashPass.getDashpassId());
                System.out.println("Customer's DashPass list after addition: " + customer.getDashPasses().stream()
                        .map(DashPass::getDashpassId)
                        .toList());

                // Log the purchase
                if (employeeId != null) {
                    Employee employee = employeeService.findEmployeeById(employeeId.getId());
                    salesRecordService.logDashPassSaleByEmployee(dashPass, customer, employee);
                    auditTrailService.logAction("New unattached DashPass purchased by customer.", dashPass.getDashpassId(), customer.getId(), employee.getId());
                } else {
                    salesRecordService.logCustomerDashPassSale(dashPass, customer);
                    auditTrailService.logAction("New unattached DashPass purchased by customer.", dashPass.getDashpassId(), customer.getId(), null);
                }

            } else {
                // Create a DashPass for reservation
                System.out.println("Creating DashPass for reservation.");
                DashPass dashPass = new DashPass();
                dashPass.setDateOfPurchase(LocalDate.now());
                dashPass.setCustomer(customer); // Set relationship with Customer
                confirmationNumber = confirmationNumberGenerator.generateConfirmationNumber();
                dashPass.setConfirmationNumber(confirmationNumber);
                dashPass.setPendingValidation(true); // Mark as pending validation

                DashPassReservation dashPassReservation = new DashPassReservation();
                dashPassReservation.setDashPass(dashPass);
                dashPassReservation.setReservation(reservation);
                dashPassReservation.setBookingDate(LocalDate.now());
                dashPassReservation.setValidated(false);
                dashPassReservation.setCustomer(customer);

                // Link DashPassReservation to flights
                for (Flight flight : reservation.getFlights()) {
                    flight.getDashPassReservations().add(dashPassReservation);
                    flight.setNumberOfDashPassesAvailable(flight.getNumberOfDashPassesAvailable() - 1);
                }

                customer.addDashPassReservation(dashPassReservation);
                System.out.println("DashPass linked to reservation. DashPass ID: " + dashPass.getDashpassId());
                System.out.println("Customer's DashPassReservation list after addition: " + customer.getDashPassReservations().stream()
                        .map(DashPassReservation::getId)
                        .toList());

                // Log the purchase
                if (employeeId != null) {
                    Employee employee = employeeService.findEmployeeById(employeeId.getId());
                    salesRecordService.logDashPassSaleByEmployee(dashPass, customer, employee);
                    auditTrailService.logAction("New DashPass linked to reservation.", dashPass.getDashpassId(), customer.getId(), employee.getId());
                } else {
                    salesRecordService.logCustomerDashPassSale(dashPass, customer);
                    auditTrailService.logAction("New DashPass linked to reservation.", dashPass.getDashpassId(), customer.getId(), null);
                }
            }

            // Log current customer state
            System.out.println("Customer DashPass list after iteration: " + customer.getDashPasses().stream()
                    .map(DashPass::getDashpassId)
                    .toList());
        }

        // Final update of DashPass counts
        System.out.println("Finalizing DashPass counts...");
        customer.updateDashPassSummary();
        System.out.println("Customer DashPass summary after update:");
        System.out.println("Total DashPass Count: " + customer.getTotalDashPassCount());
        System.out.println("Available DashPass Count: " + customer.getAvailableDashPassCount());
        System.out.println("DashPass In Use Count: " + customer.getDashPassInUseCount());

        // Save the customer and cascade changes to DashPass and DashPassReservation
        customerRepository.save(customer);

        System.out.println("Final DashPass Count: " + customer.getTotalDashPassCount());
        return confirmationNumber;
    }



    public DashPass save(DashPass dashPass) {
        System.out.println("Saving DashPass with ID: " + dashPass.getDashpassId());
        DashPass savedDashPass = dashPassRepository.save(dashPass);
        System.out.println("Saved DashPass with ID: " + savedDashPass.getDashpassId());
        return savedDashPass;
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

    public Map<String, List<DashPass>> getDashPassListByStatus(Customer customer) {
        List<DashPass> unattachedDashPasses = customer.getDashPasses().stream()
                .filter(dashPass -> dashPass.getDashPassReservation() == null)
                .collect(Collectors.toList());

        List<DashPass> attachedDashPasses = customer.getDashPasses().stream()
                .filter(dashPass -> dashPass.getDashPassReservation() != null)
                .collect(Collectors.toList());

        Map<String, List<DashPass>> dashPassMap = new HashMap<>();
        dashPassMap.put("unattached", unattachedDashPasses);
        dashPassMap.put("attached", attachedDashPasses);

        return dashPassMap;
    }

}