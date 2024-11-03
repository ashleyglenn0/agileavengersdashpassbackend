package agileavengers.southwest_dashpass.repository;

import agileavengers.southwest_dashpass.models.PaymentDetails;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentDetailsRepository extends JpaRepository<PaymentDetails, Long> {
    Optional<PaymentDetails> findByCustomerId(Long customerId);

    // New method to check if an encrypted card number exists
    boolean existsByEncryptedCardNumber(String encryptedCardNumber);

    List<PaymentDetails> findPaymentDetailsByCustomerId(Long customerId);

}

