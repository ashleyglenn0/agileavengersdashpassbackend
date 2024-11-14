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
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found with ID: " + customerId));

        // If employeeId is provided, find the employee; otherwise, leave as null
        Employee employee = (employeeId != null) ? employeeRepository.findById(employeeId)
                .orElse(null) : null;

        SupportRequest supportRequest = new SupportRequest();
        supportRequest.setSubject(subject);
        supportRequest.setMessage(message);
        supportRequest.setStatus(SupportRequest.Status.OPEN);
        supportRequest.setPriority(priority);
        supportRequest.setCustomer(customer);
        supportRequest.setCreatedBy(employee); // employee is null if not provided
        supportRequest.setCreatedDate(LocalDateTime.now());

        return supportRequestRepository.save(supportRequest);
    }

    public List<SupportRequest> getRecentSupportRequests(int limit) {
        return supportRequestRepository.findRecentSupportRequests(limit);
    }

    public List<SupportRequest> getSupportRequestsForCustomer(Customer customer) {
        return supportRequestRepository.findByCustomerOrderByCreatedDateDesc(customer);
    }

    public List<SupportRequest> findAllRequests() {
        return supportRequestRepository.findAll(); // Adjust to any specific filtering needs
    }

    // Method to find a support request by ID
    public SupportRequest findSupportRequestById(Long supportRequestId) {
        return supportRequestRepository.findById(supportRequestId)
                .orElseThrow(() -> new IllegalArgumentException("Support request not found with ID: " + supportRequestId));
    }

    // Method to update the status of a support request
    public void updateStatus(Long supportRequestId, SupportRequest.Status newStatus) {
        SupportRequest supportRequest = findSupportRequestById(supportRequestId);
        supportRequest.setStatus(newStatus);
        supportRequestRepository.save(supportRequest);
    }

    public List<SupportRequest> findActiveSupportRequests() {
        return supportRequestRepository.findByStatusNot(SupportRequest.Status.CLOSED);
    }

    public List<SupportRequest> findClosedSupportRequests() {
        return supportRequestRepository.findByStatus(SupportRequest.Status.CLOSED);
    }

    public List<SupportRequest> findEscalatedSupportRequests() {
        return supportRequestRepository.findByStatus(SupportRequest.Status.ESCALATED);
    }

}
