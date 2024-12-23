package agileavengers.southwest_dashpass.services;
import agileavengers.southwest_dashpass.models.*;
import agileavengers.southwest_dashpass.repository.CustomerRepository;
import agileavengers.southwest_dashpass.repository.DashPassRepository;
import agileavengers.southwest_dashpass.repository.DashPassReservationRepository;
import agileavengers.southwest_dashpass.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CustomerService {
    private CustomerRepository customerRepository;
    private DashPassReservationRepository dashPassReservationRepository;
    private DashPassRepository dashPassRepository;
    private UserService userService;
    private UserRepository userRepository;

    private EntityManager entityManager;

    private SessionFactory sessionFactory;
    @Autowired
    public CustomerService(CustomerRepository customerRepository, UserService userService, UserRepository userRepository,
                           EntityManager entityManager, SessionFactory sessionFactory,
                           DashPassRepository dashPassRepository, DashPassReservationRepository dashPassReservationRepository) {
        this.customerRepository = customerRepository;
        this.userService = userService;
        this.userRepository = userRepository;
        this.entityManager = entityManager;
        this.sessionFactory = sessionFactory;
        this.dashPassReservationRepository = dashPassReservationRepository;
        this.dashPassRepository = dashPassRepository;
    }

    public Customer findCustomerById(Long customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found with ID: " + customerId));
    }


    @Transactional
    public void registerCustomer(Customer customer, User user) {
        userService.saveUser(user);  // Save the user first
        customer.setUser(user);  // Link the customer to the user
        customer.setId(user.getId()); // Manually set the same ID as the user's

        // Save customer with associated user
        customerRepository.save(customer);
    }



    public void save(Customer customer) {
        customerRepository.save(customer);
    }

    public Customer findByUser(User user) {
        return customerRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Customer not found for user: " + user.getUsername()));
    }

    public List<Customer> findCustomersByName(String fullName) {
        // Split the full name on spaces (assuming first name and last name)
        String[] nameParts = fullName.trim().toLowerCase().split(" ");
        String firstName = "%" + nameParts[0] + "%";
        String lastName = nameParts.length > 1 ? "%" + nameParts[1] + "%" : firstName; // If only one part, search both as the same

        System.out.println("Searching with first name pattern: " + firstName);
        System.out.println("Searching with last name pattern: " + lastName);

        return customerRepository.findCustomersByFirstNameOrLastName(firstName, lastName);
    }

    public void saveCustomer(Customer customer) {
        customerRepository.save(customer);
    }

    public Optional<Customer> findById(Long customerId) {
        return customerRepository.findById(customerId);
    }


}
