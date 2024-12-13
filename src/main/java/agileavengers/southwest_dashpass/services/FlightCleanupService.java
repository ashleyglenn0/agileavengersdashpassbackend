package agileavengers.southwest_dashpass.services;

import agileavengers.southwest_dashpass.models.Flight;
import agileavengers.southwest_dashpass.repository.FlightRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class FlightCleanupService {

    @Autowired
    private FlightRepository flightRepository;

    // Scheduled to run every day at midnight (adjust as needed)
    @Scheduled(cron = "0 0 0 * * ?")
    public void cleanUpExpiredFlights() {
        // Get today's date
        LocalDate today = LocalDate.now();

        // Find all flights where the departure date is before today
        List<Flight> expiredFlights = flightRepository.findFlightsByDepartureDateBefore(today);

        if (!expiredFlights.isEmpty()) {
            // Delete all expired flights
            flightRepository.deleteAll(expiredFlights);
            System.out.println("Expired flights cleaned up: " + expiredFlights.size());
        }
    }
}
