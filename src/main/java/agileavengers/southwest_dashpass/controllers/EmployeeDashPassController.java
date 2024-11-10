package agileavengers.southwest_dashpass.controllers;

import agileavengers.southwest_dashpass.dtos.DisplayPaymentDetailsDTO;
import agileavengers.southwest_dashpass.dtos.PaymentDetailsDTO;
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

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Controller
public class EmployeeDashPassController {

    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private CustomerService customerService;
    @Autowired
    private DashPassService dashPassService;
    @Autowired
    private ReservationService reservationService;
    @Autowired
    private PaymentService paymentService;
    @Autowired
    private PaymentDetailsService paymentDetailsService;
    @Autowired
    private DashPassReservationService dashPassReservationService;
    @Autowired
    private EncryptionUtils encryptionUtils;
   @Autowired
    private PaymentDetailsRepository paymentDetailsRepository;

    @GetMapping("/employee/{employeeId}/verifydashpass")
    public String showVerifyDashPass(@PathVariable Long employeeId, Model model) {
        Employee employee = employeeService.findEmployeeById(employeeId);
        model.addAttribute("employee", employee);
        return "verifydashpass";
    }

    @GetMapping("/employee/{employeeId}/searchDashPass")
    public String searchDashPass(@PathVariable Long employeeId,
                                 @RequestParam(required = false) String customerName,
                                 @RequestParam(required = false) Long dashPassId,
                                 Model model) {
        // Fetch the employee for authorization/logging purposes
        Employee employee = employeeService.findEmployeeById(employeeId);
        model.addAttribute("employee", employee);

        // Flag to indicate if a search was performed
        boolean searchPerformed = false;

        // Search by DashPass ID if provided
        if (dashPassId != null) {
            DashPass dashPass = dashPassService.findDashPassByIdWithCustomerUserAndReservation(dashPassId);
            model.addAttribute("dashPass", dashPass);
            searchPerformed = true;
        }
        // Otherwise, search by customer name if provided
        else if (customerName != null && !customerName.isEmpty()) {
            List<Customer> customers = customerService.findCustomersByName(customerName);
            model.addAttribute("customers", customers);
            searchPerformed = true;
        }

        // Add the searchPerformed flag to the model
        model.addAttribute("searchPerformed", searchPerformed);

        return "verifydashpass"; // Return the same view with the results added to the model
    }

    @GetMapping("/employee/{employeeId}/searchCustomer")
    public String searchCustomer(@PathVariable Long employeeId,
                                 @RequestParam("name") String name,
                                 Model model) {
        System.out.println("Received search request from employee ID: " + employeeId + " with name: " + name);

        Employee employee = employeeService.findEmployeeById(employeeId);
        model.addAttribute("employee", employee);

        List<Customer> customers = customerService.findCustomersByName(name);
        System.out.println("Customers passed to the view: " + customers.size());

        if (customers.isEmpty()) {
            System.out.println("No customers found.");
        } else {
            customers.forEach(customer -> System.out.println("Customer passed to view: " + customer.getUser().getFirstName() + " " + customer.getUser().getLastName()));
        }

        model.addAttribute("customers", customers);
        model.addAttribute("searchPerformed", !customers.isEmpty());

        return "verifydashpass"; // This should be your view name
    }

    @GetMapping("/employee/{employeeId}/customer/{customerId}/ticket/{reservationId}")
    public String viewTicket(@PathVariable Long employeeId, @PathVariable Long customerId, @PathVariable Long reservationId, Model model) {
        // Fetch the customer, employee, and reservation
        Customer customer = customerService.findCustomerById(customerId);
        Employee employee = employeeService.findEmployeeById(employeeId);
        Reservation reservation = reservationService.findById(reservationId);

        // Add the reservation and customer details to the model
        model.addAttribute("reservation", reservation);
        model.addAttribute("dashPassReservations", reservation.getDashPassReservations());
        model.addAttribute("customer", customer);
        model.addAttribute("employee", employee);

        // Set user role to determine if Validate button should display
        Role userRole = employee.getRole(); // Assuming Role is an enum or class representing user roles
        model.addAttribute("userRole", userRole);

        return "ticket";
    }


    @GetMapping("/employee/{employeeId}/customer/{customerId}/purchaseDashPass")
    public String employeeInitiatedPurchaseDashPass(
            @PathVariable Long employeeId,
            @PathVariable Long customerId,
            Model model
    ) {
        Customer customer = customerService.findCustomerById(customerId);
        Employee employee = employeeService.findEmployeeById(employeeId);

        if (customer == null || employee == null) {
            throw new IllegalArgumentException("Customer or Employee not found.");
        }

        model.addAttribute("customer", customer);
        model.addAttribute("employee", employee); // Pass the employee to the view
        return "purchaseDashPass"; // View name for the DashPass purchase page
    }

    @PostMapping("/employee/{employeeId}/customer/{customerID}/purchasedashpass")
    public String reviewDashPassOrder(@PathVariable Long customerID,
                                      @PathVariable Long employeeId,
                                      @RequestParam(value = "selectedReservationId", required = false) Long reservationId,
                                      @RequestParam("dashPassQuantity") int dashPassQuantity,
                                      Model model) {
        // Fetch customer
        Customer customer = customerService.findCustomerById(customerID);
        Employee employee = employeeService.findEmployeeById(employeeId);

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
        model.addAttribute("employee", employee);
        model.addAttribute("customer", customer);
        model.addAttribute("dashPassQuantity", dashPassQuantity);
        model.addAttribute("totalPrice", totalPrice);
        model.addAttribute("reservation", reservation);

        return "dashpassrevieworder"; // Return to the review order page
    }


    @GetMapping("/employee/{employeeId}/customer/{customerID}/dashpasspaymentmethoddetails")
    public String showDashPassPaymentMethodDetails(@PathVariable Long customerID,
                                                   @PathVariable Long employeeId,
                                                   @RequestParam("dashPassQuantity") int dashPassQuantity,
                                                   @RequestParam(value = "selectedReservationId", required = false) Long reservationId,
                                                   Model model) {
        // Fetch customer data
        Customer customer = customerService.findCustomerById(customerID);
        Employee employee = employeeService.findEmployeeById(employeeId);
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
        model.addAttribute("employee", employee);
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


    @PostMapping("/employee/{employeeId}/customer/{customerID}/dashpasspaymentmethoddetails")
    public String completeDashPassPurchase(@PathVariable Long customerID,
                                           @PathVariable Long employeeId,
                                           @RequestParam("dashPassQuantity") int dashPassQuantity,
                                           @RequestParam(value = "selectedReservationId", required = false) Long reservationId,
                                           @RequestParam("totalPrice") double totalPrice,
                                           @RequestParam("userSelectedStatus") String userSelectedStatus,
                                           @RequestParam(value = "selectedPaymentMethodId", required = false) String selectedPaymentMethodId,
                                           @RequestParam(value = "savePaymentDetails", required = false, defaultValue = "false") boolean savePaymentDetails,
                                           @Valid @ModelAttribute("paymentDetailsDTO") PaymentDetailsDTO paymentDetailsDTO,
                                           BindingResult bindingResult,
                                           Model model) throws InterruptedException, ExecutionException {

        // Check for validation errors only if using new payment details
        if ("new".equals(selectedPaymentMethodId) && bindingResult.hasErrors()) {
            return handleDashPassPaymentError(model, customerID, employeeId, reservationId, dashPassQuantity, totalPrice, userSelectedStatus);
        }

        try {
            Customer customer = customerService.findCustomerById(customerID);
            Employee employee = employeeService.findEmployeeById(employeeId);
            if (customer == null) {
                throw new IllegalStateException("Customer not found for ID: " + customerID);
            }

            // Determine which payment details to use
            PaymentDetailsDTO paymentDetailsToUse;
            if (selectedPaymentMethodId != null && !"new".equals(selectedPaymentMethodId)) {
                // Use the saved payment method, convert ID to Long
                Long paymentMethodId = Long.parseLong(selectedPaymentMethodId);
                paymentDetailsToUse = paymentDetailsService.findPaymentDetailsById(paymentMethodId);
                if (paymentDetailsToUse == null) {
                    model.addAttribute("errorMessage", "Selected payment method not found.");
                    return "paymentmethoddetails";
                }
            } else {
                // Use new payment details from paymentDetailsDTO
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
                }
            }

            // Process the payment asynchronously with the correct payment details
            CompletableFuture<PaymentStatus> futurePaymentStatus = paymentService.processPaymentAsync(paymentDetailsToUse, userSelectedStatus);
            PaymentStatus paymentStatus = futurePaymentStatus.get();

            // If payment is successful, proceed to complete purchase
            if (paymentStatus == PaymentStatus.PAID) {
                Reservation reservation = reservationId != null ? reservationService.findById(reservationId) : null;

                String confirmationNumber = dashPassService.purchaseDashPass(customer, reservation, dashPassQuantity, paymentStatus, employee);

                // Redirect to confirmation page with required parameters
                return "redirect:/employee/" + employeeId + "/customer/" + customerID + "/dashpasspurchasecomplete" +
                        "?confirmationNumber=" + confirmationNumber +
                        "&dashPassQuantity=" + dashPassQuantity +
                        "&totalPrice=" + totalPrice;
            } else {
                // If payment fails, handle the error
                model.addAttribute("errorMessage", "Payment failed. Please try again.");
                return handleDashPassPaymentError(model, customerID, employeeId, reservationId, dashPassQuantity, totalPrice, userSelectedStatus);
            }

        } catch (ExecutionException | InterruptedException e) {
            // Handle general exceptions or failed payment process
            model.addAttribute("errorMessage", "Payment processing error. Please try again.");
            return handleDashPassPaymentError(model, customerID, employeeId, reservationId, dashPassQuantity, totalPrice, userSelectedStatus);
        }
    }


    // Helper method to handle payment errors and add the required attributes
    private String handleDashPassPaymentError(Model model, Long customerID, Long employeeId, Long reservationId,
                                              int dashPassQuantity, double totalPrice, String userSelectedStatus) {
        Customer customer = customerService.findCustomerById(customerID);
        Employee employee = employeeService.findEmployeeById(employeeId);

        model.addAttribute("customer", customer);
        model.addAttribute("employee", employee);

        Reservation reservation = reservationId != null ? reservationService.findById(reservationId) : null;
        model.addAttribute("reservation", reservation);

        model.addAttribute("dashPassQuantity", dashPassQuantity);
        model.addAttribute("totalPrice", totalPrice);
        model.addAttribute("userSelectedStatus", userSelectedStatus);

        return "dashpasspaymentmethoddetails";
    }



    @GetMapping("/employee/{employeeId}/customer/{customerID}/dashpasspurchasecomplete")
    public String showDashPassPurchaseComplete(@PathVariable Long customerID,
                                               @PathVariable Long employeeId,
                                               @RequestParam("confirmationNumber") String confirmationNumber,
                                               @RequestParam("dashPassQuantity") int dashPassQuantity,
                                               @RequestParam("totalPrice") double totalPrice,
                                               Model model) {
        // Fetch customer data to display on the confirmation page
        Customer customer = customerService.findCustomerById(customerID);
        Employee employee = employeeService.findEmployeeById(employeeId);
        if (customer == null) {
            throw new RuntimeException("Customer not found. Inside purchase complete");
        }

        if (customer.getUser() == null) {
            throw new RuntimeException("User information for this customer is missing. Inside Purchase Complete");
        }

        model.addAttribute("employee", employee);
        model.addAttribute("customer", customer);
        model.addAttribute("confirmationNumber", confirmationNumber);
        model.addAttribute("dashPassQuantity", dashPassQuantity);
        model.addAttribute("totalPrice", totalPrice);

        System.out.println("Customer: " + customer);
        System.out.println("User: " + (customer != null ? customer.getUser() : "No user associated"));


        // Return the confirmation page view
        return "dashpasspurchasecomplete";
    }

    @GetMapping("/employee/{employeeId}/customer/{customerId}/redeem")
    public String showRedeemDashPassPage(@PathVariable Long customerId, @PathVariable Long employeeId, Model model) {
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

    @PostMapping("/employee/{employeeId}/customer/{customerId}/redeemDashPass")
    public String redeemDashPass(
            @PathVariable Long customerId,
            @PathVariable Long employeeId,
            @RequestParam Long reservationId,
            @RequestParam Long dashPassId,
            Model model) {

        try {
            // Find the customer, reservation, and dashpass
            Employee employee = employeeService.findEmployeeById(employeeId);
            Customer customer = customerService.findCustomerById(customerId);
            Reservation reservation = reservationService.findById(reservationId);
            DashPass dashPass = dashPassService.findDashPassById(dashPassId);

            // Redeem the DashPass
            dashPassReservationService.redeemDashPass(customer, reservation, dashPass, employee);

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
