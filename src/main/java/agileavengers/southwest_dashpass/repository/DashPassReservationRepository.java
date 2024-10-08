package agileavengers.southwest_dashpass.repository;

import agileavengers.southwest_dashpass.models.DashPassReservation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DashPassReservationRepository extends JpaRepository<DashPassReservation, Long> {
}
