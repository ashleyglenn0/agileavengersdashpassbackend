package agileavengers.southwest_dashpass.repository;

import agileavengers.southwest_dashpass.models.Employee;
import agileavengers.southwest_dashpass.models.Shift;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShiftRepository extends JpaRepository<Shift, Long> {
    List<Shift> findByEmployeeId(Long employeeId);
}
