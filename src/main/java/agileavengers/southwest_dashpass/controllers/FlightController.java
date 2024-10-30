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
import java.util.ArrayList;
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
    public String searchFlights(@PathVariable Long customerID,
                                @RequestParam("departureAirportCode") String departureAirportCode,
                                @RequestParam("arrivalAirportCode") String arrivalAirportCode,
                                @RequestParam("departureDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate departureDate,
                                @RequestParam(value = "returnDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate returnDate,
                                @RequestParam(value = "tripType", required = false) String tripType,
                                Model model) {
        // Define a default search range (e.g., 3 days before and after the selected date)
        LocalDate departureDateRangeStart = departureDate.minusDays(3);
        LocalDate departureDateRangeEnd = departureDate.plusDays(3);

        LocalDate returnDateRangeStart = null;
        LocalDate returnDateRangeEnd = null;

        // If it's a round-trip flight, define a search range for the return date
        if ("round-trip".equalsIgnoreCase(tripType) && returnDate != null) {
            returnDateRangeStart = returnDate.minusDays(5);
            returnDateRangeEnd = returnDate.plusDays(5);
        }

        // Rest of the flight search logic...
        List<Flight> outboundFlights = flightService.searchOneWayFlights(departureAirportCode, arrivalAirportCode,
                departureDateRangeStart, departureDateRangeEnd);

        List<FlightSearchResponse> returnFlights = new ArrayList<>();
        if ("round-trip".equalsIgnoreCase(tripType)) {
            returnFlights = flightService.searchRoundTripFlights(departureAirportCode, arrivalAirportCode,
                    departureDateRangeStart, departureDateRangeEnd, returnDateRangeStart, returnDateRangeEnd);
        }

        model.addAttribute("outboundFlights", outboundFlights);
        model.addAttribute("returnFlights", returnFlights.isEmpty() ? null : returnFlights);
        model.addAttribute("tripType", tripType);
        model.addAttribute("customer", customerService.findCustomerById(customerID));

        return "flighttable";
    }





    @GetMapping("/customer/{customerID}/flighttable")
    public String showFlightTable(@PathVariable Long customerID,
                                  @RequestParam("tripType") String tripType,
                                  @RequestParam(value = "outboundFlights", required = false) List<Flight> outboundFlights,
                                  @RequestParam(value = "returnFlights", required = false) List<Flight> returnFlights,
                                  Model model) {
        // Find the customer by ID
        Customer customer = customerService.findCustomerById(customerID);

        // Add customer and tripType to the model
        model.addAttribute("customer", customer);
        model.addAttribute("tripType", tripType);

        // Add flight results to the model if they exist
        model.addAttribute("outboundFlights", outboundFlights);
        model.addAttribute("returnFlights", returnFlights != null ? returnFlights : null);  // Add return flights if present

        return "flighttable";  // Return the view for displaying results
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

        if (bindingResult.hasErrors()) {
            return handlePurchaseError(model, customerID, outboundFlightId, returnFlightId, tripType, dashPassOption, totalPrice, userSelectedStatus);
        }

        Customer customer = customerService.findCustomerById(customerID);
        if (customer == null) {
            throw new IllegalStateException("Customer not found for ID: " + customerID);
        }

        // Adjust total price if a new DashPass is being purchased
        if ("new".equalsIgnoreCase(dashPassOption)) {
            totalPrice += 50.0;
        }

        try {
            // Process payment asynchronously
            CompletableFuture<Reservation> futureReservation = bookingService.purchaseFlightAsync(
                    customer, outboundFlightId, returnFlightId, dashPassOption, tripType, totalPrice, paymentDetailsDTO, userSelectedStatus
            );

            Reservation reservation = futureReservation.get();

            // Debugging output
            System.out.println("Payment Status after async completion: " + reservation.getPaymentStatus());

            // Check if the payment was successful
            if (reservation.getPaymentStatus() == PaymentStatus.PAID) {
                processDashPassSelection(dashPassOption, customer, reservation.getFlights());

                model.addAttribute("reservation", reservation);
                model.addAttribute("outboundFlight", reservation.getFlights().get(0));

                if (reservation.getFlights().size() > 1) {
                    model.addAttribute("returnFlight", reservation.getFlights().get(1));
                }
                model.addAttribute("totalPrice", totalPrice);

                return "purchasecomplete";
            }

        } catch (ExecutionException | InterruptedException e) {
            model.addAttribute("errorMessage", "Payment failed. Please try again.");
            return handlePurchaseError(model, customerID, outboundFlightId, returnFlightId, tripType, dashPassOption, totalPrice, userSelectedStatus);
        }

        // Handle failure for unprocessed payment status
        model.addAttribute("errorMessage", "Payment failed or no valid payment status was detected.");
        return handlePurchaseError(model, customerID, outboundFlightId, returnFlightId, tripType, dashPassOption, totalPrice, userSelectedStatus);
    }


    private String handlePurchaseError(Model model, Long customerID, Long outboundFlightId, Long returnFlightId, String tripType,
                                       String dashPassOption, double totalPrice, String userSelectedStatus) {
        // Fetch flight details
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

    private void processDashPassSelection(String dashPassOption, Customer customer, List<Flight> flights) {
        for (Flight flight : flights) {
            if ("new".equalsIgnoreCase(dashPassOption)) {
                // Customer opted to purchase a new DashPass
                dashPassReservationService.createNewDashPassAndSaveNewDashPassReservation(customer, flight);
            } else if ("existing".equalsIgnoreCase(dashPassOption)) {
                // Customer opted to use an existing DashPass
                dashPassReservationService.createNewDashPassReservationAndAssignExistingDashPass(customer, flight);
            }
        }
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

