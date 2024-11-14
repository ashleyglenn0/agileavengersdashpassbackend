package agileavengers.southwest_dashpass.repository;

import agileavengers.southwest_dashpass.models.PendingCustomer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PendingCustomerRepository extends JpaRepository<PendingCustomer, Long> {
    Optional<PendingCustomer> findByEmail(String email);
}
