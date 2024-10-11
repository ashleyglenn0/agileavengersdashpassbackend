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

@Controller
public class FlightController {
    private final CustomerService customerService;
    private final AirportService airportService;
    private final FlightService flightService;
    private final DashPassService dashPassService;
    private final DashPassReservationService dashPassReservationService;

    @Autowired
    public FlightController(CustomerService customerService, AirportService airportService, FlightService flightService, DashPassService dashPassService, DashPassReservationService dashPassReservationService) {
        this.customerService = customerService;
        this.airportService = airportService;
        this.flightService = flightService;
        this.dashPassService = dashPassService;
        this.dashPassReservationService = dashPassReservationService;

    }

    @GetMapping("/customer/{customerID}/purchaseflight")
    public String showPurchaseFlightForm(@PathVariable Long customerID, Model model) {
        Customer customer = customerService.findCustomerById(customerID);

        // Prepare an empty flight object to bind to the form
        Flight flight = new Flight();

        // Prepare an empty DashPass object to bind to the form
        DashPass dashPass = new DashPass();

        // Fetch the list of airports
        List<Airport> airports = airportService.getAllAirports();

        // Add attributes to the model
        model.addAttribute("customer", customer);
        model.addAttribute("customerID", customerID);
        model.addAttribute("flight", flight); // Bind the empty flight object
        model.addAttribute("dashPass", dashPass); // Bind the empty DashPass object
        model.addAttribute("airports", airports); // Bind the list of airports

        return "purchaseflight";
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


        // Add outbound and return flights to the model
        model.addAttribute("outboundFlights", flightSearchResponse.getOutboundFlights());
        model.addAttribute("returnFlights", flightSearchResponse.getReturnFlights());


        return "flighttable";
    }

    @GetMapping("/flighttable")
    public String showFlightTable(Model model, @PathVariable Long customerID) {
        // Assuming customerService provides the customer object
        Customer customer = customerService.findCustomerById(customerID);
        model.addAttribute("customer", customer);
        return "flighttable";
    }

//    @GetMapping("/customer/{customerID}/confirmflight")
//    public String showConfirmationPage(
//            @PathVariable Long customerID,
//            @RequestParam("flightID") Long flightID,
//            @RequestParam(value = "dashPassOption", required = false) String dashPassOption,
//            Model model) {
//
//        // Retrieve the customer and flight details
//        Customer customer = customerService.findCustomerById(customerID);
//        Flight flight = flightService.findFlightById(flightID);
//
//        // Add attributes to the model to display on the confirmation page
//        model.addAttribute("customer", customer);
//        model.addAttribute("flight", flight);
//        model.addAttribute("dashPassOption", dashPassOption); // Optional: Pass the selected DashPass option
//
//        return "confirmationPage"; // Replace with your actual confirmation page template
//    }
//
//    @PostMapping("/customer/{customerID}/confirmFlight")
//    public String confirmFlight(
//            @PathVariable Long customerID,
//            @RequestParam("flightID") Long flightID,
//            @RequestParam(value = "returnFlightID", required = false) Long returnFlightID, // Optional for one-way
//            @RequestParam("dashPassOption") String dashPassOption,
//            Model model) {
//
//        // Find the customer
//        Customer customer = customerService.findCustomerById(customerID);
//
//        // Find the outbound flight
//        Flight outboundFlight = flightService.findFlightById(flightID);
//
//        // Initialize the total cost with the outbound flight's price
//        double totalCost = outboundFlight.getPrice();
//
//        // Check the DashPass option for the outbound flight
//        if ("existing".equals(dashPassOption)) {
//            if (customer.getDashPasses().size() > 0) {
//                customerService.useExistingDashPass(customer, outboundFlight);
//            } else {
//                model.addAttribute("error", "No available DashPasses.");
//                return "errorPage";
//            }
//        } else if ("purchase".equals(dashPassOption)) {
//            double dashPassPrice = 50.00;
//            totalCost += dashPassPrice;
//            DashPass newDashPass = dashPassService.createAndAssignDashPassDuringPurchase(customer, outboundFlight);
//            dashPassService.save(newDashPass);
//        }
//
//        // Add return flight logic for round-trip
//        Flight returnFlight = null;
//        if (returnFlightID != null) {
//            // Find the return flight
//            returnFlight = flightService.findFlightById(returnFlightID);
//            totalCost += returnFlight.getPrice();
//
//            // DashPass option logic can be extended here for the return flight if necessary
//        }
//
//        // Finalize the flight booking
//        Reservation outboundFlightReservation = flightService.confirmFlightForCustomer(customer.getId(), outboundFlight.getFlightID(), dashPassOption);
//
//
//        if (returnFlight != null) {
//            // If return flight exists, process that as well
//            Reservation roundtripReservation = flightService.confirmFlightForCustomer(customer.getId(), returnFlight.getFlightID(), dashPassOption);
//        }
//
//        // Add attributes for the confirmation page
//        model.addAttribute("customer", customer);
//        model.addAttribute("outboundFlight", outboundFlight);
//        model.addAttribute("totalCost", totalCost);
//        model.addAttribute("outboundFlightReservation", outboundFlightReservation);
//
//        // Add return flight to the model if it's a round-trip
//        if (returnFlight != null) {
//            model.addAttribute("returnFlight", returnFlight);
//            return "roundtripflightconfirmation"; // Render the round-trip confirmation page
//        }
//
//        // Render the one-way confirmation page if no return flight
//        return "onewayflightconfirmation";
//    }


}

