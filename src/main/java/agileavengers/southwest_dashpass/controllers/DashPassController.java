package agileavengers.southwest_dashpass.controllers;

import agileavengers.southwest_dashpass.dtos.PaymentDetailsDTO;
import agileavengers.southwest_dashpass.exceptions.PaymentFailedException;
import agileavengers.southwest_dashpass.models.*;
import agileavengers.southwest_dashpass.services.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Controller
public class DashPassController {
    private final CustomerService customerService;
    private final ReservationService reservationService;
    private final DashPassService dashPassService;
    private final PaymentService paymentService;
    private final PaymentDetailsService paymentDetailsService;

    // Constructor Injection
    @Autowired
    public DashPassController(CustomerService customerService, ReservationService reservationService,
                              DashPassService dashPassService, PaymentService paymentService,
                              PaymentDetailsService paymentDetailsService) {
        this.customerService = customerService;
        this.reservationService = reservationService;
        this.dashPassService = dashPassService;
        this.paymentService = paymentService;
        this.paymentDetailsService = paymentDetailsService;
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

    @PostMapping("/customer/{customerID}/purchasedashpass")
    public String reviewDashPassOrder(@PathVariable Long customerID,
                                      @RequestParam(value = "selectedReservationId", required = false) Long reservationId,
                                      @RequestParam("dashPassQuantity") int dashPassQuantity,
                                      Model model) {
        // Fetch customer
        Customer customer = customerService.findCustomerById(customerID);
        if (customer == null) {
            throw new RuntimeException("Customer not found.");
        }

        if (customer.getUser() == null) {
            throw new RuntimeException("User information for this customer is missing.");
        }

        Reservation reservation = null;
        if (reservationId != null) {
            reservation = reservationService.findById(reservationId);
        }

        // Calculate the total price for the dash pass(es)
        double totalPrice = dashPassQuantity * 50.0;

        // Add attributes to the model
        model.addAttribute("customer", customer);
        model.addAttribute("dashPassQuantity", dashPassQuantity);
        model.addAttribute("totalPrice", totalPrice);
        model.addAttribute("reservation", reservation);

        return "dashpassrevieworder"; // Return to the review order page
    }


    @GetMapping("/customer/{customerID}/dashpasspaymentmethoddetails")
    public String showDashPassPaymentMethodDetails(@PathVariable Long customerID,
                                                   @RequestParam("dashPassQuantity") int dashPassQuantity,
                                                   @RequestParam(value = "selectedReservationId", required = false) Long reservationId,
                                                   Model model) {
        // Fetch customer data
        Customer customer = customerService.findCustomerById(customerID);
        if (customer == null) {
            throw new RuntimeException("Customer not found.");
        }

        if (customer.getUser() == null) {
            throw new RuntimeException("User information for this customer is missing.");
        }

        PaymentDetailsDTO paymentDetailsDTO = new PaymentDetailsDTO(); // Create an instance of your DTO
        model.addAttribute("paymentDetailsDTO", paymentDetailsDTO);

        // Fetch reservation if available
        Reservation reservation = null;
        if (reservationId != null) {
            reservation = reservationService.findById(reservationId);
        }

        // Calculate total price for DashPass purchase
        double totalPrice = dashPassQuantity * 50.0;  // Assuming $50 per DashPass

        // Add customer, reservation, and calculated price to the model
        model.addAttribute("customer", customer);
        model.addAttribute("reservation", reservation); // Add reservation, null if not available
        model.addAttribute("dashPassQuantity", dashPassQuantity);
        model.addAttribute("totalPrice", totalPrice);
        model.addAttribute("userSelectedStatus", "PAID"); // Default value for the form

        System.out.println("Customer: " + customer);
        System.out.println("User: " + (customer != null ? customer.getUser() : "No user associated"));

        // Return the DashPass payment method details view
        return "dashpasspaymentmethoddetails";
    }


    @PostMapping("/customer/{customerID}/dashpasspaymentmethoddetails")
    public String completeDashPassPurchase(@PathVariable Long customerID,
                                           @RequestParam("dashPassQuantity") int dashPassQuantity,
                                           @RequestParam(value = "selectedReservationId", required = false) Long reservationId,
                                           @RequestParam("totalPrice") double totalPrice,
                                           @RequestParam("userSelectedStatus") String userSelectedStatus,
                                           @Valid @ModelAttribute("paymentDetailsDTO") PaymentDetailsDTO paymentDetailsDTO,
                                           BindingResult bindingResult,
                                           Model model) throws InterruptedException, ExecutionException {

        // Check for validation errors in the form
        if (bindingResult.hasErrors()) {
            return handleDashPassPaymentError(model, customerID, reservationId, dashPassQuantity, totalPrice, userSelectedStatus);
        }

        try {
            Customer customer = customerService.findCustomerById(customerID);

            if (customer == null) {
                throw new IllegalStateException("Customer not found for ID: " + customerID);
            }

            // Process the payment asynchronously
            CompletableFuture<PaymentStatus> futurePaymentStatus = paymentService.processPaymentAsync(paymentDetailsDTO, userSelectedStatus);
            PaymentStatus paymentStatus = futurePaymentStatus.get();

            // If payment is successful, proceed to complete purchase
            if (paymentStatus == PaymentStatus.PAID) {
                Reservation reservation = reservationId != null ? reservationService.findById(reservationId) : null;

                String confirmationNumber = dashPassService.purchaseDashPass(customer, reservation, dashPassQuantity, paymentStatus);

                // Redirect to confirmation page with required parameters
                return "redirect:/customer/" + customerID + "/dashpasspurchasecomplete?confirmationNumber=" + confirmationNumber +
                        "&dashPassQuantity=" + dashPassQuantity + "&totalPrice=" + totalPrice;
            } else {
                // If payment fails, handle the error
                model.addAttribute("errorMessage", "Payment failed. Please try again.");
                return handleDashPassPaymentError(model, customerID, reservationId, dashPassQuantity, totalPrice, userSelectedStatus);
            }

        } catch (ExecutionException | InterruptedException e) {
            // Handle general exceptions or failed payment process
            model.addAttribute("errorMessage", "Payment processing error. Please try again.");
            return handleDashPassPaymentError(model, customerID, reservationId, dashPassQuantity, totalPrice, userSelectedStatus);
        }
    }

    // Helper method to handle payment errors and add the required attributes
    private String handleDashPassPaymentError(Model model, Long customerID, Long reservationId,
                                              int dashPassQuantity, double totalPrice, String userSelectedStatus) {
        Customer customer = customerService.findCustomerById(customerID);
        model.addAttribute("customer", customer);

        Reservation reservation = reservationId != null ? reservationService.findById(reservationId) : null;
        model.addAttribute("reservation", reservation);

        model.addAttribute("dashPassQuantity", dashPassQuantity);
        model.addAttribute("totalPrice", totalPrice);
        model.addAttribute("userSelectedStatus", userSelectedStatus);

        return "dashpasspaymentmethoddetails";
    }



    @GetMapping("/customer/{customerID}/dashpasspurchasecomplete")
    public String showDashPassPurchaseComplete(@PathVariable Long customerID,
                                               @RequestParam("confirmationNumber") String confirmationNumber,
                                               @RequestParam("dashPassQuantity") int dashPassQuantity,
                                               @RequestParam("totalPrice") double totalPrice,
                                               Model model) {
        // Fetch customer data to display on the confirmation page
        Customer customer = customerService.findCustomerById(customerID);
        if (customer == null) {
            throw new RuntimeException("Customer not found. Inside purchase complete");
        }

        if (customer.getUser() == null) {
            throw new RuntimeException("User information for this customer is missing. Inside Purchase Complete");
        }

        model.addAttribute("customer", customer);
        model.addAttribute("confirmationNumber", confirmationNumber);
        model.addAttribute("dashPassQuantity", dashPassQuantity);
        model.addAttribute("totalPrice", totalPrice);

        System.out.println("Customer: " + customer);
        System.out.println("User: " + (customer != null ? customer.getUser() : "No user associated"));


        // Return the confirmation page view
        return "dashpasspurchasecomplete";
    }
}
