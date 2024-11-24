package agileavengers.southwest_dashpass.controllers;

import agileavengers.southwest_dashpass.dtos.DisplayPaymentDetailsDTO;
import agileavengers.southwest_dashpass.dtos.RoundTripFlightDTO;
import agileavengers.southwest_dashpass.repository.PaymentDetailsRepository;
import agileavengers.southwest_dashpass.utils.EncryptionUtils;
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
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;


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
    private final EncryptionUtils encryptionUtils;
    private final PaymentDetailsRepository paymentDetailsRepository;

    @Autowired
    public FlightController(CustomerService customerService, AirportService airportService, FlightService flightService,
                            DashPassService dashPassService, DashPassReservationService dashPassReservationService,
                            BookingService bookingService, ReservationService reservationService,
                            PaymentDetailsService paymentDetailsService, UserService userService,
                            EncryptionUtils encryptionUtils, PaymentDetailsRepository paymentDetailsRepository) {
        this.customerService = customerService;
        this.airportService = airportService;
        this.flightService = flightService;
        this.dashPassService = dashPassService;
        this.dashPassReservationService = dashPassReservationService;
        this.bookingService = bookingService;
        this.reservationService = reservationService;
        this.paymentDetailsService = paymentDetailsService;
        this.userService = userService;
        this.encryptionUtils = encryptionUtils;
        this.paymentDetailsRepository = paymentDetailsRepository;

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
            @RequestParam("departureDate") LocalDate departureDate,
            @RequestParam("departureAirportCode") String departureAirportCode,
            @RequestParam("arrivalAirportCode") String arrivalAirportCode,
            @RequestParam("tripType") TripType tripType,
            @RequestParam(value = "returnDate", required = false) LocalDate returnDate,
            Model model) {

        Customer customer = customerService.findCustomerById(customerID);

        // Maps to hold DashPass display flags by flight ID
        Map<Long, Boolean> showExistingDashPassMap = new HashMap<>();
        Map<Long, Boolean> showNewDashPassMap = new HashMap<>();

        if (tripType == TripType.ROUND_TRIP && returnDate != null) {
            List<RoundTripFlightDTO> roundTripFlights = flightService.searchRoundTripFlights(
                    departureAirportCode, arrivalAirportCode,
                    departureDate.minusDays(3), departureDate.plusDays(3),
                    returnDate.minusDays(5), returnDate.plusDays(5)
            );

            roundTripFlights.forEach(roundTrip -> {
                roundTrip.getOutboundFlights().forEach(flight -> {
                    showExistingDashPassMap.put(flight.getFlightID(),
                            customer.getDashPasses().size() > 0 &&
                                    flight.getNumberOfDashPassesAvailable() > 0 &&
                                    flight.canAddExistingDashPass());

                    showNewDashPassMap.put(flight.getFlightID(),
                            customer.getAvailableDashPassCount() > 0 &&
                                    flight.getNumberOfDashPassesAvailable() > 0 &&
                                    flight.canAddNewDashPass());

                });
            });
            showExistingDashPassMap.forEach((id, show) -> System.out.println("showExistingDashPassMap - Flight ID: " + id + ", Show: " + show));
            showNewDashPassMap.forEach((id, show) -> System.out.println("showNewDashPassMap - Flight ID: " + id + ", Show: " + show));



            model.addAttribute("roundTripFlights", roundTripFlights != null ? roundTripFlights : Collections.emptyList());
        } else {
            FlightSearchResponse flightSearchResponse = flightService.findFlights(
                    departureAirportCode, arrivalAirportCode, departureDate, returnDate, tripType, customer
            );

            flightSearchResponse.getOutboundFlights().forEach(flight -> {
                showExistingDashPassMap.put(flight.getFlightID(),
                        customer.getDashPasses().size() > 0 &&
                                flight.getNumberOfDashPassesAvailable() > 0 &&
                                flight.canAddExistingDashPass());

                showNewDashPassMap.put(flight.getFlightID(),
                        customer.getAvailableDashPassCount() > 0 &&
                                flight.getNumberOfDashPassesAvailable() > 0 &&
                                flight.canAddNewDashPass());


            });
            showExistingDashPassMap.forEach((id, show) -> System.out.println("showExistingDashPassMap - Flight ID: " + id + ", Show: " + show));
            showNewDashPassMap.forEach((id, show) -> System.out.println("showNewDashPassMap - Flight ID: " + id + ", Show: " + show));


            model.addAttribute("outboundFlights", flightSearchResponse.getOutboundFlights() != null ? flightSearchResponse.getOutboundFlights() : Collections.emptyList());
            model.addAttribute("returnFlights", flightSearchResponse.getReturnFlights() != null ? flightSearchResponse.getReturnFlights() : Collections.emptyList());
        }

        model.addAttribute("tripType", tripType);
        model.addAttribute("customer", customer);
        model.addAttribute("showExistingDashPassMap", showExistingDashPassMap);
        model.addAttribute("showNewDashPassMap", showNewDashPassMap);

        return "flighttable";
    }

    @GetMapping("/customer/{customerID}/addBags")
    public String displayAddBagsPage(
            @PathVariable Long customerID,
            @RequestParam("tripType") String tripType,
            @RequestParam("outboundFlightId") Long outboundFlightId,
            @RequestParam(value = "returnFlightId", required = false) Long returnFlightId,
            @RequestParam("dashPassOption") String dashPassOption,
            Model model) {

        // Retrieve customer and flight details
        Customer customer = customerService.findCustomerById(customerID);
        Flight outboundFlight = flightService.findFlightById(outboundFlightId);
        Flight returnFlight = returnFlightId != null ? flightService.findFlightById(returnFlightId) : null;

        // Calculate base total price (without bagCost)
        double totalPrice = outboundFlight.getPrice();
        if (returnFlight != null) {
            totalPrice += returnFlight.getPrice();
        }

        if ("new".equalsIgnoreCase(dashPassOption)) {
            totalPrice += 50.0; // Add DashPass cost if "new" is selected
        }

        // Add data to the model
        model.addAttribute("customer", customer);
        model.addAttribute("customerID", customerID);
        model.addAttribute("tripType", tripType);
        model.addAttribute("outboundFlightId", outboundFlightId);
        model.addAttribute("returnFlightId", returnFlightId);
        model.addAttribute("dashPassOption", dashPassOption);
        model.addAttribute("outboundFlight", outboundFlight);
        model.addAttribute("returnFlight", returnFlight);
        model.addAttribute("totalPrice", totalPrice); // Pass base totalPrice

        return "addBag"; // Returns the Add Bag template
    }


    @PostMapping("/customer/{customerID}/addBags")
    public String processAddBags(
            @PathVariable Long customerID,
            @RequestParam("tripType") String tripType,
            @RequestParam("outboundFlightId") Long outboundFlightId,
            @RequestParam(value = "returnFlightId", required = false) Long returnFlightId,
            @RequestParam("dashPassOption") String dashPassOption,
            @RequestParam(value = "bagQuantity", defaultValue = "1") int bagQuantity,
            @RequestParam("bagCost") double bagCost,
            @RequestParam("totalPrice") double totalPrice,
            Model model) {

        // Calculate bag cost
//        bagCost = 0;
        if (bagQuantity > 1) {
            bagCost = (bagQuantity - 1) * 35; // First bag is free
        }

        totalPrice += bagCost; // Add bag cost to total price

        // Redirect to the review order page with all required parameters
        return "redirect:/customer/" + customerID + "/revieworder"
                + "?outboundFlightId=" + outboundFlightId
                + (returnFlightId != null ? "&returnFlightId=" + returnFlightId : "")
                + "&dashPassOption=" + dashPassOption
                + "&tripType=" + tripType
                + "&bagQuantity=" + bagQuantity
                + "&bagCost=" + bagCost
                + "&totalPrice=" + totalPrice;
    }


    // GET mapping to show the review order page
    @GetMapping("/customer/{customerID}/revieworder")
    public String getReviewOrderPage(
            @PathVariable Long customerID,
            @RequestParam("outboundFlightId") Long outboundFlightId,
            @RequestParam(value = "returnFlightId", required = false) Long returnFlightId,
            @RequestParam("dashPassOption") String dashPassOption,
            @RequestParam("tripType") String tripType,
            @RequestParam(value = "bagQuantity", defaultValue = "1") int bagQuantity,
            @RequestParam("bagCost") double bagCost,
            @RequestParam("totalPrice") double totalPrice,
            Model model) {

        // Retrieve customer and flights for display
        Customer customer = customerService.findCustomerById(customerID);
        Flight outboundFlight = flightService.findFlightById(outboundFlightId);
        Flight returnFlight = returnFlightId != null ? flightService.findFlightById(returnFlightId) : null;

        System.out.println("Bag Quantity: " + bagQuantity + ", Bag Cost: " + bagCost);
        System.out.println("Total Price in Review Order: " + totalPrice);

        DashPass dashPass = null;
        if ("existing".equals(dashPassOption) && !customer.getDashPasses().isEmpty()) {
            dashPass = customer.getDashPasses().get(0); // Get the first DashPass in the list
        }


        // Add attributes to the model
        model.addAttribute("customer", customer);
        model.addAttribute("outboundFlight", outboundFlight);
        model.addAttribute("returnFlight", returnFlight);
        model.addAttribute("dashPassOption", dashPassOption);
        model.addAttribute("dashPass", dashPass);
        model.addAttribute("tripType", tripType);
        model.addAttribute("bagQuantity", bagQuantity);
        model.addAttribute("bagCost", bagCost);
        model.addAttribute("totalPrice", totalPrice);

        return "revieworder";
    }


    @PostMapping("/customer/{customerID}/revieworder")
    public String confirmFlight(
            @PathVariable Long customerID,
            @RequestParam("outboundFlightId") Long outboundFlightId,
            @RequestParam(value = "returnFlightId", required = false) Long returnFlightId,
            @RequestParam("dashPassOption") String dashPassOption,
            @RequestParam("tripType") String tripType,
            @RequestParam(value = "bagQuantity", defaultValue = "1") int bagQuantity,
            @RequestParam("bagCost") double bagCost,
            @RequestParam("totalPrice") double totalPrice,
            Model model) {

        // Retrieve customer and flights for validation or display
        Customer customer = customerService.findCustomerById(customerID);
        Flight outboundFlight = flightService.findFlightById(outboundFlightId);
        Flight returnFlight = returnFlightId != null ? flightService.findFlightById(returnFlightId) : null;

        // Retrieve the first available DashPass if an existing DashPass is selected
        DashPass dashPass = null;
        if ("existing".equalsIgnoreCase(dashPassOption) && !customer.getDashPasses().isEmpty()) {
            dashPass = customer.getDashPasses().get(0); // Use the first available DashPass
        }

        // Add bag cost to the total price
        totalPrice += bagCost;

        System.out.println("Bag Quantity: " + bagQuantity + ", Bag Cost: " + bagCost);
        System.out.println("Total Price in Review Order POST: " + totalPrice);


        // Redirect to the payment method details page with all relevant parameters
        return "redirect:/customer/" + customerID + "/paymentmethoddetails"
                + "?outboundFlightId=" + outboundFlightId
                + (returnFlightId != null ? "&returnFlightId=" + returnFlightId : "")
                + "&dashPassOption=" + dashPassOption
                + "&tripType=" + tripType
                + "&bagQuantity=" + bagQuantity
                + "&bagCost=" + bagCost
                + "&totalPrice=" + totalPrice
                + (dashPass != null ? "&dashPassId=" + dashPass.getDashpassId() : "");
    }



    @GetMapping("/customer/{customerID}/paymentmethoddetails")
    public String showPaymentMethodPage(@PathVariable Long customerID,
                                        @RequestParam("outboundFlightId") Long outboundFlightId,
                                        @RequestParam(value = "bagQuantity", defaultValue = "1") int bagQuantity,
                                        @RequestParam(value = "returnFlightId", required = false) Long returnFlightId,
                                        @RequestParam("dashPassOption") String dashPassOption,
                                        @RequestParam("tripType") String tripType,
                                        @RequestParam("totalPrice") double totalPrice,
                                        Model model) {
        Flight outboundFlight = flightService.findFlightById(outboundFlightId);
        Flight returnFlight = returnFlightId != null ? flightService.findFlightById(returnFlightId) : null;

        Customer customer = model.containsAttribute("customer") ?
                (Customer) model.getAttribute("customer") :
                customerService.findCustomerById(customerID);

        List<DisplayPaymentDetailsDTO> savedPaymentMethods = paymentDetailsService.getDisplayPaymentDetails(customerID);

        // Log a message if lastFourDigits is null, for debugging purposes


        model.addAttribute("paymentDetailsDTO", new PaymentDetailsDTO());
        model.addAttribute("bagQuantity", bagQuantity);
        model.addAttribute("customer", customer);
        model.addAttribute("outboundFlight", outboundFlight);
        model.addAttribute("returnFlight", returnFlight);
        model.addAttribute("dashPassOption", dashPassOption);
        model.addAttribute("tripType", tripType);
        model.addAttribute("totalPrice", totalPrice);
        model.addAttribute("paymentMethods", savedPaymentMethods); // Add saved payment methods to model

        return "paymentmethoddetails";
    }


    // POST mapping to handle the flight purchase and show purchase complete page
    @PostMapping("/customer/{customerID}/purchasecomplete")
    public String completePurchase(@PathVariable Long customerID,
                                   @RequestParam("outboundFlightId") Long outboundFlightId,
                                   @RequestParam(value = "returnFlightId", required = false) Long returnFlightId,
                                   @RequestParam("dashPassOption") String dashPassOption,
                                   @RequestParam("tripType") String tripType,
                                   @RequestParam("totalPrice") double totalPrice,
                                   @RequestParam(value = "bagQuantity", defaultValue = "1") int bagQuantity,
                                   @RequestParam("userSelectedStatus") String userSelectedStatus,
                                   @RequestParam(value = "selectedPaymentMethodId", required = false) String selectedPaymentMethodId,
                                   @RequestParam(value = "savePaymentDetails", required = false, defaultValue = "false") boolean savePaymentDetails,
                                   @Valid @ModelAttribute("paymentDetailsDTO") PaymentDetailsDTO paymentDetailsDTO,
                                   BindingResult bindingResult,
                                   Model model) throws InterruptedException, ExecutionException {

        System.out.println("(In controller) Starting completePurchase for Customer ID: " + customerID);

        // Validate payment details if using a new payment method
        if ("new".equals(selectedPaymentMethodId) && bindingResult.hasErrors()) {
            handlePaymentError(model, outboundFlightId, returnFlightId, customerID, tripType, dashPassOption, totalPrice, userSelectedStatus);
            return "paymentmethoddetails";
        }

        try {
            Customer customer = customerService.findCustomerById(customerID);
            if (customer == null) {
                throw new IllegalStateException("Customer not found for ID: " + customerID);
            }
            System.out.println("(Controller) Calling purchaseFlightAsync for Customer ID: " + customerID);

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

            // Process the payment asynchronously
            CompletableFuture<Reservation> futureReservation = bookingService.purchaseFlightAsync(
                    customer, outboundFlightId, returnFlightId, dashPassOption, tripType, totalPrice, paymentDetailsToUse, userSelectedStatus, null, bagQuantity
            );

            System.out.println("Complete Purchase - Total Price: " + totalPrice);
            System.out.println("User Selected Status: " + userSelectedStatus);


            // Wait for payment completion and handle reservation status
            Reservation reservation = futureReservation.get();
            if (reservation.getPaymentStatus() == PaymentStatus.PAID) {
                List<Bag> bags = reservation.getBags(); // Fetch associated bags

                model.addAttribute("bags", bags); // Add bags to the model
                model.addAttribute("reservation", reservation);
                model.addAttribute("outboundFlight", reservation.getFlights().get(0));

                if (reservation.getFlights().size() > 1) {
                    model.addAttribute("returnFlight", reservation.getFlights().get(1));
                }

                if (!reservation.getDashPassReservations().isEmpty()) {
                    model.addAttribute("dashPassReservation", reservation.getDashPassReservations().get(0));
                    model.addAttribute("dashPass", reservation.getDashPassReservations().get(0).getDashPass());
                }

                model.addAttribute("totalPrice", reservation.getTotalPrice());
                return "purchasecomplete";
            } else {
                // Handle payment failure
                handlePaymentError(model, outboundFlightId, returnFlightId, customerID, tripType, dashPassOption, totalPrice, userSelectedStatus);
                model.addAttribute("errorMessage", "Payment failed. Please try again.");
                return "paymentmethoddetails";
            }
        } catch (Exception e) {
            System.out.println("Unexpected exception: " + e.getMessage());
            handlePaymentError(model, outboundFlightId, returnFlightId, customerID, tripType, dashPassOption, totalPrice, userSelectedStatus);
            model.addAttribute("errorMessage", "An unexpected error occurred. Please try again.");
            return "paymentmethoddetails";
        }
    }


    // Helper method to handle errors and return the model with necessary attributes
    private void handlePaymentError(Model model, Long outboundFlightId, Long returnFlightId, Long customerID,
                                    String tripType, String dashPassOption, double totalPrice, String userSelectedStatus) {
        model.addAttribute("outboundFlight", flightService.findFlightById(outboundFlightId));
        if (returnFlightId != null) {
            model.addAttribute("returnFlight", flightService.findFlightById(returnFlightId));
        }
        model.addAttribute("customer", customerService.findCustomerById(customerID));
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

            // Add bag details to the model
            List<Bag> bags = reservation.getBags();
            if (bags != null && !bags.isEmpty()) {
                model.addAttribute("bags", bags);
            } else {
                model.addAttribute("bags", Collections.emptyList()); // Pass an empty list to avoid errors in the view
            }
        } else {
            // Handle case where no paid reservation is found
            model.addAttribute("errorMessage", "No valid reservation found.");
        }

        // Return the purchase complete view
        return "purchasecomplete";
    }

}