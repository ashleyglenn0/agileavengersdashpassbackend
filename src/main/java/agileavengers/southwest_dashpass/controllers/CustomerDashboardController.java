package agileavengers.southwest_dashpass.controllers;

import agileavengers.southwest_dashpass.models.*;
import agileavengers.southwest_dashpass.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.List;

@Controller
public class CustomerDashboardController {
    private final CustomerService customerService;
    private final ReservationService reservationService;
    private final DashPassReservationService dashPassReservationService;
    private final SupportRequestService supportRequestService;
    private final EmployeeService employeeService;

    // Constructor Injection
    @Autowired
    public CustomerDashboardController(CustomerService customerService, ReservationService reservationService,
                                       DashPassReservationService dashPassReservationService, SupportRequestService supportRequestService,
                                       EmployeeService employeeService) {
        this.customerService = customerService;
        this.reservationService = reservationService;
        this.dashPassReservationService = dashPassReservationService;
        this.supportRequestService = supportRequestService;
        this.employeeService = employeeService;
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


}
