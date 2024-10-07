package agileavengers.southwest_dashpass.controllers;

import agileavengers.southwest_dashpass.models.Customer;
import agileavengers.southwest_dashpass.models.DashPass;
import agileavengers.southwest_dashpass.models.Reservation;
import agileavengers.southwest_dashpass.services.CustomerService;
import agileavengers.southwest_dashpass.services.DashPassService;
import agileavengers.southwest_dashpass.services.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
public class DashPassController {
    private final CustomerService customerService;
    private final ReservationService reservationService;
    private final DashPassService dashPassService;

    // Constructor Injection
    @Autowired
    public DashPassController(CustomerService customerService, ReservationService reservationService, DashPassService dashPassService) {
        this.customerService = customerService;
        this.reservationService = reservationService;
        this.dashPassService = dashPassService;
    }

    @GetMapping("/customer/{customerID}/purchasedashpass")
    public String showPurchaseDashPassForm(@PathVariable Long customerID, Model model) {
        // Retrieve the customer from the database
        Customer customer = customerService.findCustomerById(customerID);
        if (customer == null) {
            throw new RuntimeException("Customer not found");
        }

        // Add necessary attributes to the model
        model.addAttribute("customer", customer);
        model.addAttribute("dashPass", new DashPass());
        // Add any other necessary data, like available reservations
        List<Reservation> reservations = reservationService.getReservationsForCustomer(customerID);
        model.addAttribute("reservations", reservations);

        return "purchasedashpass";  // This should map to the Thymeleaf template 'purchasedashpass.html'
    }
}
