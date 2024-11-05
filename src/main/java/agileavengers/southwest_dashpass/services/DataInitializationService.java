package agileavengers.southwest_dashpass.services;

import agileavengers.southwest_dashpass.models.Customer;
import agileavengers.southwest_dashpass.models.Employee;
import agileavengers.southwest_dashpass.models.SupportRequest.Priority;
import agileavengers.southwest_dashpass.repository.CustomerRepository;
import agileavengers.southwest_dashpass.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Random;

@Service
public class DataInitializationService {

    @Autowired
    private SupportRequestService supportRequestService;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Value("${initialize.support.requests:false}")
    private boolean initializeSupportRequests;

    @PostConstruct
    public void initializeSupportRequests() {
        if (!initializeSupportRequests) {
            System.out.println("Support request initialization is disabled.");
            return;
        }
        try {
            List<Customer> customers = customerRepository.findAll(); // Fetch some customers for testing

            if (customers.isEmpty()) {
                System.out.println("Ensure customer data exists before generating support requests.");
                return;
            }

            // Fetch the support employee directly
            Employee supportEmployee = employeeRepository.findById(8L)
                    .orElseThrow(() -> new IllegalArgumentException("Support employee with ID 8 not found"));

            String[] subjects = {
                    "Request for Refund",
                    "Problem with Ticket",
                    "Flight Rebooking Needed",
                    "DashPass Validation Issue",
                    "Payment Issue",
                    "Request to Cancel Flight",
                    "Lost Baggage",
                    "Request for Upgrade",
                    "Flight Delay Compensation",
                    "Assistance Needed with Login"
            };

            String[] messages = {
                    "Customer is facing issues with ticket validation.",
                    "Customer requested a refund due to an issue with booking.",
                    "Customer needs help with rebooking the flight.",
                    "DashPass validation is not working for the customer.",
                    "Customer encountered a payment issue during checkout.",
                    "Customer requests cancellation and refund for flight.",
                    "Customer lost their baggage and needs assistance.",
                    "Customer requested an upgrade for the next flight.",
                    "Customer faced delay and is asking for compensation.",
                    "Customer has trouble logging into their account."
            };

            Random random = new Random();

            for (int i = 0; i < 15; i++) {
                Customer customer = customers.get(random.nextInt(customers.size()));
                String subject = subjects[random.nextInt(subjects.length)];
                String message = messages[random.nextInt(messages.length)];
                Priority priority = random.nextBoolean() ? Priority.NORMAL : Priority.URGENT;

                // Use the updated createSupportRequest method with employee ID 8
                supportRequestService.createSupportRequest(customer.getId(), supportEmployee.getId(), subject, message, priority);
            }

            System.out.println("Sample support requests initialized in the database.");
        } catch (Exception e) {
            System.err.println("Error during support request initialization: " + e.getMessage());
            e.printStackTrace();
        }
    }

}


