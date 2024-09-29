package agileavengers.southwest_dashpass.services;

import agileavengers.southwest_dashpass.models.Employee;
import agileavengers.southwest_dashpass.models.User;
import agileavengers.southwest_dashpass.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EmployeeService {
    @Autowired
    private EmployeeRepository employeeRepository;

    public void registerEmployee(Long id, String firstName, String lastName, String username, String password, String email, String role) {
        Employee employee = new Employee(id, firstName, lastName, username, password, email, role);
        employee.setEmployeeId(id);
        employee.setFirstName(firstName);
        employee.setLastName(lastName);
        employee.setUsername(username);
        employee.setPassword(password);
        employee.setEmail(email);
        employee.setRole(role);

        employeeRepository.save(employee);
    }

}
