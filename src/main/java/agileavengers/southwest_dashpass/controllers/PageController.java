package agileavengers.southwest_dashpass.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {
//    @GetMapping("/login")
//    public String showLoginPage(){
//        return "login";
//    }
    @GetMapping("/customerDashboard")
    public String showCustomerDashboard(){
        return "customerDashboard";
    }

    @GetMapping("/employeeDashboard")
    public String showEmployeeDashboard(){
        return "employeeDashboard";
    }

    //TODO: Add controllers for Dashboards to go to Purchase and other pages

}
