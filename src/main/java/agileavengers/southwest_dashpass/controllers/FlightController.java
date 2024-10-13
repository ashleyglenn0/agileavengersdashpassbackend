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
        Customer customer = customerService.findCustomerById(customerID);

        FlightSearchResponse flightSearchResponse = flightService.findFlights(
                departureAirport,
                arrivalAirport,
                selectedDate,
                returnDate,
                tripType,
                customer
        );

        System.out.println("Outbound Flights: " + flightSearchResponse.getOutboundFlights());
        System.out.println("Return Flights: " + flightSearchResponse.getReturnFlights());

        model.addAttribute("outboundFlights", flightSearchResponse.getOutboundFlights());
        model.addAttribute("returnFlights", flightSearchResponse.getReturnFlights());
        model.addAttribute("tripType", tripType);  // TripType is correctly passed

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

    @GetMapping("/customer/{customerID}/confirmFlight")
    public String getConfirmFlightDetails(@PathVariable Long customerID,
                                          @RequestParam("outboundFlightId") Long outboundFlightId,
                                          @RequestParam(value = "returnFlightId", required = false) Long returnFlightId,
                                          @RequestParam("dashPassOption") String dashPassOption,
                                          @RequestParam("tripType") String tripType,  // Ensure tripType is passed
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
            totalPrice += 50.0; // Assuming DashPass is $50
        }

        model.addAttribute("customer", customer);
        model.addAttribute("outboundFlight", outboundFlight);
        model.addAttribute("returnFlight", returnFlight);
        model.addAttribute("dashPassOption", dashPassOption);
        model.addAttribute("tripType", tripType);
        model.addAttribute("totalPrice", totalPrice);

        return "finalconfirmation";
    }

    @PostMapping("/customer/{customerID}/confirmFlight")
    public String confirmFlight(@PathVariable Long customerID,
                                @RequestParam("outboundFlightId") Long outboundFlightId,
                                @RequestParam(value = "returnFlightId", required = false) Long returnFlightId,
                                @RequestParam("dashPassOption") String dashPassOption,
                                @RequestParam("tripType") String tripType,
                                Model model) {
        // Retrieve the customer and flight details
        Customer customer = customerService.findCustomerById(customerID);
        Flight outboundFlight = flightService.findFlightById(outboundFlightId);

        // Retrieve return flight details if applicable
        Flight returnFlight = null;
        if (returnFlightId != null) {
            returnFlight = flightService.findFlightById(returnFlightId);
        }

        // Calculate total price
        double totalPrice = outboundFlight.getPrice();
        if (returnFlight != null) {
            totalPrice += returnFlight.getPrice();
        }

        // Add DashPass price if a new one is being purchased
        if ("new".equals(dashPassOption)) {
            totalPrice += 50;
        }

        // Redirect to purchase flight page with all necessary parameters
        return "redirect:/customer/" + customerID + "/finalconfirmation"
                + "?outboundFlightId=" + outboundFlightId
                + (returnFlightId != null ? "&returnFlightId=" + returnFlightId : "")
                + "&dashPassOption=" + dashPassOption
                + "&tripType=" + tripType
                + "&totalPrice=" + totalPrice;
    }


    // GET mapping to show the purchase flight page for final confirmation
    @GetMapping("/customer/{customerID}/finalconfirmation")
    public String showPurchaseFlightPage(@PathVariable Long customerID,
                                         @RequestParam("outboundFlightId") Long outboundFlightId,
                                         @RequestParam(value = "returnFlightID", required = false) Long returnFlightID,
                                         @RequestParam("dashPassOption") String dashPassOption,
                                         @RequestParam("tripType") String tripType,
                                         @RequestParam("totalPrice") double totalPrice,
                                         Model model) {
        // Retrieve customer and flight details
        Customer customer = customerService.findCustomerById(customerID);
        Flight outboundFlight = flightService.findFlightById(outboundFlightId);
        Flight returnFlight = null;
        if (returnFlightID != null) {
            returnFlight = flightService.findFlightById(returnFlightID);
        }

        // Add data to model for the purchase confirmation view
        model.addAttribute("customer", customer);
        model.addAttribute("outboundFlight", outboundFlight);
        model.addAttribute("returnFlight", returnFlight);
        model.addAttribute("dashPassOption", dashPassOption);
        model.addAttribute("tripType", tripType);
        model.addAttribute("totalPrice", totalPrice);

        return "finalconfirmation"; // Displays purchase flight review page
    }

    // POST mapping to handle final flight purchase and booking creation
    @PostMapping("/customer/{customerID}/finalconfirmation")
    public String purchaseFlight(@PathVariable Long customerID,
                                 @RequestParam("outboundFlightId") Long outboundFlightId,
                                 @RequestParam(value = "returnFlightId", required = false) Long returnFlightId,
                                 @RequestParam("dashPassOption") String dashPassOption,
                                 @RequestParam("tripType") String tripType,
                                 @RequestParam("totalPrice") double totalPrice,
                                 Model model) {
        // Retrieve customer and flight details
        Customer customer = customerService.findCustomerById(customerID);
        Flight outboundFlight = flightService.findFlightById(outboundFlightId);

        // Initialize returnFlight to null
        Flight returnFlight = null;

        // Only retrieve return flight if returnFlightId is not null
        if (returnFlightId != null) {
            returnFlight = flightService.findFlightById(returnFlightId);
        }

        // Call booking service to finalize the purchase
        Reservation booking;
        if (returnFlight != null) {
            booking = bookingService.purchaseFlight(customer, outboundFlight.getFlightID(), returnFlight.getFlightID(), dashPassOption, tripType, totalPrice);
        } else {
            booking = bookingService.purchaseFlight(customer, outboundFlight.getFlightID(), null, dashPassOption, tripType, totalPrice);
        }

        // Add the necessary attributes to the model to match Thymeleaf template
        model.addAttribute("reservation", booking);
        model.addAttribute("outboundFlight", outboundFlight);
        model.addAttribute("returnFlight", returnFlight);
        model.addAttribute("totalPrice", totalPrice);

        return "confirmation"; // Shows booking confirmation page
    }

    @GetMapping("/confirmation")
    public String getConfirmationPage(Model model, @RequestParam Long reservationId) {
        Optional<Reservation> reservationOpt = Optional.ofNullable(reservationService.findById(reservationId));
        if (reservationOpt.isPresent()) {
            Reservation reservation = reservationOpt.get();
            if (!reservation.getFlights().isEmpty()) {
                model.addAttribute("reservation", reservation);
            } else {
                model.addAttribute("error", "No flights associated with this reservation.");
            }
        } else {
            model.addAttribute("error", "Reservation not found.");
        }
        return "confirmation";
    }


}

