package agileavengers.southwest_dashpass.services;

import agileavengers.southwest_dashpass.dtos.EmployeeDTO;
import agileavengers.southwest_dashpass.models.Employee;
import agileavengers.southwest_dashpass.models.Shift;
import agileavengers.southwest_dashpass.models.User;
import agileavengers.southwest_dashpass.repository.EmployeeRepository;
import agileavengers.southwest_dashpass.repository.ShiftRepository;
import agileavengers.southwest_dashpass.repository.TimeOffRequestRepository;
import agileavengers.southwest_dashpass.utils.ShiftGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ShiftService {

    @Autowired
    private ShiftRepository shiftRepository;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private TimeOffRequestRepository timeOffRequestRepository;
    @Autowired
    private ShiftGenerator shiftGenerator;

    public List<EmployeeDTO> getScheduledEmployeesForToday(LocalDate date) {
        // Define start and end of the day for the given date
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59);

        // Retrieve employees scheduled within the day
        List<Employee> employeesWithShifts = employeeRepository.findEmployeesScheduledForDate(startOfDay, endOfDay);

        return employeesWithShifts.stream()
                .filter(employee -> timeOffRequestRepository.findApprovedRequestsForDate(employee.getId(), date).isEmpty())
                .map(employee -> {
                    User user = employee.getUser();
                    if (user == null) {
                        return null; // Skip employees without associated User
                    }

                    // Find the shift scheduled for today, if available
                    Optional<Shift> shiftForToday = employee.getShifts().stream()
                            .filter(shift -> shift.getDate().toLocalDate().isEqual(date))
                            .findFirst();

                    if (shiftForToday.isPresent()) {
                        Shift shift = shiftForToday.get();
                        LocalDateTime shiftStart = shift.getStartTime();
                        LocalDateTime shiftEnd = shift.getEndTime();

                        return new EmployeeDTO(
                                employee.getId(),
                                user.getFirstName(),
                                user.getLastName(),
                                employee.getRole(),
                                shiftStart != null ? shiftStart.toLocalTime() : null,
                                shiftEnd != null ? shiftEnd.toLocalTime() : null
                        );
                    } else {
                        return null; // No shift found for today
                    }
                })
                .filter(Objects::nonNull) // Remove entries without shifts or users today
                .collect(Collectors.toList());
    }





//    public List<EmployeeDTO> getScheduledEmployeesForToday(LocalDate date) {
//        LocalDateTime startOfDay = date.atStartOfDay();
//        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
//
//        // Fetch employees with shifts for the specified date range
//        List<Employee> employeesWithShifts = employeeRepository.findEmployeesScheduledForDate(startOfDay, endOfDay);
//
//        return employeesWithShifts.stream()
//                .filter(employee -> timeOffRequestRepository.findApprovedRequestsForDate(employee.getId(), date).isEmpty())
//                .map(employee -> {
//                    // Find today's shift
//                    Optional<Shift> shiftForToday = employee.getShifts().stream()
//                            .filter(shift -> !shift.getStartTime().isBefore(startOfDay) && !shift.getEndTime().isAfter(endOfDay))
//                            .findFirst();
//
//                    // Map to EmployeeDTO if shift exists
//                    return shiftForToday.map(shift -> new EmployeeDTO(
//                            employee.getId(),
//                            employee.getRole(),
//                            shift.getStartTime(),
//                            shift.getEndTime()
//                    )).orElse(null); // Return null if no shift found
//                })
//                .filter(Objects::nonNull) // Remove nulls for employees without shifts today
//                .collect(Collectors.toList());
//    }

    public void generateAndSaveShiftsForAllEmployees() {
        List<Employee> employees = employeeRepository.findAll();

        for (Employee employee : employees) {
            // Check if the employee has a shift for today
            LocalDate today = LocalDate.now();
            boolean hasShiftToday = shiftRepository.existsByEmployeeAndDate(employee, today.atStartOfDay());

            // If no shift exists for today, generate one
            if (!hasShiftToday) {
                List<Shift> generatedShifts = shiftGenerator.generateShifts(employee, 1); // Generate 1 shift for today
                shiftRepository.saveAll(generatedShifts); // Save to the database
            }
        }
    }
}
