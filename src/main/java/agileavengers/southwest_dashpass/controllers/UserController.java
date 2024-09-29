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
        return "redirect:/signup.html";
    }

    @PostMapping("/signup")
    public String registerUser(@Validated @ModelAttribute("user") User user, BindingResult result, Model model) {
       if(result.hasErrors()){
           return "redirect:/signup.html?error=username";
       }
       if(userService.loadUserByUsername(user.getUsername()) != null){
           return "redirect:/signup.html?error=username";
       }

       user.setPassword(passwordEncoder.encode(user.getPassword()));

       if(user.getUserType() == UserType.EMPLOYEE){
           user.setUserType(UserType.EMPLOYEE);
       } else {
           user.setUserType(UserType.CUSTOMER);
       }

       userService.saveUser(user);

       return "redirect:/login.html";
    }
}
