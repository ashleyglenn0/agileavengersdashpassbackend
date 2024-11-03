package agileavengers.southwest_dashpass.services;

import agileavengers.southwest_dashpass.models.Customer;
import agileavengers.southwest_dashpass.models.User;
import agileavengers.southwest_dashpass.models.UserType;
import agileavengers.southwest_dashpass.repository.CustomerRepository;
import agileavengers.southwest_dashpass.repository.EmployeeRepository;
import agileavengers.southwest_dashpass.repository.UserRepository;

import jakarta.transaction.Transactional;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final CustomerRepository customerRepository;
    private final EmployeeService employeeService;
    private PasswordEncoder passwordEncoder;
    private ApplicationEventPublisher eventPublisher;

    // Constructor injection is preferred
    @Autowired
    public UserService(UserRepository userRepository, EmployeeRepository employeeRepository,
                       CustomerRepository customerRepository, EmployeeService employeeService,
                       PasswordEncoder passwordEncoder, ApplicationEventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.employeeRepository = employeeRepository;
        this.customerRepository = customerRepository;
        this.employeeService = employeeService;
        this.passwordEncoder = passwordEncoder;
        this.eventPublisher = eventPublisher;
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Find the user by username
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username " + username));

        // Log user loading for verification
        System.out.println("Loaded user: " + user.getUsername());

        // Collection to hold authorities
        Collection<GrantedAuthority> authorities = new ArrayList<>();

        // Assign authorities based on the UserType
        if (user.getUserType() == UserType.CUSTOMER) {
            if (user.getCustomer() == null) {
                throw new UsernameNotFoundException("Customer profile not found for username: " + username);
            }
            authorities.add(new SimpleGrantedAuthority("ROLE_CUSTOMER"));
        } else if (user.getUserType() == UserType.EMPLOYEE) {
            if (user.getEmployee() == null) {
                throw new UsernameNotFoundException("Employee profile not found for username: " + username);
            }
            authorities.add(new SimpleGrantedAuthority("ROLE_EMPLOYEE"));
        }

        // Log authorities for debugging
        System.out.println("Authorities: " + authorities);

        // Create and return a Spring Security User object
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                authorities
        );
    }



    // Save or update the user based on the UserType
    @Transactional
    public User saveUser(User user) {
        // Check if the username already exists
        if (userRepository.existsByUsername(user.getUsername())) {
            System.out.println("Duplicate username found: " + user.getUsername());
            throw new RuntimeException("Username already exists");
        }

        // Encode the password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        System.out.println("Saving user: " + user.getUsername() + " inside save user method");

        // Save the user
        return userRepository.save(user);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username " + username));
    }

}
