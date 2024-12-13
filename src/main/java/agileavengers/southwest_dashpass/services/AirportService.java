package agileavengers.southwest_dashpass.services;

import agileavengers.southwest_dashpass.models.Airport;
import agileavengers.southwest_dashpass.repository.AirportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AirportService {
    private final AirportRepository airportRepository;

    @Autowired
    public AirportService(AirportRepository airportRepository){
        this.airportRepository = airportRepository;
    }

    public List<Airport> getAllAirports() {
        // Fetch all airports from the repository
        return airportRepository.findAll();
    }
}
