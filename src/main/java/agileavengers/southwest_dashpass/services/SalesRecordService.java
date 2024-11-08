package agileavengers.southwest_dashpass.services;

import agileavengers.southwest_dashpass.models.*;
import agileavengers.southwest_dashpass.repository.SalesRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class SalesRecordService {

    @Autowired
    private SalesRecordRepository salesRecordRepository;

    public void logDashPassSale(DashPass dashPass, Customer customer, Employee employee) {
        SalesRecord salesRecord = new SalesRecord(dashPass, customer, employee, LocalDate.now());
        salesRecordRepository.save(salesRecord);
    }

    public void logFlightSale(Flight flight, Customer customer, Employee employee) {
        SalesRecord salesRecord = new SalesRecord(flight, customer, employee, LocalDate.now());
        salesRecordRepository.save(salesRecord);
    }

    public void logDashPassSaleWithFlight(DashPass dashPass, Flight flight, Customer customer, Employee employee) {
        SalesRecord salesRecord = new SalesRecord(dashPass, flight, customer, employee, LocalDate.now());
        salesRecordRepository.save(salesRecord);
    }

    // Additional methods for generating reports, querying by employee, etc.
}
