package agileavengers.southwest_dashpass.utils;

import agileavengers.southwest_dashpass.models.Employee;
import agileavengers.southwest_dashpass.models.TimeOffRequest;
import agileavengers.southwest_dashpass.models.TimeOffRequestStatus;
import agileavengers.southwest_dashpass.repository.EmployeeRepository;
import agileavengers.southwest_dashpass.repository.TimeOffRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
public class TimeOffRequestGenerator {

    private final TimeOffRequestRepository timeOffRequestRepository;
    private final EmployeeRepository employeeRepository;
    private final Random random = new Random();

    @Autowired
    public TimeOffRequestGenerator(TimeOffRequestRepository timeOffRequestRepository, EmployeeRepository employeeRepository) {
        this.timeOffRequestRepository = timeOffRequestRepository;
        this.employeeRepository = employeeRepository;
    }

    // Generate a specified number of random time-off requests
    public List<TimeOffRequest> generateTimeOffRequests(int count) {
        List<Employee> employees = employeeRepository.findAll();
        List<TimeOffRequest> timeOffRequests = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            // Randomly select an employee
            Employee employee = employees.get(random.nextInt(employees.size()));

            // Generate random start and end dates for time-off within the next month
            LocalDate startDate = LocalDate.now().plusDays(random.nextInt(30));
            LocalDate endDate = startDate.plusDays(1 + random.nextInt(5));  // 1 to 5-day time-off duration

            // Create and populate a TimeOffRequest object
            TimeOffRequest timeOffRequest = new TimeOffRequest();
            timeOffRequest.setEmployee(employee);
            timeOffRequest.setStartDate(startDate);
            timeOffRequest.setEndDate(endDate);
            timeOffRequest.setReason("Personal time off");
            timeOffRequest.setStatus(TimeOffRequestStatus.valueOf(random.nextBoolean() ? "Pending" : "Approved")); // Random status

            // Save the time-off request
            timeOffRequests.add(timeOffRequest);
            timeOffRequestRepository.save(timeOffRequest);
        }

        return timeOffRequests;
    }
}

