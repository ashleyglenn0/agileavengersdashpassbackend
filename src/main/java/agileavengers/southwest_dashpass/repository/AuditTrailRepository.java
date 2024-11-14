package agileavengers.southwest_dashpass.repository;

import agileavengers.southwest_dashpass.models.AuditTrail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AuditTrailRepository extends JpaRepository<AuditTrail, Long> {
    @Query("SELECT a FROM AuditTrail a WHERE a.employeeId = :employeeId")
    List<AuditTrail> findAllActionsByEmployee(@Param("employeeId") Long employeeId);

    @Query("SELECT a FROM AuditTrail a WHERE a.actionType = 'DASHPASS_VALIDATION' AND a.timestamp BETWEEN :startDate AND :endDate")
    List<AuditTrail> findValidationsByDate(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);



}
