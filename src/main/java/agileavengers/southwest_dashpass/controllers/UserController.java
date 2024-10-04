package agileavengers.southwest_dashpass.controllers;

import agileavengers.southwest_dashpass.models.Customer;
import agileavengers.southwest_dashpass.models.Employee;
import agileavengers.southwest_dashpass.models.User;
import agileavengers.southwest_dashpass.models.UserType;
import agileavengers.southwest_dashpass.services.CustomerService;
import agileavengers.southwest_dashpass.services.EmployeeService;
import agileavengers.southwest_dashpass.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/signup")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "signup";
    }

    @PostMapping("/signup")
    public String registerUser(@Validated @ModelAttribute("user") User user,
                               @RequestParam String userType,
                               BindingResult result,
                               Model model) {
        // Check for validation errors
        if (result.hasErrors()) {
            // Send the user back to the signup page with errors
            model.addAttribute("error", "There was an error in the form submission.");
            return "signup";  // This refers to the Thymeleaf template
        }

        // Check if the username already exists
        if (userService.loadUserByUsername(user.getUsername()) != null) {
            model.addAttribute("error", "Username already exists.");
            return "signup";  // Send back to the signup page with an error message
        }

        // Encode the password before saving the user
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Set the user type based on the form submission
        user.setUserType(UserType.valueOf(userType.toUpperCase()));  // Converts "CUSTOMER" or "EMPLOYEE" to the enum type

        // Save the user via the UserService
        userService.saveUser(user);

        // Redirect to the login page upon successful registration
        return "redirect:/login";  // Thymeleaf will resolve the login template
    }
}
