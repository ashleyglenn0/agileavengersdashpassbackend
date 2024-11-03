package agileavengers.southwest_dashpass.controllers;

import agileavengers.southwest_dashpass.services.FlightCleanupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {
    @Autowired
    FlightCleanupService flightCleanupService;

    @GetMapping("/cleanupPage")
    public String showCleanupPage() {
        return "cleanupFlights";  // The Thymeleaf template name (cleanupFlights.html)
    }

    @PostMapping("/cleanupFlights")
    public String cleanUpExpiredFlights() {
        // Call the service to clean up flights
        flightCleanupService.cleanUpExpiredFlights();
        return "redirect:/admin/success"; // Redirect to a success page or dashboard after cleanup
    }

    @GetMapping("/success")
    public String showSuccessPage() {
        return "success"; // Success page (success.html)
    }
}
