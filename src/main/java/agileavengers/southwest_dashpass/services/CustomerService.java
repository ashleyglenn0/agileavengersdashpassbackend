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

    public Integer getNumberOfDashPassesAvailableForPurchase(Long customerId) {
        Customer customer = findCustomerById(customerId);
        // Filter out redeemed DashPasses
        long unredeemedDashPasses = customer.getDashPasses().stream()
                .filter(dashPass -> !dashPass.isRedeemed())
                .count();
        return customer.getMaxNumberOfDashPasses() - (int) unredeemedDashPasses;
    }

    public Integer getNumberOfDashPassesAvailableToRedeem(Long customerId) {
        Customer customer = findCustomerById(customerId);
        // Return only DashPasses that are not yet redeemed
        return (int) customer.getDashPasses().stream()
                .filter(dashPass -> !dashPass.isRedeemed())
                .count();
    }

    public Integer getNumberOfDashPassesInUse(Long customerId) {
        Customer customer = findCustomerById(customerId);
        // Assuming DashPassReservations represent the redeemed DashPasses
        return customer.getDashPassReservations().size();
    }

    public Integer getTotalNumberOfDashPasses(Long customerId) {
        Customer customer = findCustomerById(customerId);
        // Ensure that DashPasses are only counted once
        long dashPassesInReservations = customer.getDashPassReservations().size();
        long dashPassesNotInReservations = customer.getDashPasses().stream()
                .filter(dashPass -> !dashPass.isRedeemed())
                .count();
        return (int) (dashPassesInReservations + dashPassesNotInReservations);
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

    public void useExistingDashPass(Customer customer, Flight flight) {
        DashPass availableDashPass = customer.getDashPasses().stream()
                .filter(dashPass -> !dashPass.isRedeemed())
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No available DashPass"));

        availableDashPass.setRedeemed(true);
        dashPassRepository.save(availableDashPass);

        DashPassReservation reservation = new DashPassReservation();
        reservation.setDashPass(availableDashPass);
        reservation.setFlight(flight);
        reservation.setCustomer(customer);
        dashPassReservationRepository.save(reservation);
    }

    public void save(Customer customer) {
        customerRepository.save(customer);
    }

    public Customer findByUser(User user) {
        return customerRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Customer not found for user: " + user.getUsername()));
    }

}
