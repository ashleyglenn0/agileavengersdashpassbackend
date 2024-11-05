package agileavengers.southwest_dashpass.controllers;

import agileavengers.southwest_dashpass.models.*;
import agileavengers.southwest_dashpass.repository.ShiftRepository;
import agileavengers.southwest_dashpass.services.CustomerService;
import agileavengers.southwest_dashpass.services.EmployeeService;
import agileavengers.southwest_dashpass.services.PendingCustomerService;
import agileavengers.southwest_dashpass.services.SupportRequestService;
import agileavengers.southwest_dashpass.utils.AnnouncementGenerator;
import agileavengers.southwest_dashpass.utils.ShiftGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
public class EmployeeDashboardController {
    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private ShiftRepository shiftRepository;
    @Autowired
    private ShiftGenerator shiftGenerator;
    @Autowired
    private SupportRequestService supportRequestService;
    @Autowired
    private PendingCustomerService pendingCustomerService;
    @Autowired
    private CustomerService customerService;

    @GetMapping("/employee/{employeeId}/employeedashboard")
    public String showEmployeeDashboard(@PathVariable Long employeeId, Model model, Principal principal) {
        // Fetch employee by ID
        Employee employee = employeeService.findEmployeeById(employeeId);

        // If employee is not found, show a custom error page
        if (employee == null) {
            model.addAttribute("error", "Employee not found with ID: " + employeeId);
            return "error/customer-not-found";  // You should have a custom error page
        }

        // Ensure the employee belongs to the currently logged-in user
        if (!employee.getUser().getUsername().equals(principal.getName())) {
            return "error/403";  // Show a 403 error page if access is unauthorized
        }

        // Fetch shifts, announcements, and role
        List<Shift> shifts = shiftRepository.findByEmployeeId(employeeId);
        List<Announcement> announcements = AnnouncementGenerator.generateRandomAnnouncements(3);
        Role role = employee.getRole();

        // Fetch only the first 3 or 4 support requests if the employee is SUPPORT or MANAGER
        if (role == Role.SUPPORT || role == Role.MANAGER) {
            List<SupportRequest> supportRequests = supportRequestService.getRecentSupportRequests(4);
            model.addAttribute("supportRequests", supportRequests);
        }

        // Add employee data to the model
        model.addAttribute("employee", employee);
        model.addAttribute("shifts", shifts);
        model.addAttribute("announcements", announcements);
        model.addAttribute("role", role);

        return "employeedashboard";
    }


    @PostMapping("/employee/{employeeId}/requestShiftChange")
    public String requestShiftChange(@PathVariable Long employeeId) {
        Employee employee = employeeService.findEmployeeById(employeeId); // Assuming you have this method
        supportRequestService.createShiftChangeRequest(employee);
        return "redirect:/employee/{employeeId}/employeedashboard"; // Redirect to dashboard with a message if needed
    }

    @GetMapping("/employee/{employeeId}/addcustomer")
    public String showAddCustomerForm(@PathVariable Long employeeId, Model model) {
        // Add any model attributes if needed, such as employee information for context
        model.addAttribute("employeeId", employeeId); // Optional: pass employeeId for tracking
        return "addcustomer"; // This is the name of your Thymeleaf template for the Add Customer form
    }

    @GetMapping("/employee/{employeeId}/managerequests")
    public String showManageRequests(@PathVariable Long employeeId, Model model) {
        Employee employee = employeeService.findEmployeeById(employeeId);
        if (employee == null) {
            model.addAttribute("error", "Employee not found.");
            return "error";
        }

        List<SupportRequest> supportRequests = supportRequestService.findActiveSupportRequests(); // Fetch all requests for management
        model.addAttribute("employee", employee);
        model.addAttribute("supportRequests", supportRequests);

        return "support-requests-management"; // View name
    }

    @GetMapping("/employee/{employeeId}/supportrequest/{supportRequestId}/details")
    public String showSupportRequestDetails(@PathVariable Long employeeId,
                                            @PathVariable Long supportRequestId,
                                            Model model) {
        Employee employee = employeeService.findEmployeeById(employeeId);
        SupportRequest supportRequest = supportRequestService.findSupportRequestById(supportRequestId);

        if (supportRequest == null) {
            model.addAttribute("error", "Support request not found.");
            return "error";
        }

        model.addAttribute("employee", employee);
        model.addAttribute("supportRequest", supportRequest);

        return "support-requests-details";
    }

    @GetMapping("/employee/{employeeId}/archivedrequests")
    public String showArchivedRequests(@PathVariable Long employeeId, Model model) {
        Employee employee = employeeService.findEmployeeById(employeeId);
        List<SupportRequest> closedRequests = supportRequestService.findClosedSupportRequests();

        model.addAttribute("employee", employee);
        model.addAttribute("closedRequests", closedRequests);

        return "archived-support-requests";
    }

    @PostMapping("/employee/{employeeId}/add-customer")
    public String addPendingCustomer(@PathVariable Long employeeId, Model model, @ModelAttribute PendingCustomer pendingCustomer) {
        Employee employee = employeeService.findEmployeeById(employeeId);
        if (employee == null) {
            model.addAttribute("error", "Employee not found.");
            return "error";  // Return an error view if the employee doesn't exist
        }

        model.addAttribute("employee", employee);
        pendingCustomerService.savePendingCustomer(pendingCustomer);

        return "redirect:/employee/" + employeeId + "/employeedashboard";  // Redirect to employee's dashboard
    }

    @GetMapping("/employee/{employeeId}/viewcustomer/customer/{customerId}/details")
    public String showCustomerDetails(@PathVariable Long employeeId,
                                      @PathVariable Long customerId,
                                      Model model,
                                      Authentication authentication) {
        // Retrieve the customer by ID
        Optional<Customer> customerOpt = customerService.findById(customerId);
        if (customerOpt.isPresent()) {
            Customer customer = customerOpt.get();
            model.addAttribute("customer", customer);
        } else {
            model.addAttribute("error", "Customer not found.");
            return "error";  // Redirect or display an error page if the customer doesn't exist
        }

        // Retrieve the employee by ID
        Optional<Employee> employeeOpt = employeeService.findById(employeeId);
        if (employeeOpt.isPresent()) {
            Employee employee = employeeOpt.get();
            model.addAttribute("employee", employee);
        } else {
            model.addAttribute("error", "Employee not found.");
            return "error";  // Redirect or display an error page if the employee doesn't exist
        }

        // Check if the user has support or manager roles
        boolean canEditUsernameAndEmail = authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_SUPPORT") ||
                        grantedAuthority.getAuthority().equals("ROLE_MANAGER"));
        model.addAttribute("canEditUsernameAndEmail", canEditUsernameAndEmail);

        return "customerdetails";
    }

    @GetMapping("/employee/{employeeId}/search-customer")
    public String searchCustomer(@PathVariable Long employeeId,
                                 @RequestParam(value = "name", required = false) String name,
                                 Model model) {
        // Retrieve the employee to add to the model
        Optional<Employee> employeeOpt = employeeService.findById(employeeId);
        if (employeeOpt.isPresent()) {
            model.addAttribute("employee", employeeOpt.get());
        } else {
            model.addAttribute("error", "Employee not found.");
            return "error";  // Display an error page if the employee doesn't exist
        }

        // Check if a search term was provided
        if (name != null && !name.isEmpty()) {
            // Perform the search if a name is provided
            List<Customer> customers = customerService.findCustomersByName(name);
            model.addAttribute("customers", customers);
            model.addAttribute("customerName", name);  // Prepopulate the search input with the search term
        }

        // Return the template for customer search
        return "searchcustomer";
    }



}
