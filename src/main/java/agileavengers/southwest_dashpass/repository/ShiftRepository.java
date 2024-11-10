package agileavengers.southwest_dashpass.repository;

import agileavengers.southwest_dashpass.models.Employee;
import agileavengers.southwest_dashpass.models.Shift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ShiftRepository extends JpaRepository<Shift, Long> {
    List<Shift> findByEmployeeId(Long employeeId);

    @Query("SELECT s FROM Shift s WHERE s.startTime >= :startOfDay AND s.endTime <= :endOfDay")
    List<Shift> findShiftsForToday(@Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);

    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN TRUE ELSE FALSE END FROM Shift s WHERE s.employee = :employee AND DATE(s.date) = DATE(:date)")
    boolean existsByEmployeeAndDate(@Param("employee") Employee employee, @Param("date") LocalDateTime date);

    @Query("SELECT e FROM Employee e JOIN e.shifts s WHERE s.date BETWEEN :startDate AND :endDate")
    List<Employee> findEmployeesScheduledForDate(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);


}

