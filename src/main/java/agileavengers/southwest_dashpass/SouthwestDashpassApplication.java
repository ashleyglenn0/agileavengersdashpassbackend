package agileavengers.southwest_dashpass;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
public class SouthwestDashpassApplication {

	public static void main(String[] args) {
		SpringApplication.run(SouthwestDashpassApplication.class, args);
	}

}
