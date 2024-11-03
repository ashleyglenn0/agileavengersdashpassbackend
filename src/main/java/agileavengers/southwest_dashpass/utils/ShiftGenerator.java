package agileavengers.southwest_dashpass.utils;

import agileavengers.southwest_dashpass.models.Employee;
import agileavengers.southwest_dashpass.models.Shift;
import agileavengers.southwest_dashpass.repository.ShiftRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class ShiftGenerator {
    @Autowired
    private ShiftRepository shiftRepository;

    // Generate random shifts for a specific employee
    public List<Shift> generateRandomShiftsForEmployee(Employee employee, int count) {
        List<Shift> shifts = new ArrayList<>();
        LocalDate today = LocalDate.now();

        // Generate the specified number of shifts within the next 7 days with random times
        for (int i = 0; i < count; i++) {
            Shift shift = new Shift();
            shift.setEmployee(employee);
            shift.setDate(today.plusDays((long) (Math.random() * 7)));  // Shift within the next week
            shift.setStartTime(LocalTime.of(9 + (int) (Math.random() * 3), 0));  // Start time between 9-11 AM
            shift.setEndTime(shift.getStartTime().plusHours(8));  // 8-hour shift
            shifts.add(shift);
        }

        // Save generated shifts to the database and return
        shiftRepository.saveAll(shifts);
        return shifts;
    }
}
