package agileavengers.southwest_dashpass.services;

import agileavengers.southwest_dashpass.models.Employee;
import agileavengers.southwest_dashpass.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;

public class EmployeeService {
    @Autowired
    private EmployeeRepository employeeRepository;

    public void saveEmployee(Employee employee) {
        employeeRepository.save(employee);
    }
}
