package agileavengers.southwest_dashpass.controllers;

import agileavengers.southwest_dashpass.dtos.PaymentDetailsDTO;
import agileavengers.southwest_dashpass.models.Customer;
import agileavengers.southwest_dashpass.models.PaymentDetails;
import agileavengers.southwest_dashpass.services.CustomerService;
import agileavengers.southwest_dashpass.services.PaymentDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
public class PaymentMethodController {
    private final CustomerService customerService;
    private final PaymentDetailsService paymentDetailsService;

    @Autowired
    public PaymentMethodController(CustomerService customerService, PaymentDetailsService paymentDetailsService){
        this.customerService = customerService;
        this.paymentDetailsService = paymentDetailsService;
    }

    @GetMapping("/customer/{customerID}/managepaymentmethods")
    public String showManagePaymentMethods(@PathVariable Long customerID, Model model) {
        // Retrieve the customer by ID
        Customer customer = customerService.findCustomerById(customerID);
        if (customer == null) {
            throw new IllegalArgumentException("Customer not found for ID: " + customerID);
        }

        // Retrieve payment methods for the customer
        List<PaymentDetails> paymentMethods = paymentDetailsService.findPaymentMethodsByCustomer(customerID);

        // Add attributes to the model
        model.addAttribute("customer", customer);
        model.addAttribute("paymentMethods", paymentMethods);

        return "managepaymentmethods"; // The name of the Thymeleaf template
    }

    @GetMapping("/customer/{customerID}/addpaymentmethod")
    public String showAddPaymentMethodPage(@PathVariable Long customerID, Model model) {
        // Retrieve the customer by ID
        Customer customer = customerService.findCustomerById(customerID);
        if (customer == null) {
            throw new IllegalArgumentException("Customer not found for ID: " + customerID);
        }

        // Optional: Retrieve existing payment methods for display or duplicate checking
        List<PaymentDetails> existingPaymentMethods = paymentDetailsService.findPaymentMethodsByCustomer(customerID);

        // Add attributes to the model
        model.addAttribute("customer", customer);
        model.addAttribute("paymentDetailsDTO", new PaymentDetailsDTO()); // Empty DTO for form binding
        model.addAttribute("existingPaymentMethods", existingPaymentMethods); // Optional

        return "addnewpaymentmethod"; // Thymeleaf template for adding a payment method
    }

}
