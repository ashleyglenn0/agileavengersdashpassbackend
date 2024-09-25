package agileavengers.southwest_dashpass.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    @GetMapping("/")
    public String getLandingPage() {
        return "landingPage.html";
    }
}
