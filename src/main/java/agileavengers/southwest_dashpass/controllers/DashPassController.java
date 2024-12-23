package agileavengers.southwest_dashpass.controllers;

import agileavengers.southwest_dashpass.dtos.DisplayPaymentDetailsDTO;
import agileavengers.southwest_dashpass.dtos.PaymentDetailsDTO;
import agileavengers.southwest_dashpass.exceptions.PaymentFailedException;
import agileavengers.southwest_dashpass.models.*;
import agileavengers.southwest_dashpass.repository.PaymentDetailsRepository;
import agileavengers.southwest_dashpass.services.*;
import agileavengers.southwest_dashpass.utils.EncryptionUtils;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
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
    private final DashPassReservationService dashPassReservationService;
    private final EncryptionUtils encryptionUtils;
    private final PaymentDetailsRepository paymentDetailsRepository;

    // Constructor Injection
    @Autowired
    public DashPassController(CustomerService customerService, ReservationService reservationService,
                              DashPassService dashPassService, PaymentService paymentService,
                              PaymentDetailsService paymentDetailsService,
                              DashPassReservationService dashPassReservationService,
                              EncryptionUtils encryptionUtils, PaymentDetailsRepository paymentDetailsRepository) {
        this.customerService = customerService;
        this.reservationService = reservationService;
        this.dashPassService = dashPassService;
        this.paymentService = paymentService;
        this.paymentDetailsService = paymentDetailsService;
        this.dashPassReservationService = dashPassReservationService;
        this.encryptionUtils = encryptionUtils;
        this.paymentDetailsRepository = paymentDetailsRepository;
    }

    //Customer DashPass Methods

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

        // Check if the customer has reached the maximum number of DashPasses
        if (customer.getTotalDashPassCount() >= customer.getMaxDashPasses()) {
            model.addAttribute("errorMessage", "You have reached the maximum number of DashPasses allowed.");
            model.addAttribute("customer", customer);  // Add customer info to show on the purchase page if needed
            // Re-display the purchase form with the error message
            return "purchasedashpass";
        }

        Reservation reservation = null;
        if (reservationId != null) {
            reservation = reservationService.findById(reservationId);
            System.out.println("Reservation ID: " + (reservation != null ? reservation.getReservationId() : "No Reservation"));
            System.out.println("Flight Departure Date: " + (reservation != null ? reservation.getFlightDepartureDate() : "No Date"));
            System.out.println("Airport Code: " + (reservation != null ? reservation.getAirportCode() : "No Code"));

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

        List<DisplayPaymentDetailsDTO> savedPaymentMethods = paymentDetailsService.getDisplayPaymentDetails(customerID);

        // Add customer, reservation, and calculated price to the model
        model.addAttribute("customer", customer);
        model.addAttribute("reservation", reservation); // Add reservation, null if not available
        model.addAttribute("dashPassQuantity", dashPassQuantity);
        model.addAttribute("totalPrice", totalPrice);
        model.addAttribute("userSelectedStatus", "PAID"); // Default value for the form
        model.addAttribute("paymentMethods", savedPaymentMethods);

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
                                           @RequestParam(value = "selectedPaymentMethodId", required = false) String selectedPaymentMethodId,
                                           @RequestParam(value = "savePaymentDetails", required = false, defaultValue = "false") boolean savePaymentDetails,
                                           @Valid @ModelAttribute("paymentDetailsDTO") PaymentDetailsDTO paymentDetailsDTO,
                                           BindingResult bindingResult,
                                           Model model) throws InterruptedException, ExecutionException {

        System.out.println("Entering completeDashPassPurchase for customer: " + customerID + " at " + LocalDateTime.now());
        System.out.println("Processing DashPass purchase for customer: " + customerID + " at " + LocalDateTime.now());
        System.out.println("DashPass Quantity: " + dashPassQuantity);
        System.out.println("Selected Reservation ID: " + reservationId);

        // Check for validation errors
        if ("new".equals(selectedPaymentMethodId) && bindingResult.hasErrors()) {
            System.out.println("Validation errors detected in payment details.");
            return handleDashPassPaymentError(model, customerID, reservationId, dashPassQuantity, totalPrice, userSelectedStatus);
        }

        try {
            Customer customer = customerService.findCustomerById(customerID);
            if (customer == null) {
                throw new IllegalStateException("Customer not found for ID: " + customerID);
            }

            System.out.println("Customer Retrieved: " + customer.getId());
            System.out.println("Total DashPass Count Before Purchase: " + customer.getTotalDashPassCount());

            // Determine which payment details to use
            PaymentDetailsDTO paymentDetailsToUse;
            if (selectedPaymentMethodId != null && !"new".equals(selectedPaymentMethodId)) {
                Long paymentMethodId = Long.parseLong(selectedPaymentMethodId);
                paymentDetailsToUse = paymentDetailsService.findPaymentDetailsById(paymentMethodId);
                if (paymentDetailsToUse == null) {
                    model.addAttribute("errorMessage", "Selected payment method not found.");
                    return "paymentmethoddetails";
                }
                System.out.println("Using saved payment method with ID: " + paymentMethodId);
            } else {
                paymentDetailsToUse = paymentDetailsDTO;
                if (savePaymentDetails) {
                    PaymentDetails newPaymentDetails = new PaymentDetails(
                            customer,
                            encryptionUtils.encrypt(paymentDetailsDTO.getCardNumber()),
                            encryptionUtils.encrypt(paymentDetailsDTO.getExpirationDate()),
                            encryptionUtils.encrypt(paymentDetailsDTO.getCvv()),
                            encryptionUtils.encrypt(paymentDetailsDTO.getZipCode()),
                            encryptionUtils.encrypt(paymentDetailsDTO.getCardName())
                    );
                    paymentDetailsRepository.save(newPaymentDetails);
                    System.out.println("New payment details saved for customer: " + customerID);
                }
            }

            // Process payment asynchronously
            System.out.println("Processing payment...");
            CompletableFuture<PaymentStatus> futurePaymentStatus = paymentService.processPaymentAsync(paymentDetailsToUse, userSelectedStatus);
            PaymentStatus paymentStatus = futurePaymentStatus.get();

            if (paymentStatus == PaymentStatus.PAID) {
                Reservation reservation = reservationId != null ? reservationService.findById(reservationId) : null;

                System.out.println("Payment successful. Proceeding with DashPass purchase.");
                System.out.println("Reservation ID: " + (reservation != null ? reservation.getReservationId() : "No Reservation"));

                // Call purchaseDashPass and log the confirmation number
                String confirmationNumber = dashPassService.purchaseDashPass(customer, reservation, dashPassQuantity, paymentStatus, null);
                System.out.println("DashPass purchase completed. Confirmation Number: " + confirmationNumber);

                return "redirect:/customer/" + customerID + "/dashpasspurchasecomplete?confirmationNumber=" + confirmationNumber +
                        "&dashPassQuantity=" + dashPassQuantity + "&totalPrice=" + totalPrice;
            } else {
                System.out.println("Payment failed. Returning to payment method details page.");
                model.addAttribute("errorMessage", "Payment failed. Please try again.");
                return handleDashPassPaymentError(model, customerID, reservationId, dashPassQuantity, totalPrice, userSelectedStatus);
            }

        } catch (Exception e) {
            System.out.println("Error during DashPass purchase: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("errorMessage", "An error occurred while processing your DashPass purchase.");
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

    @GetMapping("/customer/{customerId}/redeem")
    public String showRedeemDashPassPage(@PathVariable Long customerId, Model model) {
        // Retrieve the customer details
        Customer customer = customerService.findCustomerById(customerId);

        // Retrieve reservations that don't have a DashPass attached yet
        List<Reservation> availableReservations = reservationService.findReservationsWithoutDashPass(customerId);

        System.out.println("Reservations without DashPass: " + availableReservations.size());
        for (Reservation res : availableReservations) {
            System.out.println("Reservation ID: " + res.getReservationId() + " Flights: " + res.getFlights().size());
        }

        // Add customer and reservations to the model
        model.addAttribute("customer", customer);
        model.addAttribute("reservationsWithoutDashPass", availableReservations);

        return "redeemdashpass";
    }

    @PostMapping("/customer/{customerId}/redeemDashPass")
    public String redeemDashPass(
            @PathVariable Long customerId,
            @RequestParam Long reservationId,
            @RequestParam Long dashPassId,
            Model model) {

        try {
            // Find the customer, reservation, and dashpass
            Customer customer = customerService.findCustomerById(customerId);
            model.addAttribute("customer", customer);
            Reservation reservation = reservationService.findById(reservationId);
            DashPass dashPass = dashPassService.findDashPassById(dashPassId);

            // Redeem the DashPass
            dashPassReservationService.addDashPassToExistingFlightReservation(customer, reservation, dashPass, null);

            // Set success alert in model
            model.addAttribute("dashPassAdded", true);
        } catch (Exception e) {
            // Set failure alert in model
            model.addAttribute("dashPassAdded", false);
            model.addAttribute("errorMessage", "Failed to redeem DashPass: " + e.getMessage());
        }

        // Reload the Redeem DashPass page with updated model
        return "redeemdashpass";
    }
}