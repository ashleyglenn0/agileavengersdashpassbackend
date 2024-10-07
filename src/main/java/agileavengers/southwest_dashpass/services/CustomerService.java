package agileavengers.southwest_dashpass.services;
import agileavengers.southwest_dashpass.models.Customer;
import agileavengers.southwest_dashpass.models.User;
import agileavengers.southwest_dashpass.models.UserType;
import agileavengers.southwest_dashpass.repository.CustomerRepository;
import agileavengers.southwest_dashpass.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomerService {
    private CustomerRepository customerRepository;
    private UserService userService;
    private UserRepository userRepository;

    private EntityManager entityManager;

    private SessionFactory sessionFactory;
    @Autowired
    public CustomerService(CustomerRepository customerRepository, UserService userService, UserRepository userRepository,
                           EntityManager entityManager, SessionFactory sessionFactory) {
        this.customerRepository = customerRepository;
        this.userService = userService;
        this.userRepository = userRepository;
        this.entityManager = entityManager;
        this.sessionFactory = sessionFactory;
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

    public Integer getNumberOfDashPassesAvailableForPurchase(Long customerId) {
        Customer customer = findCustomerById(customerId);
        // Number of DashPasses that are not attached to any reservation
        return customer.getMaxNumberOfDashPasses() - customer.getDashPasses().size();
    }

    public Integer getNumberOfDashPassesAvailableToRedeem(Long customerId) {
        Customer customer = findCustomerById(customerId);
        return customer.getDashPasses().size();
    }

    public Integer getNumberOfDashPassesInUse(Long customerId) {
        Customer customer = findCustomerById(customerId);
        // Assuming DashPassReservations represent the redeemed DashPasses
        return customer.getDashPassReservations().size();
    }

    public Integer getTotalNumberOfDashPasses(Long customerId) {
        Customer customer = findCustomerById(customerId);
        // Some logic to get total number of dash passes
        return customer.getDashPassReservations().size() + customer.getDashPasses().size();
    }
    public Integer getMaxNumberOfDashPasses(Long customerId) {
        Customer customer = findCustomerById(customerId);
        // Some logic to get total number of dash passes
        return customer.getMaxNumberOfDashPasses();
    }

    private void clearCaches() {
        // Clear first-level cache (per-transaction cache)
        entityManager.clear();

        // Clear second-level cache (shared cache for the session factory)
        sessionFactory.getCache().evictAllRegions();
    }

}
