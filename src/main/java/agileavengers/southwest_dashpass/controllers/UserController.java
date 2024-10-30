package agileavengers.southwest_dashpass.controllers;

import agileavengers.southwest_dashpass.models.*;
import agileavengers.southwest_dashpass.services.CustomerService;
import agileavengers.southwest_dashpass.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
    private CustomerService customerService;
    @Autowired
    private UserService userService;
    @GetMapping("/signup")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("customer", new Customer());
        return "signup";
    }

    @PostMapping("/signup")
    public String registerCustomer(@Validated @ModelAttribute("user") User user,
                                   BindingResult result,
                                   @RequestParam String userType,
                                   Model model) {

        // Check for validation errors in user fields
        if (result.hasErrors()) {
            model.addAttribute("error", "There was an error in the form submission.");
            return "signup";
        }

        // Check if the username already exists
//        if (userService.existsByUsername(user.getUsername())) {
//            result.rejectValue("username", "error.user", "Username already exists.");
//            model.addAttribute("error", "Username already exists.");
//            return "signup";
//        }

        // Set the userType
        try {
            user.setUserType(UserType.valueOf(userType.toUpperCase()));  // Set user type (CUSTOMER or EMPLOYEE)
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", "Invalid user type specified.");
            return "signup";
        }

        // Create the customer and associate it with the saved user
        Customer customer = new Customer();
        customer.setUser(user);  // Link the customer to the saved user

        // Set additional customer attributes
        customer.setCanFly(true);  // Default values
        customer.setCanPurchaseDashPass(true);
        customer.setCanPurchaseFlight(true);

        // Save the customer
        customerService.registerCustomer(customer, user);

        System.out.println("Registration process completed for user: " + user.getUsername());

        // Redirect to the login page after successful registration
        return "redirect:/login";
    }


}
