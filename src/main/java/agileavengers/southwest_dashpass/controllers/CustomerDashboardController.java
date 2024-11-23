package agileavengers.southwest_dashpass.controllers;

import agileavengers.southwest_dashpass.dtos.DashPassSummary;
import agileavengers.southwest_dashpass.models.*;
import agileavengers.southwest_dashpass.services.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Random;

@Controller
public class CustomerDashboardController {
    private final CustomerService customerService;
    private final ReservationService reservationService;
    private final DashPassReservationService dashPassReservationService;
    private final SupportRequestService supportRequestService;
    private final EmployeeService employeeService;
    private final DashPassService dashPassService;
    private final BagService bagService;

    // Constructor Injection
    @Autowired
    public CustomerDashboardController(CustomerService customerService, ReservationService reservationService,
                                       DashPassReservationService dashPassReservationService, SupportRequestService supportRequestService,
                                       EmployeeService employeeService, DashPassService dashPassService, BagService bagService) {
        this.customerService = customerService;
        this.reservationService = reservationService;
        this.dashPassReservationService = dashPassReservationService;
        this.supportRequestService = supportRequestService;
        this.employeeService = employeeService;
        this.dashPassService = dashPassService;
        this.bagService = bagService;
    }

    @GetMapping("/customer/{customerID}/customerdashboard")
    public String showCustomerDashboard(@PathVariable Long customerID, Model model, Principal principal) {
        // Fetch customer by ID
        Customer customer = customerService.findCustomerById(customerID);

        List<Reservation> upcomingFlight = reservationService.getUpcomingReservationsForCustomer(customerID);

        // If customer is not found, show a custom error page

        if (customer == null) {
            model.addAttribute("error", "Customer not found with ID: " + customerID);
            return "error/customer-not-found";  // You should have a custom error page
        }

        // Ensure the customer belongs to the currently logged-in user
        if (!customer.getUser().getUsername().equals(principal.getName())) {
            return "error/403";  // Show a 403 error page if access is unauthorized
        }

//        System.out.println("Upcoming Flight: " + (upcomingFlight != null ? upcomingFlight.getFlightDepartureDate() : "None"));
        for (Reservation reservation : upcomingFlight) {
            System.out.println("Reservation ID: " + reservation.getReservationId());
            System.out.println("Flights: " + reservation.getFlights());  // Check if flights are populated

            System.out.println("DashPass Reservations: " + reservation.getDashPassReservations());

        }

        Reservation soonestReservation = reservationService.findSoonestUpcomingReservationForCustomer(customerID);
        List<Reservation> pastReservations = reservationService.findPastReservations(customer);

        // Fetch past DashPass reservations
        List<DashPassReservation> pastDashPassReservations = dashPassReservationService.findPastDashPassReservations(customer);
        model.addAttribute("soonestReservation", soonestReservation);


        // Add customer information to the model
        model.addAttribute("customer", customer);
        model.addAttribute("upcomingFlight", upcomingFlight);
        model.addAttribute("pastReservations", pastReservations);
        model.addAttribute("pastDashPassReservations", pastDashPassReservations);


        return "customerdashboard";  // Return the dashboard view
    }

    @PostMapping("/customer/{customerId}/submitSupportRequest")
    public String submitSupportRequest(@PathVariable Long customerId,
                                       @RequestParam Long employeeId, // ID of the assigned employee
                                       @RequestParam String subject,
                                       @RequestParam String message,
                                       @RequestParam SupportRequest.Priority priority,
                                       Model model) {
        // Fetch the customer and employee by their IDs
        Customer customer = customerService.findCustomerById(customerId);
        Employee employee = employeeService.findEmployeeById(employeeId);

        // Create the support request
        supportRequestService.createSupportRequest(customer.getId(), employee.getId(), subject, message, priority);

        // Add a success message to the model
        model.addAttribute("successMessage", "Your support request has been submitted.");

        // Return the appropriate view or redirect
        return "customerDashboard"; // Replace with the view name for the customer dashboard or confirmation page
    }

    @GetMapping("/customer/{customerID}/dashpass-summary")
    public String getDashPassSummary(@PathVariable Long customerID, Model model) {
        // Retrieve the customer
        Customer customer = customerService.findCustomerById(customerID);
        model.addAttribute("customer", customer);

        // Create DashPassSummary directly using the Customer entity
        DashPassSummary dashPassSummary = new DashPassSummary(customer);
        model.addAttribute("dashPassSummary", dashPassSummary);

        return "dashpass-summary";  // View to display the DashPass summary
    }


    @GetMapping("/customer/{customerID}/send-support-request")
    public String showSendSupportRequestForm(@PathVariable Long customerID, Model model) {
        Customer customer = customerService.findCustomerById(customerID); // Fetch customer by ID
        if (customer == null) {
            throw new IllegalArgumentException("Customer not found with ID: " + customerID);
        }

        model.addAttribute("customer", customer); // Add customer to model
        model.addAttribute("customerID", customerID); // Add customerID directly to model
        model.addAttribute("supportRequest", new SupportRequest()); // Initialize empty support request

        return "send-support-request"; // Render template
    }

    @PostMapping("/customer/{customerID}/send-support-request")
    public String submitSupportRequest(
            @PathVariable Long customerID,
            @RequestParam("subject") String subject,
            @RequestParam("message") String message,
            @RequestParam("priority") SupportRequest.Priority priority,
            Model model) {

        // Find the customer by ID
        Customer customer = customerService.findCustomerById(customerID);
        if (customer == null) {
            model.addAttribute("errorMessage", "Customer not found.");
            return "send-support-request";
        }

        // Create the support request without an employee
        try {
            supportRequestService.createSupportRequest(customerID, null, subject, message, priority);
            model.addAttribute("successMessage", "Your support request has been submitted successfully.");
        } catch (Exception e) {
            model.addAttribute("errorMessage", "An error occurred while submitting your request. Please try again.");
            e.printStackTrace();
        }

        return "send-support-request";
    }

    @GetMapping("/customer/{customerID}/customer-support-requests")
    public String viewSupportRequests(@PathVariable Long customerID, Principal principal, Model model) {
        Customer customer = customerService.findCustomerById(customerID);
        if (customer == null || !customer.getUser().getUsername().equals(principal.getName())) {
            return "error/403";  // Unauthorized access error page
        }

        List<SupportRequest> supportRequests = supportRequestService.getSupportRequestsForCustomer(customer);
        model.addAttribute("supportRequests", supportRequests);
        model.addAttribute("customer", customer);

        return "customer-support-requests";  // Name of the template
    }

    @GetMapping("/customer/{customerId}/ticket/{reservationId}")
    public String viewTicket(@PathVariable Long customerId, @PathVariable Long reservationId, Model model) {
        Customer customer = customerService.findCustomerById(customerId);
        model.addAttribute("customer", customer);
        // Retrieve the reservation from the service
        Reservation reservation = reservationService.findById(reservationId);

        // Verify that the reservation belongs to the specified customer
        if (!reservation.getCustomer().getId().equals(customerId)) {
            throw new AccessDeniedException("This reservation does not belong to the customer.");
        }

        // Add the reservation and DashPass reservations to the model
        model.addAttribute("reservation", reservation);
        model.addAttribute("dashPassReservations", reservation.getDashPassReservations());

        return "ticket"; // Thymeleaf template name
    }

    @GetMapping("/customer/{customerID}/trackbag")
    public String trackBags(
            @PathVariable Long customerID,
            @RequestParam(value = "reservationId", required = false) Long reservationId,
            Model model) {

        Customer customer = customerService.findCustomerById(customerID);

        List<Bag> bags;
        Reservation reservation = null;

        if (reservationId != null) {
            bags = bagService.findBagsByReservationId(reservationId);
            reservation = reservationService.findById(reservationId); // Fetch reservation
        } else {
            bags = bagService.findBagsByCustomerId(customerID);
        }

        // Get the bag status (use the first bag's status for simplicity)
        String bagStatusDisplayName = bags.isEmpty() ? null : bags.get(0).getStatus().getDisplayName();
        System.out.println("Bag Status: " + bagStatusDisplayName);

        // Generate randomized time between 5 and 25 minutes
        int randomizedTimeToNextStep = new Random().nextInt(21) + 5;

        // Debugging
        System.out.println("Bag Status Display Name: " + bagStatusDisplayName);
        System.out.println("Randomized Time to Next Step: " + randomizedTimeToNextStep);
        System.out.println("Flight: " + (reservation != null && !reservation.getFlights().isEmpty() ? reservation.getFlights().get(0) : "No flight found"));

        model.addAttribute("randomizedTimeToNextStep", randomizedTimeToNextStep);
        model.addAttribute("customer", customer);
        model.addAttribute("bags", bags);
        model.addAttribute("bagStatus", bagStatusDisplayName); // Use display name
        model.addAttribute("flight", reservation != null && !reservation.getFlights().isEmpty() ? reservation.getFlights().get(0) : null);

        return "trackbag";
    }

}
