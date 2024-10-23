package agileavengers.southwest_dashpass.utils;

import agileavengers.southwest_dashpass.dtos.PaymentDetailsDTO;
import agileavengers.southwest_dashpass.models.PaymentStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.concurrent.CompletableFuture;

@Component
public class MockPaymentProcessor {

    private final Random random = new Random();

    // Simulate payment processing asynchronously with a random outcome
    @Async
    public CompletableFuture<PaymentStatus> processPaymentAsync(PaymentDetailsDTO paymentDetails, String userSelectedStatus) throws InterruptedException {
        // Simulate a delay (to mimic real-world payment processing delay)
        Thread.sleep(5000);

        // Randomly select a payment status
        int outcome = random.nextInt(3); // Generates 0, 1, or 2

        PaymentStatus paymentStatus;
        switch (outcome) {
            case 0:
                paymentStatus = PaymentStatus.PAID;
                break;
            case 1:
                paymentStatus = PaymentStatus.PENDING;
                break;
            default:
                paymentStatus = PaymentStatus.FAILED;
                break;
        }

        return CompletableFuture.completedFuture(paymentStatus);
    }



        // Randomize payment status method
    private PaymentStatus randomizePaymentStatus() {
        Random random = new Random();
        int result = random.nextInt(3); // 0 = PENDING, 1 = PAID, 2 = FAILED

        switch (result) {
            case 0:
                return PaymentStatus.PENDING;
            case 1:
                return PaymentStatus.PAID;
            case 2:
            default:
                return PaymentStatus.FAILED;
        }
    }

    // For simulating the pending status change after a delay
    public PaymentStatus simulatePendingStatus() throws InterruptedException {
        // Simulate processing time (e.g., 5 seconds)
        Thread.sleep(5000);

        // Randomly decide the final payment status after processing
        Random random = new Random();
        return random.nextBoolean() ? PaymentStatus.PAID : PaymentStatus.FAILED;
    }
}

