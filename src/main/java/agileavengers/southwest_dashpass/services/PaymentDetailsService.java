package agileavengers.southwest_dashpass.services;

import agileavengers.southwest_dashpass.dtos.PaymentDetailsDTO;
import agileavengers.southwest_dashpass.models.Customer;
import agileavengers.southwest_dashpass.models.PaymentDetails;
import agileavengers.southwest_dashpass.repository.PaymentDetailsRepository;
import agileavengers.southwest_dashpass.utils.EncryptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PaymentDetailsService {

    private final PaymentDetailsRepository paymentDetailsRepository;
    private final EncryptionUtils encryptionUtils;

    @Autowired
    public PaymentDetailsService(PaymentDetailsRepository paymentDetailsRepository, EncryptionUtils encryptionUtils) {
        this.paymentDetailsRepository = paymentDetailsRepository;
        this.encryptionUtils = encryptionUtils;
    }

    public void savePaymentDetails(PaymentDetailsDTO paymentDetailsDTO, Customer customer) {
        if (paymentDetailsDTO.isSavePaymentDetails()) {
            PaymentDetails paymentDetails = new PaymentDetails();
            paymentDetails.setCustomer(customer);
            paymentDetails.setEncryptedCardNumber(encryptionUtils.encrypt(paymentDetailsDTO.getCardNumber()));
            paymentDetails.setEncryptedExpirationDate(encryptionUtils.encrypt(paymentDetailsDTO.getExpirationDate()));
            paymentDetails.setEncryptedCVV(encryptionUtils.encrypt(paymentDetailsDTO.getCvv()));
            paymentDetails.setEncryptedBillingZip(encryptionUtils.encrypt(paymentDetailsDTO.getZipCode()));
            paymentDetails.setEncryptedNameOnCard(encryptionUtils.encrypt(paymentDetailsDTO.getCardName()));
            paymentDetailsRepository.save(paymentDetails);
        }
    }

    // Get decrypted payment details for display, showing only last 4 digits of card number
    public PaymentDetailsDTO getDecryptedPaymentDetails(PaymentDetails paymentDetails) {
        PaymentDetailsDTO dto = new PaymentDetailsDTO();

        // Decrypt sensitive fields
        String decryptedCardNumber = encryptionUtils.decrypt(paymentDetails.getEncryptedCardNumber());
        dto.setCardNumber("**** **** **** " + decryptedCardNumber.substring(decryptedCardNumber.length() - 4));
        dto.setCvv("***");  // Optionally mask CVV for security
        dto.setExpirationDate(encryptionUtils.decrypt(paymentDetails.getEncryptedExpirationDate()));

        return dto;
    }

    // Check if a payment method with the encrypted card number exists in the database
    public boolean checkIfPaymentMethodExists(String cardNumber) {
        String encryptedCardNumber = encryptionUtils.encrypt(cardNumber);
        return paymentDetailsRepository.existsByEncryptedCardNumber(encryptedCardNumber);
    }

    public List<PaymentDetails> findPaymentMethodsByCustomer(Long customerId) {
        return paymentDetailsRepository.findPaymentDetailsByCustomerId(customerId);
    }

}
