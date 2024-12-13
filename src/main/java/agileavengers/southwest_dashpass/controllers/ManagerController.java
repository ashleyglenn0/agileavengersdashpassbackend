package agileavengers.southwest_dashpass.controllers;

import agileavengers.southwest_dashpass.dtos.EmployeeDTO;
import agileavengers.southwest_dashpass.models.TimeOffRequest;
import agileavengers.southwest_dashpass.services.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
public class ManagerController {
    @Autowired
    private EmployeeService employeeService;

    @GetMapping("/refreshScheduledEmployees")
    public List<EmployeeDTO> refreshScheduledEmployees() {
        return employeeService.getScheduledEmployeesForToday();
    }

    // Endpoint to view all time-off requests (redirects to another view or loads data)
    @GetMapping("/{employeeId}/timeoffrequests")
    public String viewTimeOffRequests(@PathVariable Long employeeId, Model model) {
        List<TimeOffRequest> requests = employeeService.getAllTimeOffRequests();
        model.addAttribute("timeOffRequests", requests);
        return "timeoffrequests";
    }
}
