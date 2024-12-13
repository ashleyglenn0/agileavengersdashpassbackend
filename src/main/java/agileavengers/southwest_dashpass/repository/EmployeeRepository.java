package agileavengers.southwest_dashpass.repository;

import agileavengers.southwest_dashpass.models.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    @Query("SELECT e FROM Employee e JOIN e.shifts s WHERE s.startTime >= :startOfDay AND s.endTime <= :endOfDay")
    List<Employee> findEmployeesScheduledForDate(@Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);


}
