package agileavengers.southwest_dashpass.repository;

import agileavengers.southwest_dashpass.models.TimeOffRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface TimeOffRequestRepository extends JpaRepository<TimeOffRequest, Long> {
    // Fetch all time off requests
    List<TimeOffRequest> findAll();

    @Query("SELECT t FROM TimeOffRequest t WHERE t.employee.id = :employeeId AND t.status = 'APPROVED' AND " +
            "((t.startDate <= :date AND t.endDate >= :date) OR t.startDate = :date)")
    List<TimeOffRequest> findApprovedRequestsForDate(@Param("employeeId") Long employeeId, @Param("date") LocalDate date);
}
