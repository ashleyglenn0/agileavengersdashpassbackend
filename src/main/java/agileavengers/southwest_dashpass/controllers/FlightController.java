package agileavengers.southwest_dashpass.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import agileavengers.southwest_dashpass.dtos.PaymentDetailsDTO;
import agileavengers.southwest_dashpass.exceptions.PaymentFailedException;
import agileavengers.southwest_dashpass.models.*;
import agileavengers.southwest_dashpass.services.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;


@Controller
public class FlightController {
    private static final Logger logger = LoggerFactory.getLogger(FlightController.class);
    private final CustomerService customerService;
    private final AirportService airportService;
    private final FlightService flightService;
    private final DashPassService dashPassService;
    private final DashPassReservationService dashPassReservationService;
    private final ReservationService reservationService;
    private final BookingService bookingService;
    private final PaymentDetailsService paymentDetailsService;
    private final UserService userService;

    @Autowired
    public FlightController(CustomerService customerService, AirportService airportService, FlightService flightService,
                            DashPassService dashPassService, DashPassReservationService dashPassReservationService,
                            BookingService bookingService, ReservationService reservationService,
                            PaymentDetailsService paymentDetailsService, UserService userService) {
        this.customerService = customerService;
        this.airportService = airportService;
        this.flightService = flightService;
        this.dashPassService = dashPassService;
        this.dashPassReservationService = dashPassReservationService;
        this.bookingService = bookingService;
        this.reservationService = reservationService;
        this.paymentDetailsService = paymentDetailsService;
        this.userService = userService;

    }

    public Customer getLoggedInCustomer() {
        // Get the currently authenticated Spring Security user
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // Check if the principal is an instance of the Spring Security user
        if (principal instanceof org.springframework.security.core.userdetails.User) {
            String username = ((org.springframework.security.core.userdetails.User) principal).getUsername();

            // Use the UserService to retrieve your custom User entity from the database
            User user = userService.findByUsername(username); // Assuming findByUsername method exists

            // Now retrieve the associated customer using the custom User entity
            return customerService.findByUser(user);
        } else {
            throw new RuntimeException("No authenticated user found");
        }
    }

    @GetMapping("/customer/{customerID}/purchaseflight")
    public String showPurchaseFlightForm(@PathVariable Long customerID, Model model) {
        Customer customer = customerService.findCustomerById(customerID);

        Flight flight = new Flight();
        DashPass dashPass = new DashPass();
        List<Airport> airports = airportService.getAllAirports();

        model.addAttribute("customer", customer);
        model.addAttribute("customerID", customerID);
        model.addAttribute("flight", flight);
        model.addAttribute("dashPass", dashPass);
        model.addAttribute("airports", airports);

        return "searchflight";
    }

    @GetMapping("/customer/{customerID}/searchFlightsByDate")
    public String searchFlightsByDate(
            @PathVariable Long customerID,
            @RequestParam("selectedDate") LocalDate selectedDate,
            @RequestParam("departureAirport") String departureAirport,
            @RequestParam("arrivalAirport") String arrivalAirport,
            @RequestParam("tripType") TripType tripType,
            @RequestParam(value = "returnDate", required = false) LocalDate returnDate,
            Model model) {

        // Find the customer by ID
        Customer customer = customerService.findCustomerById(customerID);

        // Search for flights based on provided parameters
        FlightSearchResponse flightSearchResponse = flightService.findFlights(
                departureAirport,
                arrivalAirport,
                selectedDate,
                returnDate,
                tripType,
                customer
        );

        // Log the flights found
        System.out.println("Outbound Flights: " + flightSearchResponse.getOutboundFlights());
        System.out.println("Return Flights: " + flightSearchResponse.getReturnFlights());

        // Add flight search results and customer to the model
        model.addAttribute("outboundFlights", flightSearchResponse.getOutboundFlights());
        model.addAttribute("returnFlights", flightSearchResponse.getReturnFlights());
        model.addAttribute("tripType", tripType);  // Pass tripType
        model.addAttribute("customer", customer);  // Add customer to the model

        return "flighttable";
    }


    @GetMapping("/flighttable")
    public String showFlightTable(Model model, @PathVariable Long customerID,
                                  @RequestParam("tripType") String tripType) {  // Add tripType as a request parameter
        Customer customer = customerService.findCustomerById(customerID);
        model.addAttribute("customer", customer);
        model.addAttribute("tripType", tripType);  // Add tripType to the model
        return "flighttable";
    }

    // GET mapping to show the review order page
    @GetMapping("/customer/{customerID}/revieworder")
    public String getReviewOrderPage(@PathVariable Long customerID,
                                     @RequestParam("outboundFlightId") Long outboundFlightId,
                                     @RequestParam(value = "returnFlightId", required = false) Long returnFlightId,
                                     @RequestParam("dashPassOption") String dashPassOption,
                                     @RequestParam("tripType") String tripType,
                                     Model model) {
        Customer customer = customerService.findCustomerById(customerID);
        Flight outboundFlight = flightService.findFlightById(outboundFlightId);
        Flight returnFlight = null;
        if (returnFlightId != null) {
            returnFlight = flightService.findFlightById(returnFlightId);
        }

        double totalPrice = outboundFlight.getPrice();
        if (returnFlight != null) {
            totalPrice += returnFlight.getPrice();
        }

        if ("new".equals(dashPassOption)) {
            totalPrice += 50.0;
        }

        model.addAttribute("customer", customer);
        model.addAttribute("outboundFlight", outboundFlight);
        model.addAttribute("returnFlight", returnFlight);
        model.addAttribute("dashPassOption", dashPassOption);
        model.addAttribute("tripType", tripType);
        model.addAttribute("totalPrice", totalPrice);

        return "revieworder"; // Updated to match the new template name
    }

    // POST mapping to handle the final flight purchase and create the reservation
    @PostMapping("/customer/{customerID}/revieworder")
    public String confirmFlight(@PathVariable Long customerID,
                                @RequestParam("outboundFlightId") Long outboundFlightId,
                                @RequestParam(value = "returnFlightId", required = false) Long returnFlightId,
                                @RequestParam("dashPassOption") String dashPassOption,
                                @RequestParam("tripType") String tripType,
                                Model model) {
        Customer customer = customerService.findCustomerById(customerID);
        Flight outboundFlight = flightService.findFlightById(outboundFlightId);
        Flight returnFlight = null;
        if (returnFlightId != null) {
            returnFlight = flightService.findFlightById(returnFlightId);
        }

        double totalPrice = outboundFlight.getPrice();
        if (returnFlight != null) {
            totalPrice += returnFlight.getPrice();
        }

        if ("new".equals(dashPassOption)) {
            totalPrice += 50.0;
        }

        // Redirect with the parameters added directly to the URL
        return "redirect:/customer/" + customerID + "/paymentmethoddetails"
                + "?outboundFlightId=" + outboundFlightId
                + (returnFlightId != null ? "&returnFlightId=" + returnFlightId : "")
                + "&dashPassOption=" + dashPassOption
                + "&tripType=" + tripType
                + "&totalPrice=" + totalPrice;
    }



    // GET mapping for payment method details page (adjust this as needed)
    @GetMapping("/customer/{customerID}/paymentmethoddetails")
    public String showPaymentMethodPage(@PathVariable Long customerID,
                                        @RequestParam("outboundFlightId") Long outboundFlightId,
                                        @RequestParam(value = "returnFlightId", required = false) Long returnFlightId,
                                        @RequestParam("dashPassOption") String dashPassOption,
                                        @RequestParam("tripType") String tripType,
                                        @RequestParam("totalPrice") double totalPrice,
                                        Model model) {

        Flight outboundFlight = flightService.findFlightById(outboundFlightId);
        Flight returnFlight = null;
        if (returnFlightId != null) {
            returnFlight = flightService.findFlightById(returnFlightId);
        }

        // If the customer is not in the model (in case of direct access or reload), fetch it.
        if (!model.containsAttribute("customer")) {
            Customer customer = customerService.findCustomerById(customerID);
            model.addAttribute("customer", customer);
        }

        logger.debug("Customer ID: " + customerID);
        logger.debug("Outbound Flight: " + outboundFlightId);
        if (returnFlightId != null) {
            logger.debug("Return Flight: " + returnFlightId);
        }
        logger.debug("DashPass Option: " + dashPassOption);


        model.addAttribute("paymentDetailsDTO", new PaymentDetailsDTO());
        model.addAttribute("outboundFlight", outboundFlight);
        model.addAttribute("returnFlight", returnFlight);
        model.addAttribute("dashPassOption", dashPassOption);
        model.addAttribute("tripType", tripType);
        model.addAttribute("totalPrice", totalPrice);

        return "paymentmethoddetails"; // Template for payment method details
    }

    // POST mapping to handle the flight purchase and show purchase complete page
    @PostMapping("/customer/{customerID}/purchasecomplete")
    public String completePurchase(@PathVariable Long customerID,
                                   @RequestParam("outboundFlightId") Long outboundFlightId,
                                   @RequestParam(value = "returnFlightId", required = false) Long returnFlightId,
                                   @RequestParam("dashPassOption") String dashPassOption,
                                   @RequestParam("tripType") String tripType,
                                   @RequestParam("totalPrice") double totalPrice,
                                   @RequestParam("userSelectedStatus") String userSelectedStatus,
                                   @Valid @ModelAttribute("paymentDetailsDTO") PaymentDetailsDTO paymentDetailsDTO,
                                   BindingResult bindingResult,
                                   Model model) throws InterruptedException, ExecutionException {

        // Check for validation errors in the form
        if (bindingResult.hasErrors()) {
            Flight outboundFlight = flightService.findFlightById(outboundFlightId);
            model.addAttribute("outboundFlight", outboundFlight);

            if (returnFlightId != null) {
                Flight returnFlight = flightService.findFlightById(returnFlightId);
                model.addAttribute("returnFlight", returnFlight);
            }

            Customer customer = customerService.findCustomerById(customerID);
            model.addAttribute("customer", customer);

            model.addAttribute("tripType", tripType);
            model.addAttribute("dashPassOption", dashPassOption);
            model.addAttribute("totalPrice", totalPrice);
            model.addAttribute("userSelectedStatus", userSelectedStatus);

            return "paymentmethoddetails";
        }

        try {
            Customer customer = customerService.findCustomerById(customerID);

            if (customer == null) {
                throw new IllegalStateException("Customer not found for ID: " + customerID);
            }

            logger.debug("Customer ID: " + customerID);
            logger.debug("Outbound Flight: " + outboundFlightId);
            if (returnFlightId != null) {
                logger.debug("Return Flight: " + returnFlightId);
            }
            logger.debug("DashPass Option: " + dashPassOption);

            // Process the payment asynchronously
            CompletableFuture<Reservation> futureReservation = bookingService.purchaseFlightAsync(
                    customer, outboundFlightId, returnFlightId, dashPassOption, tripType, totalPrice, paymentDetailsDTO, userSelectedStatus
            );

            // Wait for the payment to process
            Reservation reservation = futureReservation.get();

            // If payment is successful, proceed to the purchase complete page
            if (reservation.getPaymentStatus() == PaymentStatus.PAID) {
                model.addAttribute("reservation", reservation);
                model.addAttribute("outboundFlight", reservation.getFlights().get(0));

                if (reservation.getFlights().size() > 1) {
                    model.addAttribute("returnFlight", reservation.getFlights().get(1));
                }

                // Pass the DashPassReservation details to the model
                if (!reservation.getDashPassReservations().isEmpty()) {
                    model.addAttribute("dashPassReservation", reservation.getDashPassReservations().get(0));
                    model.addAttribute("dashPass", reservation.getDashPassReservations().get(0).getDashPass());
                }

                model.addAttribute("totalPrice", reservation.getTotalPrice());
                return "purchasecomplete";
            }

        } catch (ExecutionException | InterruptedException e) {
            // If payment fails, return to the payment page with an error message
            handlePaymentError(model, outboundFlightId, returnFlightId, customerID, tripType, dashPassOption, totalPrice, userSelectedStatus);
            model.addAttribute("errorMessage", "Payment failed. Please try again.");
            return "paymentmethoddetails";
        }

        // If the payment status is anything other than PAID, assume failure and return to payment page
        handlePaymentError(model, outboundFlightId, returnFlightId, customerID, tripType, dashPassOption, totalPrice, userSelectedStatus);
        model.addAttribute("errorMessage", "Payment failed or no valid payment status was detected.");

        return "paymentmethoddetails";
    }


    // Helper method to handle errors and return the model with necessary attributes
    private void handlePaymentError(Model model, Long outboundFlightId, Long returnFlightId, Long customerID,
                                    String tripType, String dashPassOption, double totalPrice, String userSelectedStatus) {
        Flight outboundFlight = flightService.findFlightById(outboundFlightId);
        model.addAttribute("outboundFlight", outboundFlight);

        if (returnFlightId != null) {
            Flight returnFlight = flightService.findFlightById(returnFlightId);
            model.addAttribute("returnFlight", returnFlight);
        }

        Customer customer = customerService.findCustomerById(customerID);
        model.addAttribute("customer", customer);

        model.addAttribute("tripType", tripType);
        model.addAttribute("dashPassOption", dashPassOption);
        model.addAttribute("totalPrice", totalPrice);
        model.addAttribute("userSelectedStatus", userSelectedStatus);
    }


    @GetMapping("/customer/{customerID}/purchasecomplete")
    public String purchaseComplete(@PathVariable Long customerID, Model model) {
        // Fetch customer
        Customer customer = customerService.findCustomerById(customerID);
        model.addAttribute("customer", customer);

        // Fetch the latest reservation for the customer where payment status is PAID
        Reservation reservation = reservationService.findLatestPaidReservationForCustomer(customer);

        if (reservation != null) {
            // Add reservation details to the model
            model.addAttribute("reservation", reservation);
            model.addAttribute("outboundFlight", reservation.getFlights().get(0)); // assuming at least one flight

            // Check if there's a return flight for round-trip
            if (reservation.getFlights().size() > 1) {
                model.addAttribute("returnFlight", reservation.getFlights().get(1));
            }

            // Add DashPass reservation details, if applicable
            List<DashPassReservation> dashPassReservations = reservation.getDashPassReservations();
            if (dashPassReservations != null && !dashPassReservations.isEmpty()) {
                model.addAttribute("dashPassReservations", dashPassReservations);
            }
        } else {
            // Handle case where no paid reservation is found
            model.addAttribute("errorMessage", "No valid reservation found.");
        }

        // Return the purchase complete view
        return "purchasecomplete";
    }

}

