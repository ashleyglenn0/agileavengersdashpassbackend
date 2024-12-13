package agileavengers.southwest_dashpass.services;

import agileavengers.southwest_dashpass.models.PendingCustomer;
import agileavengers.southwest_dashpass.repository.PendingCustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PendingCustomerService {
    @Autowired
    private PendingCustomerRepository pendingCustomerRepository;

    public void savePendingCustomer(PendingCustomer pendingCustomer) {
        pendingCustomerRepository.save(pendingCustomer);
    }

    public Optional<PendingCustomer> findByEmail(String email) {
        return pendingCustomerRepository.findByEmail(email);
    }

    // Delete a PendingCustomer entry
    public void deletePendingCustomer(PendingCustomer pendingCustomer) {
        pendingCustomerRepository.delete(pendingCustomer);
    }

}
