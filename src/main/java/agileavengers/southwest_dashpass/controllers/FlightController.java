package agileavengers.southwest_dashpass.controllers;

import agileavengers.southwest_dashpass.models.*;
import agileavengers.southwest_dashpass.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class FlightController {
    private final CustomerService customerService;
    private final AirportService airportService;
    private final FlightService flightService;
    private final DashPassService dashPassService;
    private final DashPassReservationService dashPassReservationService;
    private final ReservationService reservationService;

    private final BookingService bookingService;

    @Autowired
    public FlightController(CustomerService customerService, AirportService airportService, FlightService flightService,
                            DashPassService dashPassService, DashPassReservationService dashPassReservationService,
                            BookingService bookingService, ReservationService reservationService) {
        this.customerService = customerService;
        this.airportService = airportService;
        this.flightService = flightService;
        this.dashPassService = dashPassService;
        this.dashPassReservationService = dashPassReservationService;
        this.bookingService = bookingService;
        this.reservationService = reservationService;

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

        // Redirect to the final confirmation page with necessary parameters
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
        Customer customer = customerService.findCustomerById(customerID);
        Flight outboundFlight = flightService.findFlightById(outboundFlightId);
        Flight returnFlight = null;
        if (returnFlightId != null) {
            returnFlight = flightService.findFlightById(returnFlightId);
        }

        model.addAttribute("customer", customer);
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
                                   Model model) {
        Customer customer = customerService.findCustomerById(customerID);
        Flight outboundFlight = flightService.findFlightById(outboundFlightId);
        Flight returnFlight = null;
        if (returnFlightId != null) {
            returnFlight = flightService.findFlightById(returnFlightId);
        }

        // Process the purchase
        Reservation reservation = bookingService.purchaseFlight(customer, outboundFlightId, returnFlightId, dashPassOption, tripType, totalPrice);

        model.addAttribute("reservation", reservation);
        model.addAttribute("outboundFlight", outboundFlight);
        model.addAttribute("returnFlight", returnFlight);
        model.addAttribute("totalPrice", totalPrice);

        return "purchasecomplete"; // Updated to match the new template name
    }



}

