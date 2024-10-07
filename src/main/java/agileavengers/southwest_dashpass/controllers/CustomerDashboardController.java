package agileavengers.southwest_dashpass.controllers;

import agileavengers.southwest_dashpass.models.Customer;
import agileavengers.southwest_dashpass.services.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.security.Principal;

@Controller
public class CustomerDashboardController {
    private final CustomerService customerService;

    // Constructor Injection
    @Autowired
    public CustomerDashboardController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping("/customer/{customerID}/customerdashboard")
    public String showCustomerDashboard(@PathVariable Long customerID, Model model, Principal principal) {
        // Fetch customer by ID
        Customer customer = customerService.findCustomerById(customerID);

        // If customer is not found, show a custom error page
        if (customer == null) {
            model.addAttribute("error", "Customer not found with ID: " + customerID);
            return "error/customer-not-found";  // You should have a custom error page
        }

        // Ensure the customer belongs to the currently logged-in user
        if (!customer.getUser().getUsername().equals(principal.getName())) {
            return "error/403";  // Show a 403 error page if access is unauthorized
        }

        // Add customer information to the model
        model.addAttribute("customer", customer);
        return "customerdashboard";  // Return the dashboard view
    }

}
