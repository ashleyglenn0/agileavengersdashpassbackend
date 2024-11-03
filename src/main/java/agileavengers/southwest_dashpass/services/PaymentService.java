package agileavengers.southwest_dashpass.services;

import agileavengers.southwest_dashpass.dtos.PaymentDetailsDTO;
import agileavengers.southwest_dashpass.models.PaymentDetails;
import agileavengers.southwest_dashpass.models.PaymentStatus;
import agileavengers.southwest_dashpass.utils.ConfirmationNumberGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class PaymentService {

    // Inject any necessary dependencies, like the confirmation number generator utility
    @Autowired
    private ConfirmationNumberGenerator confirmationNumberGenerator;

    @Async
    public CompletableFuture<PaymentStatus> processPaymentAsync(PaymentDetailsDTO paymentDetails, String userSelectedStatus) {
        // Simulate a delay for processing the payment
        try {
            Thread.sleep(2000); // Simulate payment processing delay
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        PaymentStatus paymentStatus;

        // Generate a random payment status or use the userSelectedStatus (PAID, PENDING, FAILED, etc.)
        if ("RANDOMIZE".equalsIgnoreCase(userSelectedStatus)) {
            paymentStatus = generateRandomPaymentStatus();
        } else {
            paymentStatus = PaymentStatus.valueOf(userSelectedStatus);
        }

        // Note: Confirmation number generation is now handled in the relevant service (e.g., ReservationService or DashPassReservationService).
        if (paymentStatus == PaymentStatus.PAID) {
            System.out.println("Payment confirmed.");
        }

        return CompletableFuture.completedFuture(paymentStatus);
    }


    private PaymentStatus generateRandomPaymentStatus() {
        int randomIndex = (int) (Math.random() * PaymentStatus.values().length);
        return PaymentStatus.values()[randomIndex];
    }

}
