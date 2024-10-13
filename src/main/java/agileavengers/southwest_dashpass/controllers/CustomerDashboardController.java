package agileavengers.southwest_dashpass.controllers;

import agileavengers.southwest_dashpass.models.Customer;
import agileavengers.southwest_dashpass.models.DashPass;
import agileavengers.southwest_dashpass.models.Reservation;
import agileavengers.southwest_dashpass.services.CustomerService;
import agileavengers.southwest_dashpass.services.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.security.Principal;
import java.util.List;

@Controller
public class CustomerDashboardController {
    private final CustomerService customerService;
    private final ReservationService reservationService;

    // Constructor Injection
    @Autowired
    public CustomerDashboardController(CustomerService customerService, ReservationService reservationService) {
        this.customerService = customerService;
        this.reservationService = reservationService;
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

        Integer numberOfDashPassesInUse = customer.getNumberOfDashPassesUsed();
        Integer numberOfDashPassesAvailableForPurchase = customer.getNumberOfDashPassesAvailableForPurchase();
        Integer numberOfDashPassesAvailableToAddToReservation = customer.getNumberOfDashPasses();
        Integer totalNumberOfDashPassesOwned = customer.getTotalDashPassesCustomerHas();

//        System.out.println("Upcoming Flight: " + (upcomingFlight != null ? upcomingFlight.getFlightDepartureDate() : "None"));
        for (Reservation reservation : upcomingFlight) {
            System.out.println("Reservation ID: " + reservation.getReservationId());
            System.out.println("Flights: " + reservation.getFlights());  // Check if flights are populated
        }


        // Add customer information to the model
        model.addAttribute("customer", customer);
        model.addAttribute("numberOfDashPassesInUse", numberOfDashPassesInUse);
        model.addAttribute("numberOfDashPassesAvailableForPurchase", numberOfDashPassesAvailableForPurchase);
        model.addAttribute("numberOfDashPassesAvailableToAddToReservation", numberOfDashPassesAvailableToAddToReservation);
        model.addAttribute("totalNumberOfDashPassesOwned", totalNumberOfDashPassesOwned);
        model.addAttribute("upcomingFlight", upcomingFlight);


        return "customerdashboard";  // Return the dashboard view
    }

}
