package agileavengers.southwest_dashpass.controllers;

import agileavengers.southwest_dashpass.dtos.CustomerUpdateDTO;
import agileavengers.southwest_dashpass.dtos.EmployeeDTO;
import agileavengers.southwest_dashpass.models.*;
import agileavengers.southwest_dashpass.repository.ShiftRepository;
import agileavengers.southwest_dashpass.services.*;
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
import java.util.*;
import java.util.stream.Collectors;

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
    @Autowired
    private ReservationService reservationService;
    @Autowired
    private SalesRecordService salesRecordService;
    @Autowired
    private ShiftService shiftService;

    @GetMapping("/employee/{employeeId}/employeedashboard")
    public String showEmployeeDashboard(@PathVariable Long employeeId, Model model, Principal principal) {
        Employee employee = employeeService.findEmployeeById(employeeId);

        if (employee == null || !employee.getUser().getUsername().equals(principal.getName())) {
            return "error/403"; // Unauthorized
        }
        // Generate today's shifts for all employees if missing
        shiftService.generateAndSaveShiftsForAllEmployees();

        // Fetch sales data
        Long teamTotalSales = salesRecordService.calculateTeamTotalSales();
        Map<String, Object> topPerformerData = salesRecordService.getTopPerformingEmployee();
        Map<String, Long> salesBreakdown = salesRecordService.getSalesBreakdown();
        // Calculate individual counts
        Map<String, Long> individualSalesBreakdown = salesRecordService.getIndividualSalesBreakdown(employeeId);

        List<Shift> allShifts = shiftRepository.findByEmployeeId(employeeId);
        List<Shift> uniqueShifts = allShifts.stream()
                .filter(shift -> shift.getDate().isAfter(LocalDate.now().atStartOfDay()) || shift.getDate().isEqual(LocalDate.now().atStartOfDay()))
                .filter(new HashSet<>()::add) // Ensures only unique dates are kept
                .limit(4) // Only take the first 4 unique upcoming shifts
                .collect(Collectors.toList());
        model.addAttribute("shifts", uniqueShifts);

        List<Announcement> announcements = AnnouncementGenerator.generateRandomAnnouncements(3);
        Role role = employee.getRole();
        LocalDate date = LocalDate.now();

        // Get scheduled employees for today
        List<EmployeeDTO> scheduledEmployees = shiftService.getScheduledEmployeesForToday(date);


        // Fetch only the first 3 or 4 support requests if the employee is SUPPORT or MANAGER
        if (role == Role.SUPPORT || role == Role.MANAGER) {
            List<SupportRequest> supportRequests = supportRequestService.getRecentSupportRequests(4);
            model.addAttribute("supportRequests", supportRequests);
        }


        // Add attributes to the model
        model.addAttribute("role", role);
        model.addAttribute("employee", employee);
        model.addAttribute("teamTotalSales", teamTotalSales);
        model.addAttribute("topPerformer", topPerformerData);
        model.addAttribute("employeeSalesCount", salesBreakdown.get("employeeSales"));
        model.addAttribute("customerSalesCount", salesBreakdown.get("customerSales"));
        model.addAttribute("announcements", announcements);
        model.addAttribute("scheduledEmployees", scheduledEmployees); // Add scheduled employees
        model.addAttribute("dashPassSalesCount", individualSalesBreakdown.get("dashPassSales"));
        model.addAttribute("flightSalesCount", individualSalesBreakdown.get("flightSales"));

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
    @GetMapping("/employee/{employeeId}/escalatedrequests")
    public String showEscalatedRequests(@PathVariable Long employeeId, Model model) {
        Employee employee = employeeService.findEmployeeById(employeeId);
        List<SupportRequest> escalatedRequests = supportRequestService.findEscalatedSupportRequests();

        model.addAttribute("employee", employee);
        model.addAttribute("escalatedRequests", escalatedRequests);

        return "escalated-support-requests";
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
        Optional<Customer> customerOpt = customerService.findById(customerId);
        if (customerOpt.isEmpty()) {
            model.addAttribute("error", "Customer not found.");
            return "error";
        }
        Employee employee = employeeService.findEmployeeById(employeeId);
        // Populate the CustomerUpdateDTO
        CustomerUpdateDTO customerUpdateDTO = new CustomerUpdateDTO();
        Customer customer = customerOpt.get();
        if (customer.getUser() != null) {
            customerUpdateDTO.setFirstName(customer.getUser().getFirstName());
            customerUpdateDTO.setLastName(customer.getUser().getLastName());
            customerUpdateDTO.setEmail(customer.getUser().getEmail());
            customerUpdateDTO.setUsername(customer.getUser().getUsername());
        }

        model.addAttribute("customerUpdateDTO", customerUpdateDTO);

        // Set canEdit and canEditUsernameAndEmail based on role
        boolean canEdit = true; // Assuming all roles can edit first name and last name
        boolean canEditUsernameAndEmail = employee.getRole() == Role.SUPPORT || employee.getRole() == Role.MANAGER;

        model.addAttribute("canEdit", canEdit);
        model.addAttribute("canEditUsernameAndEmail", canEditUsernameAndEmail);
        model.addAttribute("employee", employee);
        model.addAttribute("customer", customer);

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

    @GetMapping("/employee/{employeeId}/customer/{customerId}/reservations")
    public String viewCustomerReservations(@PathVariable Long employeeId, @PathVariable Long customerId, Model model) {
        Employee employee = employeeService.findEmployeeById(employeeId);
        Customer customer = customerService.findCustomerById(customerId);
        List<Reservation> reservations = reservationService.getReservationsForCustomer(customerId);
        Role role = employee.getRole();

        model.addAttribute("employee", employee);
        model.addAttribute("customer", customer);
        model.addAttribute("reservations", reservations);
        model.addAttribute("userRole", role); // Assuming Role has a getName() method
        System.out.println(role);

        return "reservationList"; // This is the template name for your reservation list view
    }

    @GetMapping("/employee/{employeeId}/sales")
    public String viewSalesByType(
            @PathVariable Long employeeId,
            @RequestParam(name = "salesType", defaultValue = "all") String salesType,
            Model model) {

        // Get the employee information
        Employee employee = employeeService.findEmployeeById(employeeId);
        if (employee == null) {
            model.addAttribute("error", "Employee not found.");
            return "error";
        }

        // Fetch sales based on the salesType parameter
        List<SalesRecord> salesRecords;
        if (salesType.equals("mySales")) {
            salesRecords = salesRecordService.findSalesByEmployee(employeeId);
        } else {
            salesRecords = salesRecordService.findSalesByType(salesType);
        }

        Map<Long, String> salesDateMap = salesRecords.stream()
                .collect(Collectors.toMap(
                        SalesRecord::getId,
                        record -> record.getSaleDate() != null ? record.getSaleDate().toString() : "N/A"
                ));

        // Add necessary attributes to the model
        model.addAttribute("employeeId", employeeId);
        model.addAttribute("salesType", salesType);
        model.addAttribute("salesRecords", salesRecords);
        model.addAttribute("salesDateMap", salesDateMap);

        return "saleslist"; // Return the shared template for displaying sales records
    }

    @PostMapping("/employee/{employeeId}/customer/{customerId}/update")
    public String updateCustomerDetails(
            @PathVariable Long employeeId,
            @PathVariable Long customerId,
            @ModelAttribute("customerUpdateDTO") CustomerUpdateDTO customerDto,
            Model model) {

        Employee employee = employeeService.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));

        Role employeeRole = employee.getRole();
        customerDto.setCustomerId(customerId); // Ensure the DTO has the correct customer ID

        employeeService.editCustomerDetails(customerDto, employeeRole);

        return "redirect:/employee/" + employeeId + "/employeedashboard";
    }

}
