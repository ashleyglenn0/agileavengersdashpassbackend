package agileavengers.southwest_dashpass.controllers;

import agileavengers.southwest_dashpass.models.*;
import agileavengers.southwest_dashpass.services.CustomerService;
import agileavengers.southwest_dashpass.services.EmployeeService;
import agileavengers.southwest_dashpass.services.PendingCustomerService;
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

import java.util.Optional;

@Controller
public class UserController {
    @Autowired
    private CustomerService customerService;
    @Autowired
    private UserService userService;
    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private PendingCustomerService pendingCustomerService;

    @GetMapping("/signup")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("customer", new Customer());
        model.addAttribute("employee", new Employee());
        return "signup";
    }

    @PostMapping("/signup")
    public String registerUser(@Validated @ModelAttribute("user") User user,
                               BindingResult result,
                               @RequestParam String userType,
                               @RequestParam(required = false) String role,
                               Model model) {

        // Check for validation errors in user fields
        if (result.hasErrors()) {
            model.addAttribute("error", "There was an error in the form submission.");
            return "signup";
        }

        // Set the userType
        try {
            user.setUserType(UserType.valueOf(userType.toUpperCase()));
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", "Invalid user type specified.");
            return "signup";
        }

        if (userType.equalsIgnoreCase("CUSTOMER")) {
            // Create and configure Customer
            Customer customer = new Customer();
            customer.setUser(user);
            customer.setCanFly(true);
            customer.setCanPurchaseDashPass(true);
            customer.setCanPurchaseFlight(true);

            // Save Customer
            customerService.registerCustomer(customer, user);

        } else if (userType.equalsIgnoreCase("EMPLOYEE")) {
            if (role == null || role.isEmpty()) {
                model.addAttribute("error", "Role is required for employee registration.");
                return "signup";
            }

            // Create and configure Employee
            Employee employee = new Employee();
            employee.setUser(user);

            try {
                employee.setRole(Role.valueOf(role.toUpperCase()));
            } catch (IllegalArgumentException e) {
                model.addAttribute("error", "Invalid role specified for employee.");
                return "signup";
            }

            employee.setCanAddCustomer(true);
            employee.setCanSellDashPass(true);

            // Save Employee
            employeeService.registerEmployee(employee, user);
        }

        // Redirect to the login page after successful registration
        return "redirect:/login";
    }

    // Complete registration method
    @GetMapping("/complete-registration")
    public String showCompleteRegistrationForm() {
        return "completeregistration"; // The Thymeleaf template name
    }

    @PostMapping("/complete-registration")
    public String completeRegistration(@RequestParam String email,
                                       @RequestParam String username,
                                       @RequestParam String password) {
        Optional<PendingCustomer> pendingCustomerOpt = pendingCustomerService.findByEmail(email);
        if (pendingCustomerOpt.isEmpty()) {
            return "error"; // Or handle this case with an error message
        }

        PendingCustomer pendingCustomer = pendingCustomerOpt.get();

        // Create and set up the User entity
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);  // Encrypt the password
        user.setFirstName(pendingCustomer.getFirstName());
        user.setLastName(pendingCustomer.getLastName());
        user.setEmail(pendingCustomer.getEmail());
        userService.saveUser(user);

        // Create and set up the Customer entity, linking it to the User
        Customer customer = new Customer();
        customer.setUser(user);  // Associate the User with the Customer
        customerService.saveCustomer(customer);

        // Delete the PendingCustomer record to clean up
        pendingCustomerService.deletePendingCustomer(pendingCustomer);

        return "redirect:/login";  // Redirect to the login page upon successful registration
    }


}
