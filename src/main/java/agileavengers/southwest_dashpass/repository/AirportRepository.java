package agileavengers.southwest_dashpass.repository;

import agileavengers.southwest_dashpass.models.Airport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AirportRepository extends JpaRepository<Airport, Long> {
    Airport findByAirportCode(String airportCode);
}
