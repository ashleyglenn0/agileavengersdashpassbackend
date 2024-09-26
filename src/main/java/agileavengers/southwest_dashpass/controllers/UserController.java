package agileavengers.southwest_dashpass.controllers;

import agileavengers.southwest_dashpass.models.Customer;
import agileavengers.southwest_dashpass.models.Employee;
import agileavengers.southwest_dashpass.models.User;
import agileavengers.southwest_dashpass.models.UserType;
import agileavengers.southwest_dashpass.services.CustomerService;
import agileavengers.southwest_dashpass.services.EmployeeService;
import agileavengers.southwest_dashpass.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private CustomerService customerService;
    @Autowired
    private EmployeeService employeeService;
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "signup";
    }

    @PostMapping("/register")
    public String registerUser(@RequestParam String firstName,
                               @RequestParam String lastName,
                               @RequestParam String username,
                               @RequestParam String email,
                               @RequestParam String password,
                               @RequestParam String role,
                               @RequestParam UserType userType) {
        if (userType.equals("EMPLOYEE")) {
            employeeService.registerEmployee(firstName, lastName, username, email, password, role);
        } else if (userType.equals("CUSTOMER")) {
            customerService.registerCustomer(firstName, lastName, username, email, password);
        }
        return "redirect:/login"; // Redirect to the login page after successful registration
    }
}
