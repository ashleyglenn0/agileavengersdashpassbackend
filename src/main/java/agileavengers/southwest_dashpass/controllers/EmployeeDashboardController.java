package agileavengers.southwest_dashpass.controllers;

import agileavengers.southwest_dashpass.models.*;
import agileavengers.southwest_dashpass.repository.ShiftRepository;
import agileavengers.southwest_dashpass.services.EmployeeService;
import agileavengers.southwest_dashpass.services.SupportRequestService;
import agileavengers.southwest_dashpass.utils.AnnouncementGenerator;
import agileavengers.southwest_dashpass.utils.ShiftGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

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
        return "redirect:/employee/{employeeId}/dashboard"; // Redirect to dashboard with a message if needed
    }

    @GetMapping("/employee/{employeeId}/addcustomer")
    public String showAddCustomerForm(@PathVariable Long employeeId, Model model) {
        // Add any model attributes if needed, such as employee information for context
        model.addAttribute("employeeId", employeeId); // Optional: pass employeeId for tracking
        return "addcustomer"; // This is the name of your Thymeleaf template for the Add Customer form
    }


}
