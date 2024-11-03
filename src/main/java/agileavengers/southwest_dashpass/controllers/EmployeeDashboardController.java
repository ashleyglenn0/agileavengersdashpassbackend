package agileavengers.southwest_dashpass.controllers;

import agileavengers.southwest_dashpass.models.*;
import agileavengers.southwest_dashpass.repository.ShiftRepository;
import agileavengers.southwest_dashpass.services.EmployeeService;
import agileavengers.southwest_dashpass.utils.AnnouncementGenerator;
import agileavengers.southwest_dashpass.utils.ShiftGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

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
    @GetMapping("/employee/{employeeID}/employeedashboard")
    public String showCustomerDashboard(@PathVariable Long employeeID, Model model, Principal principal) {
        // Fetch employee by ID
        Employee employee = employeeService.findEmployeeById(employeeID);

        // If employee is not found, show a custom error page
        if (employee == null) {
            model.addAttribute("error", "Employee not found with ID: " + employeeID);
            return "error/customer-not-found";  // You should have a custom error page
        }


        // Ensure the customer belongs to the currently logged-in user
        if (!employee.getUser().getUsername().equals(principal.getName())) {
            return "error/403";  // Show a 403 error page if access is unauthorized
        }

        // Check if the employee already has shifts; if not, generate them
        List<Shift> shifts = shiftRepository.findByEmployeeId(employeeID);
        if (shifts.isEmpty()) {
            shifts = shiftGenerator.generateRandomShiftsForEmployee(employee, 2);
        }
        List<Announcement> announcements = AnnouncementGenerator.generateRandomAnnouncements(2);


        // Add customer information to the model
        model.addAttribute("employee", employee);
        model.addAttribute("shifts", shifts);
        model.addAttribute("announcements", announcements);

        return "employeedashboard";  // Return the dashboard view
    }

}
