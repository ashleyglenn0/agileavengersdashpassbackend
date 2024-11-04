package agileavengers.southwest_dashpass.services;

import agileavengers.southwest_dashpass.models.Customer;
import agileavengers.southwest_dashpass.models.Employee;
import agileavengers.southwest_dashpass.models.SupportRequest;
import agileavengers.southwest_dashpass.repository.CustomerRepository;
import agileavengers.southwest_dashpass.repository.EmployeeRepository;
import agileavengers.southwest_dashpass.repository.SupportRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SupportRequestService {

    @Autowired
    private SupportRequestRepository supportRequestRepository;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private CustomerRepository customerRepository;

    public SupportRequest createShiftChangeRequest(Employee employee) {
        SupportRequest request = new SupportRequest(
                "Shift Change Request",
                "Employee has requested a shift change.",
                SupportRequest.Status.ESCALATED,
                SupportRequest.Priority.URGENT,
                employee
        );
        return supportRequestRepository.save(request);
    }

    public SupportRequest createSupportRequest(Long customerId, Long employeeId, String subject, String message, SupportRequest.Priority priority) {
        // Find customer
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found with ID: " + customerId));

        // Find employee
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found with ID: " + employeeId));

        // Create and initialize SupportRequest
        SupportRequest supportRequest = new SupportRequest();
        supportRequest.setSubject(subject);
        supportRequest.setMessage(message);
        supportRequest.setStatus(SupportRequest.Status.OPEN); // Default status
        supportRequest.setPriority(priority);
        supportRequest.setCustomer(customer); // Attach customer
        supportRequest.setCreatedBy(employee); // Attach employee as creator
        supportRequest.setCreatedDate(LocalDateTime.now());

        // Save the support request to the database
        SupportRequest savedRequest = supportRequestRepository.save(supportRequest);

        return savedRequest;
    }
    public List<SupportRequest> getRecentSupportRequests(int limit) {
        return supportRequestRepository.findRecentSupportRequests(limit);
    }


}
