package agileavengers.southwest_dashpass.utils;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ConfirmationNumberGenerator {

    public static String generateConfirmationNumber() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
    }
}

