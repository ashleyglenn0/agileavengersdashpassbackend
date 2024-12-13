package agileavengers.southwest_dashpass.utils;

import agileavengers.southwest_dashpass.models.Employee;
import agileavengers.southwest_dashpass.models.Shift;
import agileavengers.southwest_dashpass.repository.ShiftRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class ShiftGenerator {
    @Autowired
    private ShiftRepository shiftRepository;

    // Generate random shifts for a specific employee
    public List<Shift> generateShifts(Employee employee, int count) {
        List<Shift> shifts = new ArrayList<>();
        LocalDateTime today = LocalDateTime.now();

        for (int i = 0; i < count; i++) {
            Shift shift = new Shift();
            shift.setEmployee(employee);

            // Generate a random shift date within the next week
            LocalDateTime shiftDate = today.plusDays((long) (Math.random() * 7));
            shift.setDate(shiftDate);

            // Generate a random start time between 9 AM and 11 AM
            int startHour = 9 + (int) (Math.random() * 3);
            LocalDateTime startTime = LocalDateTime.of(LocalDate.from(shiftDate), LocalTime.of(startHour, 0));
            shift.setStartTime(startTime);

            // Set an 8-hour shift duration
            LocalDateTime endTime = startTime.plusHours(8);
            shift.setEndTime(endTime);

            shifts.add(shift);
        }

        return shifts;
    }
}
