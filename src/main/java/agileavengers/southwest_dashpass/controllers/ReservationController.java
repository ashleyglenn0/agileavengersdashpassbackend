package agileavengers.southwest_dashpass.controllers;

import agileavengers.southwest_dashpass.models.Customer;
import agileavengers.southwest_dashpass.models.Reservation;
import agileavengers.southwest_dashpass.services.CustomerService;
import agileavengers.southwest_dashpass.services.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
public class ReservationController {
    private final CustomerService customerService;
    private final ReservationService reservationService;

    @Autowired
    public ReservationController(CustomerService customerService, ReservationService reservationService){
        this.customerService = customerService;
        this.reservationService = reservationService;

    }
    @GetMapping("/customer/{customerID}/reservations")
    public String getAllReservations(@PathVariable Long customerID, Model model) {
        System.out.println("Inside ReservationController");
        System.out.println("CustomerID: " + customerID);

        // Retrieve customer details
        Customer customer = customerService.findCustomerById(customerID);
        System.out.println("Customer: " + customer);

        // Get all reservations for the customer
        List<Reservation> reservations = reservationService.getReservationsForCustomer(customerID);
        System.out.println("Reservations: " + reservations);

        // Add reservations and customer to the model
        model.addAttribute("customer", customer);
        model.addAttribute("reservations", reservations);

        // Return the template for viewing all reservations
        return "reservationlist";
    }

    @GetMapping("/customer/{customerID}/reservation/{reservationId}")
    public String viewReservationDetails(@PathVariable Long customerID, @PathVariable Long reservationId, Model model) {
        // Get the reservation details by ID
        Customer customer = customerService.findCustomerById(customerID);
        Reservation reservation = reservationService.findById(reservationId);

        // Add the reservation and customer to the model
        model.addAttribute("customer", customer);
        model.addAttribute("reservation", reservation);

        // Return the reservation details template
        return "reservationdetails";
    }

    @PostMapping("/customer/{customerID}/reservation/deletereservation/{id}")
    public String deleteReservation(@PathVariable Long customerID, @PathVariable Long id) {
        reservationService.deleteReservation(id);

        // Redirect back to manage payment methods page after deletion
        return "reservationlist";
    }

}
