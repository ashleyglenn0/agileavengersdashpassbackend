package agileavengers.southwest_dashpass.services;

import agileavengers.southwest_dashpass.dtos.DisplayPaymentDetailsDTO;
import agileavengers.southwest_dashpass.dtos.PaymentDetailsDTO;
import agileavengers.southwest_dashpass.models.Customer;
import agileavengers.southwest_dashpass.models.PaymentDetails;
import agileavengers.southwest_dashpass.repository.PaymentDetailsRepository;
import agileavengers.southwest_dashpass.utils.EncryptionUtils;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PaymentDetailsService {

    private final PaymentDetailsRepository paymentDetailsRepository;
    private final EncryptionUtils encryptionUtils;

    @Autowired
    public PaymentDetailsService(PaymentDetailsRepository paymentDetailsRepository, EncryptionUtils encryptionUtils) {
        this.paymentDetailsRepository = paymentDetailsRepository;
        this.encryptionUtils = encryptionUtils;
    }

    // Save encrypted payment details
    @Transactional
    public void savePaymentDetails(PaymentDetailsDTO paymentDetailsDTO, Customer customer) {
        if (paymentDetailsDTO.isSavePaymentDetails()) {
            PaymentDetails paymentDetails = new PaymentDetails(
                    customer,
                    encryptionUtils.encrypt(paymentDetailsDTO.getCardNumber()),
                    encryptionUtils.encrypt(paymentDetailsDTO.getExpirationDate()),
                    encryptionUtils.encrypt(paymentDetailsDTO.getCvv()),
                    encryptionUtils.encrypt(paymentDetailsDTO.getZipCode()),
                    encryptionUtils.encrypt(paymentDetailsDTO.getCardName())
            );
            System.out.println("Encrypted Card Number: " + paymentDetails.getEncryptedCardNumber());
            paymentDetailsRepository.save(paymentDetails);
            System.out.println("Payment details saved with ID: " + paymentDetails.getId());
        }
    }

    // Retrieve all payment methods for a given customer
    public List<PaymentDetails> findPaymentMethodsByCustomer(Long customerId) {
        return paymentDetailsRepository.findPaymentDetailsByCustomerId(customerId);
    }

    // Delete payment method by ID
    public void deletePaymentMethodById(Long paymentMethodID) {
        if (paymentDetailsRepository.existsById(paymentMethodID)) {
            paymentDetailsRepository.deleteById(paymentMethodID);
        } else {
            throw new IllegalArgumentException("Payment method not found for ID: " + paymentMethodID);
        }
    }

    public List<DisplayPaymentDetailsDTO> getDisplayPaymentDetails(Long customerId) {
        List<PaymentDetails> encryptedPaymentDetailsList = paymentDetailsRepository.findPaymentDetailsByCustomerId(customerId);

        return encryptedPaymentDetailsList.stream().map(paymentDetails -> {
            // Decrypt and format card details
            String decryptedCardNumber = encryptionUtils.decrypt(paymentDetails.getEncryptedCardNumber());
            String maskedCardNumber = "**** **** **** " + decryptedCardNumber.substring(decryptedCardNumber.length() - 4);

            return new DisplayPaymentDetailsDTO(
                    paymentDetails.getId(),
                    maskedCardNumber,
                    encryptionUtils.decrypt(paymentDetails.getEncryptedNameOnCard()),
                    encryptionUtils.decrypt(paymentDetails.getEncryptedExpirationDate())
            );
        }).collect(Collectors.toList());
    }


    public List<PaymentDetails> findSavedPaymentMethodsForCustomer(Long customerId) {
        return paymentDetailsRepository.findPaymentDetailsByCustomerId(customerId);
    }

    public PaymentDetailsDTO findPaymentDetailsById(Long paymentMethodId) {
        return paymentDetailsRepository.findById(paymentMethodId)
                .map(this::convertToDTO)  // Convert entity to DTO if found
                .orElse(null);            // Return null if not found
    }

    // Helper method to convert PaymentDetails to PaymentDetailsDTO
    private PaymentDetailsDTO convertToDTO(PaymentDetails paymentDetails) {
        PaymentDetailsDTO dto = new PaymentDetailsDTO();
        dto.setId(paymentDetails.getId());

        // Decrypt sensitive data before setting in the DTO
        dto.setCardNumber("**** **** **** " + encryptionUtils.decrypt(paymentDetails.getEncryptedCardNumber()).substring(12));
        dto.setCardName(encryptionUtils.decrypt(paymentDetails.getEncryptedNameOnCard()));
        dto.setExpirationDate(encryptionUtils.decrypt(paymentDetails.getEncryptedExpirationDate()));
        dto.setZipCode(encryptionUtils.decrypt(paymentDetails.getEncryptedBillingZip()));
        dto.setSavePaymentDetails(paymentDetails.isSavePaymentDetails());

        return dto;
    }

}
