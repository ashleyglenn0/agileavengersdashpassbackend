package agileavengers.southwest_dashpass.services;

import agileavengers.southwest_dashpass.models.Customer;
import agileavengers.southwest_dashpass.models.Employee;
import agileavengers.southwest_dashpass.models.User;
import agileavengers.southwest_dashpass.models.UserType;
import agileavengers.southwest_dashpass.repository.CustomerRepository;
import agileavengers.southwest_dashpass.repository.EmployeeRepository;
import agileavengers.southwest_dashpass.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final CustomerRepository customerRepository;
    private final EmployeeService employeeService;
    private final CustomerService customerService;

    // Constructor injection is preferred
    @Autowired
    public UserService(UserRepository userRepository, EmployeeRepository employeeRepository,
                       CustomerRepository customerRepository, EmployeeService employeeService,
                       CustomerService customerService) {
        this.userRepository = userRepository;
        this.employeeRepository = employeeRepository;
        this.customerRepository = customerRepository;
        this.employeeService = employeeService;
        this.customerService = customerService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Find the user by username
        User user = userRepository.findByUsername(username);

        // Throw exception if the user is not found
        if (user == null) {
            throw new UsernameNotFoundException("User not found with username " + username);
        }

        // Assign authorities based on the UserType
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getUserType().name()));  // Convert UserType to ROLE_

        // Create a Spring Security User object with the user's credentials and authorities
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.isEnabled(),  // Set enabled status
                true,  // accountNonExpired
                true,  // credentialsNonExpired
                true,  // accountNonLocked
                authorities
        );
    }

    // Save or update the user based on the UserType
    public void saveUser(User user) {
        if (user.getUserType() == UserType.EMPLOYEE && user instanceof Employee) {
            // Call the employeeService's registerEmployee method for additional logic
            Employee employee = (Employee) user;
            employeeService.registerEmployee(employee.getEmployeeId(), employee.getFirstName(), employee.getLastName(),
                    employee.getUsername(), employee.getPassword(), employee.getEmail(), employee.getRole());
        } else if (user.getUserType() == UserType.CUSTOMER && user instanceof Customer) {
            // Call the customerService's registerCustomer method for additional logic
            Customer customer = (Customer) user;
            customerService.registerCustomer(customer.getCustomerId(), customer.getFirstName(), customer.getLastName(), customer.getUsername(), customer.getEmail(), customer.getPassword());
            customerRepository.save(customer); // Save to CustomerRepository
        } else {
            // Save to the general UserRepository if no specific type is found
            userRepository.save(user);
        }
    }
}
