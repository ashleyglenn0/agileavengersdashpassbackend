package agileavengers.southwest_dashpass.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    @GetMapping("/landingpage")
    public String getLandingPage() {

        return "landingpage";
    }
}
