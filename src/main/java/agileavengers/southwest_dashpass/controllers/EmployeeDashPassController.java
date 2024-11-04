package agileavengers.southwest_dashpass.controllers;

import agileavengers.southwest_dashpass.models.Customer;
import agileavengers.southwest_dashpass.models.DashPass;
import agileavengers.southwest_dashpass.models.Employee;
import agileavengers.southwest_dashpass.services.CustomerService;
import agileavengers.southwest_dashpass.services.DashPassService;
import agileavengers.southwest_dashpass.services.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class EmployeeDashPassController {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private DashPassService dashPassService;

    @GetMapping("/employee/{employeeId}/verifydashpass")
    public String showVerifyDashPass(@PathVariable Long employeeId, Model model) {
        Employee employee = employeeService.findEmployeeById(employeeId);
        model.addAttribute("employee", employee);
        return "verifydashpass";
    }

    @GetMapping("/employee/{employeeId}/searchDashPass")
    public String searchDashPass(@PathVariable Long employeeId,
                                 @RequestParam(required = false) String customerName,
                                 @RequestParam(required = false) Long dashPassId,
                                 Model model) {
        // Fetch the employee for authorization/logging purposes
        Employee employee = employeeService.findEmployeeById(employeeId);
        model.addAttribute("employee", employee);

        // Flag to indicate if a search was performed
        boolean searchPerformed = false;

        // Search by DashPass ID if provided
        if (dashPassId != null) {
            DashPass dashPass = dashPassService.findDashPassByIdWithCustomerUserAndReservation(dashPassId);
            model.addAttribute("dashPass", dashPass);
            searchPerformed = true;
        }
        // Otherwise, search by customer name if provided
        else if (customerName != null && !customerName.isEmpty()) {
            List<Customer> customers = customerService.findCustomersByName(customerName);
            model.addAttribute("customers", customers);
            searchPerformed = true;
        }

        // Add the searchPerformed flag to the model
        model.addAttribute("searchPerformed", searchPerformed);

        return "verifydashpass"; // Return the same view with the results added to the model
    }

    @GetMapping("/employee/{employeeId}/searchCustomer")
    public String searchCustomer(@PathVariable Long employeeId,
                                 @RequestParam("name") String name,
                                 Model model) {
        System.out.println("Received search request from employee ID: " + employeeId + " with name: " + name);

        Employee employee = employeeService.findEmployeeById(employeeId);
        model.addAttribute("employee", employee);

        List<Customer> customers = customerService.findCustomersByName(name);
        System.out.println("Customers passed to the view: " + customers.size());

        if (customers.isEmpty()) {
            System.out.println("No customers found.");
        } else {
            customers.forEach(customer -> System.out.println("Customer passed to view: " + customer.getUser().getFirstName() + " " + customer.getUser().getLastName()));
        }

        model.addAttribute("customers", customers);
        model.addAttribute("searchPerformed", !customers.isEmpty());

        return "verifydashpass"; // This should be your view name
    }

}
