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

    // Save payment details for a customer
    // Save payment details for a customer
    public PaymentDetails savePaymentDetails(Customer customer, String cardNumber, String expirationDate, String cvv, String billingZip, String nameOnCard) {
        PaymentDetails paymentDetails = new PaymentDetails(customer, cardNumber, expirationDate, cvv, billingZip, nameOnCard);
        return paymentDetailsRepository.save(paymentDetails);
    }

    // Get payment details for a customer by their ID
    public Optional<PaymentDetails> getPaymentDetailsByCustomerId(Long customerId) {
        return paymentDetailsRepository.findByCustomerId(customerId);
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

    public PaymentDetailsDTO getDecryptedPaymentDetails(PaymentDetails paymentDetails) {
        PaymentDetailsDTO dto = new PaymentDetailsDTO();
        // Decrypt sensitive fields before returning
        dto.setCardNumber(encryptionUtils.decrypt(paymentDetails.getEncryptedCardNumber()));
        dto.setCvv(encryptionUtils.decrypt(paymentDetails.getEncryptedCVV()));
        dto.setExpirationDate(encryptionUtils.decrypt(paymentDetails.getEncryptedExpirationDate()));
        return dto;
    }

    // New Method to check if encrypted card number exists in the database
    public boolean checkIfPaymentMethodExists(String cardNumber) {
        // Encrypt the provided card number to compare with stored encrypted values
        String encryptedCardNumber = encryptionUtils.encrypt(cardNumber);

        // Check if any stored payment details have this encrypted card number
        List<PaymentDetails> paymentDetailsList = paymentDetailsRepository.findAll();
        for (PaymentDetails paymentDetails : paymentDetailsList) {
            if (paymentDetails.getEncryptedCardNumber().equals(encryptedCardNumber)) {
                return true; // Found a match
            }
        }
        return false;
    }

}
