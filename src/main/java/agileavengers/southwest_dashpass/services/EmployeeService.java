package agileavengers.southwest_dashpass.services;

import agileavengers.southwest_dashpass.dtos.CustomerUpdateDTO;
import agileavengers.southwest_dashpass.dtos.EmployeeDTO;
import agileavengers.southwest_dashpass.models.*;
import agileavengers.southwest_dashpass.repository.CustomerRepository;
import agileavengers.southwest_dashpass.repository.EmployeeRepository;
import agileavengers.southwest_dashpass.repository.TimeOffRequestRepository;
import agileavengers.southwest_dashpass.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EmployeeService {
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private TimeOffRequestRepository timeOffRequestRepository;
    @Autowired
    private CustomerRepository customerRepository;

    @Transactional
    public void registerEmployee(Employee employee, User user) {
        // Save the User directly using UserRepository
        if (userRepository.existsByUsername(user.getUsername())) {
            System.out.println("Duplicate username found: " + user.getUsername());
            throw new RuntimeException("Username already exists");
        }

        // Encode the password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        System.out.println("Saving user: " + user.getUsername() + " inside save user method");

        // Save the user
         userRepository.save(user);
        // Save Employee with associated User
        employeeRepository.save(employee);
    }
    public Employee findEmployeeById(Long employeeId) {
        return employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found with ID: " + employeeId));
    }

    public Optional<Employee> findById(Long employeeId) {
        return employeeRepository.findById(employeeId);
    }

    // Get all employees scheduled for today
    public List<EmployeeDTO> getScheduledEmployeesForToday() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        return employeeRepository.findEmployeesScheduledForDate(startOfDay, endOfDay)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private EmployeeDTO convertToDTO(Employee employee) {
        EmployeeDTO dto = new EmployeeDTO();
        dto.setId(employee.getId());
        dto.setRole(employee.getRole());
        dto.setShiftStartTime(dto.getShiftStartTime());
        dto.setShiftEnd(dto.getShiftEndTime());
        return dto;
    }

    public List<TimeOffRequest> getAllTimeOffRequests() {
        return timeOffRequestRepository.findAll();
    }

    @Transactional
    public Customer editCustomerDetails(CustomerUpdateDTO customerDto, Role role) {
        Customer existingCustomer = customerRepository.findById(customerDto.getCustomerId())
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        User user = existingCustomer.getUser();
        if (user == null) {
            throw new IllegalStateException("The customer does not have an associated user.");
        }

        // Update fields
        user.setFirstName(customerDto.getFirstName());
        user.setLastName(customerDto.getLastName());

        if (role == Role.SUPPORT || role == Role.MANAGER) {
            user.setEmail(customerDto.getEmail());
            user.setUsername(customerDto.getUsername());
        }

        return customerRepository.save(existingCustomer);
    }


}
