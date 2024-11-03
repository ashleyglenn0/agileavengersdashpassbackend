package agileavengers.southwest_dashpass.services;

import agileavengers.southwest_dashpass.models.Customer;
import agileavengers.southwest_dashpass.models.Employee;
import agileavengers.southwest_dashpass.models.Role;
import agileavengers.southwest_dashpass.models.User;
import agileavengers.southwest_dashpass.repository.EmployeeRepository;
import agileavengers.southwest_dashpass.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class EmployeeService {
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

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
                .orElseThrow(() -> new RuntimeException("Customer not found with ID: " + employeeId));
    }

}
