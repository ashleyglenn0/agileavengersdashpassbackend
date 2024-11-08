package agileavengers.southwest_dashpass.services;

import agileavengers.southwest_dashpass.models.AuditTrail;
import agileavengers.southwest_dashpass.models.Customer;
import agileavengers.southwest_dashpass.models.Employee;
import agileavengers.southwest_dashpass.repository.AuditTrailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuditTrailService {

    @Autowired
    private AuditTrailRepository auditTrailRepository;

    public void logAction(String actionType, Long entityId, Long employeeId, Long customerId) {
        AuditTrail auditTrail = new AuditTrail();
        auditTrail.setActionType(actionType);
        auditTrail.setEntityId(entityId);
        auditTrail.setEmployeeId(employeeId);
        auditTrail.setCustomerId(customerId);
        auditTrail.setTimestamp(LocalDateTime.now());
        auditTrailRepository.save(auditTrail);
    }

    public void logReservationValidation(Long employeeId, Long customerId, Long reservationId) {
        // Create and save the audit trail record
        AuditTrail auditTrail = new AuditTrail();
        auditTrail.setActionType("VALIDATE_RESERVATION");
        auditTrail.setDescription("Reservation validated for customer ID: " + customerId + ", reservation ID: " + reservationId);
        auditTrail.setTimestamp(LocalDateTime.now());
        auditTrail.setEmployeeId(employeeId);
        auditTrail.setCustomerId(customerId);
        auditTrail.setReservationId(reservationId);

        auditTrailRepository.save(auditTrail);
    }

    // Additional methods for filtering logs, querying by action type, etc.
}
