package agileavengers.southwest_dashpass;

import agileavengers.southwest_dashpass.services.FlightService;
import agileavengers.southwest_dashpass.tools.FlightDataPopulator;
import agileavengers.southwest_dashpass.utils.EncryptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;



@SpringBootApplication
@EnableTransactionManagement
@EnableScheduling
public class SouthwestDashpassApplication implements CommandLineRunner {

	@Autowired
    private FlightDataPopulator flightDataPopulator;
	@Autowired
	private FlightService flightService;

	public static void main(String[] args) {

		SpringApplication.run(SouthwestDashpassApplication.class, args);
		try {
			String originalData = "SensitiveData123";

			// Encrypt the data
			String encryptedData = EncryptionUtils.encrypt(originalData);
			System.out.println("Encrypted Data: " + encryptedData);

			// Decrypt the data
			String decryptedData = EncryptionUtils.decrypt(encryptedData);
			System.out.println("Decrypted Data: " + decryptedData);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public void run(String... args) throws Exception {
		// Call the flight data generator here
		flightDataPopulator.populateFlightsIfEmpty(); // Generate 100 random flights
		System.out.println("Flights generated successfully.");
	}

	public class YourMainClass {
		public static void main(String[] args) {
			String secretKey = System.getenv("SECRET_KEY");
			System.out.println("Fetched Secret Key: " + secretKey); // Debug print to check the value
		}
	}


}
