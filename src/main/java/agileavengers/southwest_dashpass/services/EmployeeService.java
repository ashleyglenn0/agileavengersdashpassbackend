package agileavengers.southwest_dashpass.services;

import agileavengers.southwest_dashpass.models.Employee;
import agileavengers.southwest_dashpass.repository.EmployeeRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EmployeeService {
    @Autowired
    private EmployeeRepository employeeRepository;

    @Transactional
    public void registerEmployee(Employee employee) {
        employeeRepository.save(employee);
    }

}
