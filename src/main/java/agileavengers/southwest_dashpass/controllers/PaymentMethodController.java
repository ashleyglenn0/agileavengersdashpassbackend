package agileavengers.southwest_dashpass.controllers;

import agileavengers.southwest_dashpass.dtos.PaymentDetailsDTO;
import agileavengers.southwest_dashpass.models.Customer;
import agileavengers.southwest_dashpass.models.PaymentDetails;
import agileavengers.southwest_dashpass.repository.PaymentDetailsRepository;
import agileavengers.southwest_dashpass.services.CustomerService;
import agileavengers.southwest_dashpass.services.PaymentDetailsService;
import agileavengers.southwest_dashpass.utils.EncryptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class PaymentMethodController {
    private final CustomerService customerService;
    private final PaymentDetailsService paymentDetailsService;
    private final EncryptionUtils encryptionUtils;
    private final PaymentDetailsRepository paymentDetailsRepository;

    @Autowired
    public PaymentMethodController(CustomerService customerService, PaymentDetailsService paymentDetailsService,
                                   EncryptionUtils encryptionUtils, PaymentDetailsRepository paymentDetailsRepository) {
        this.customerService = customerService;
        this.paymentDetailsService = paymentDetailsService;
        this.encryptionUtils = encryptionUtils;
        this.paymentDetailsRepository = paymentDetailsRepository;
    }

    @GetMapping("/customer/{customerID}/managepaymentmethods")
    public String showManagePaymentMethods(@PathVariable Long customerID, Model model) {
        // Retrieve the customer by ID
        Customer customer = customerService.findCustomerById(customerID);
        if (customer == null) {
            throw new IllegalArgumentException("Customer not found for ID: " + customerID);
        }

        // Retrieve and decrypt payment methods for the customer
        List<PaymentDetails> paymentMethods = paymentDetailsRepository.findPaymentDetailsByCustomerId(customerID);

        // Add a masked version of each card number for display
        List<String> maskedCardNumbers = paymentMethods.stream()
                .map(payment -> getMaskedCardNumber(payment.getEncryptedCardNumber()))
                .collect(Collectors.toList());

        model.addAttribute("customer", customer);
        model.addAttribute("paymentMethods", paymentMethods);
        model.addAttribute("maskedCardNumbers", maskedCardNumbers);
        return "managepaymentmethods";
    }

    @GetMapping("/customer/{customerID}/addpaymentmethod")
    public String showAddPaymentMethodPage(@PathVariable Long customerID, Model model) {
        // Retrieve the customer by ID
        Customer customer = customerService.findCustomerById(customerID);
        if (customer == null) {
            throw new IllegalArgumentException("Customer not found for ID: " + customerID);
        }

        model.addAttribute("customer", customer);
        model.addAttribute("paymentDetailsDTO", new PaymentDetailsDTO()); // Empty DTO for form binding

        return "addnewpaymentmethod"; // Thymeleaf template for adding a payment method
    }

    @PostMapping("/customer/{customerID}/savepaymentmethod")
    public String savePaymentMethod(
            @PathVariable Long customerID,
            @ModelAttribute("paymentDetailsDTO") PaymentDetailsDTO paymentDetailsDTO,
            Model model) {

        // Retrieve the customer by ID
        Customer customer = customerService.findCustomerById(customerID);
        if (customer == null) {
            throw new IllegalArgumentException("Customer not found for ID: " + customerID);
        }

        // Check if payment method already exists to avoid duplicates
        String encryptedCardNumber = encryptionUtils.encrypt(paymentDetailsDTO.getCardNumber());
        boolean paymentMethodExists = paymentDetailsRepository.existsByEncryptedCardNumber(encryptedCardNumber);

        if (paymentMethodExists) {
            model.addAttribute("errorMessage", "This payment method already exists.");
            model.addAttribute("customer", customer);
            model.addAttribute("paymentDetailsDTO", paymentDetailsDTO);
            return "addnewpaymentmethod"; // Return to form with error message
        }

        // Save the new payment method with encrypted details directly in the controller
        PaymentDetails paymentDetails = new PaymentDetails(
                customer,
                encryptedCardNumber,
                encryptionUtils.encrypt(paymentDetailsDTO.getExpirationDate()),
                encryptionUtils.encrypt(paymentDetailsDTO.getCvv()),
                encryptionUtils.encrypt(paymentDetailsDTO.getZipCode()),
                encryptionUtils.encrypt(paymentDetailsDTO.getCardName())
        );

        // Save paymentDetails directly
        paymentDetailsRepository.save(paymentDetails);
        System.out.println("Payment details saved with ID: " + paymentDetails.getId());

        // Redirect to manage payment methods page after successful save
        return "redirect:/customer/" + customerID + "/managepaymentmethods";
    }


    @PostMapping("/customer/{customerID}/deletepaymentmethod/{id}")
    public String deletePaymentMethod(@PathVariable Long customerID, @PathVariable Long id) {
        paymentDetailsService.deletePaymentMethodById(id);

        // Redirect back to manage payment methods page after deletion
        return "redirect:/customer/" + customerID + "/managepaymentmethods";
    }


    private String getMaskedCardNumber(String encryptedCardNumber) {
        // Decrypt the card number first
        String decryptedCardNumber = encryptionUtils.decrypt(encryptedCardNumber);

        // Ensure the decrypted card number is valid for masking
        if (decryptedCardNumber != null && decryptedCardNumber.length() >= 4) {
            // Return masked format showing only the last 4 digits
            return "**** **** **** " + decryptedCardNumber.substring(decryptedCardNumber.length() - 4);
        }
        return "****"; // Fallback if the card number is null or too short
    }
}
