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
        Thread.sleep(5000);

        PaymentStatus paymentStatus;
        if (userSelectedStatus != null) {
            // Determine the payment status based on user selection
            switch (userSelectedStatus.toLowerCase()) {
                case "paid":
                    paymentStatus = PaymentStatus.PAID;
                    break;
                case "failed":
                    paymentStatus = PaymentStatus.FAILED;
                    break;
                case "pending":
                    paymentStatus = PaymentStatus.PENDING;
                    break;
                default:
                    paymentStatus = PaymentStatus.FAILED;
                    break;
            }
        } else {
            // Randomized outcome if no user selection is provided
            int outcome = random.nextInt(3); // Generates 0, 1, or 2
            paymentStatus = (outcome == 0) ? PaymentStatus.PAID :
                    (outcome == 1) ? PaymentStatus.PENDING :
                            PaymentStatus.FAILED;
        }

        return CompletableFuture.completedFuture(paymentStatus);
    }

}

