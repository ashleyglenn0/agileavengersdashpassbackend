package agileavengers.southwest_dashpass.repository;

import agileavengers.southwest_dashpass.models.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
}
