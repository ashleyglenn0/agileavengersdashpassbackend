package agileavengers.southwest_dashpass.services;

import agileavengers.southwest_dashpass.models.Customer;
import agileavengers.southwest_dashpass.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;

public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    public void saveCustomer(Customer customer) {
        customerRepository.save(customer);
    }
}
