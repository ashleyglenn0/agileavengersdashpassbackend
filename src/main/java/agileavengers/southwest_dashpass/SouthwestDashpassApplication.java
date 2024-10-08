package agileavengers.southwest_dashpass;


import agileavengers.southwest_dashpass.tools.FlightDataPopulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;


@SpringBootApplication
@EnableTransactionManagement
public class SouthwestDashpassApplication implements CommandLineRunner {

	@Autowired
	private FlightDataPopulator flightDataPopulator;

	public static void main(String[] args) {

		SpringApplication.run(SouthwestDashpassApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		// Call the flight data generator here
		flightDataPopulator.populateFlightsIfEmpty(); // Generate 100 random flights
		System.out.println("Flights generated successfully.");
	}

}
