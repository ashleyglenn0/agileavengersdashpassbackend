package agileavengers.southwest_dashpass.services;

import agileavengers.southwest_dashpass.models.*;
import agileavengers.southwest_dashpass.repository.SalesRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SalesRecordService {

    @Autowired
    private SalesRecordRepository salesRecordRepository;
    @Autowired
    private EmployeeService employeeService;

    public void logCustomerFlightSale(Flight flight, Customer customer) {
        SalesRecord salesRecord = new SalesRecord(flight, customer, null, LocalDate.now());
        salesRecordRepository.save(salesRecord);
    }

    public void logFlightSaleByEmployee(Flight flight, Customer customer, Employee employee) {
        SalesRecord salesRecord = new SalesRecord(flight, customer, employee, LocalDate.now());
        salesRecordRepository.save(salesRecord);
    }

    public void logCustomerDashPassSale(DashPass dashPass, Customer customer) {
        SalesRecord salesRecord = new SalesRecord(dashPass, customer, null, LocalDate.now());
        salesRecordRepository.save(salesRecord);
    }

    public void logDashPassSaleByEmployee(DashPass dashPass, Customer customer, Employee employee) {
        SalesRecord salesRecord = new SalesRecord(dashPass, customer, employee, LocalDate.now());
        salesRecordRepository.save(salesRecord);
    }

    public void logCustomerRedeemDashPass(DashPass dashPass, Customer customer){
        SalesRecord salesRecord = new SalesRecord(dashPass, customer, LocalDate.now());
        salesRecordRepository.save(salesRecord);
    }

    public void logRedeemDashPassByEmployee(DashPass dashPass, Customer customer, Employee employee){
        SalesRecord salesRecord = new SalesRecord(dashPass, customer, employee, LocalDate.now());
        salesRecordRepository.save(salesRecord);
    }
    public List<SalesRecord> findSalesByType(String salesType) {
        return salesRecordRepository.findSalesByType(salesType);
    }

    public List<SalesRecord> findSalesByEmployee(Long employeeId) {
        return salesRecordRepository.findByEmployeeId(employeeId);
    }

    public List<SalesRecord> getAllFlightSales() {
        return salesRecordRepository.findSalesByType("flight");
    }

    public List<SalesRecord> getAllDashPassSales() {
        return salesRecordRepository.findSalesByType("dashpass");
    }

    public List<SalesRecord> getAllSales() {
        return salesRecordRepository.findAll();
    }

    public List<SalesRecord> getSalesByEmployee(Long employeeId) {
        return salesRecordRepository.findByEmployeeId(employeeId);
    }

    public List<SalesRecord> getSalesWithBothDashPassAndFlight() {
        return salesRecordRepository.findSalesWithBothDashPassAndFlight();
    }

    public List<SalesRecord> getSalesWithBothDashPassAndFlightByEmployee(Long employeeId) {
        return salesRecordRepository.findSalesWithBothDashPassAndFlightByEmployee(employeeId);
    }

    public List<SalesRecord> getFlightSalesWithinDateRange(LocalDate startDate, LocalDate endDate) {
        return salesRecordRepository.findFlightSalesWithinDateRange(startDate, endDate);
    }

    public List<SalesRecord> getSalesByCustomer(Long customerId) {
        return salesRecordRepository.findSalesByCustomer(customerId);
    }

    public List<SalesRecord> getByEmployeeIdAndDateRange(Long employeeId, LocalDate startDate, LocalDate endDate) {
        return salesRecordRepository.findByEmployeeIdAndDateRange(employeeId, startDate, endDate);
    }

    public List<SalesRecord> getByCustomerIdAndDateRange(Long customerId, LocalDate startDate, LocalDate endDate) {
        return salesRecordRepository.findByCustomerIdAndDateRange(customerId, startDate, endDate);
    }

    public List<SalesRecord> getSalesByDateRange(LocalDate startDate, LocalDate endDate) {
        return salesRecordRepository.findByDateRange(startDate, endDate);
    }


    // Retrieves all sales records for managers
    public List<SalesRecord> getAllSalesRecords() {
        return salesRecordRepository.findAll(); // Fetches all sales records
    }

    // Retrieves sales records for a specific employee
    public List<SalesRecord> getSalesByEmployeeId(Long employeeId) {
        return salesRecordRepository.findByEmployeeId(employeeId);
    }

    // Audit method to retrieve sales records based on specific filters
    public List<SalesRecord> auditSales(LocalDate startDate, LocalDate endDate, Long employeeId, Long customerId) {
        if (employeeId != null) {
            // Retrieve sales by a specific employee within a date range
            return salesRecordRepository.findByEmployeeIdAndDateRange(employeeId, startDate, endDate);
        } else if (customerId != null) {
            // Retrieve sales for a specific customer within a date range
            return salesRecordRepository.findByCustomerIdAndDateRange(customerId, startDate, endDate);
        } else {
            // Retrieve all sales within a date range
            return salesRecordRepository.findByDateRange(startDate, endDate);
        }
    }

    // Calculate team total sales (customer + employee sales)
    public Long calculateTeamTotalSales() {
        Long employeeSales = salesRecordRepository.countEmployeeSales();
        Long customerSales = salesRecordRepository.countCustomerSales();
        return (employeeSales != null ? employeeSales : 0) + (customerSales != null ? customerSales : 0);
    }

    // Get top performing employee with sales count
    public Map<String, Object> getTopPerformingEmployee() {
        List<Object[]> topPerformers = salesRecordRepository.findTopPerformingEmployees();
        if (topPerformers.isEmpty()) {
            return null;
        }

        Object[] topPerformerData = topPerformers.get(0);
        Long employeeId = (Long) topPerformerData[0];
        Long salesCount = (Long) topPerformerData[1];

        if (employeeId == null) {
            return null; // No valid employee ID found, return null or handle as needed
        }

        Employee employee = employeeService.findEmployeeById(employeeId); // Fetch employee details
        if (employee == null) {
            return null; // Employee not found, handle this case if necessary
        }

        Employee topEmployee = employeeService.findEmployeeById(employeeId);

        Map<String, Object> result = new HashMap<>();
        result.put("employee", topEmployee);
        result.put("totalSales", salesCount);
        return result;
    }

    // Get customer and employee sales counts separately
    public Map<String, Long> getSalesBreakdown() {
        Long employeeSalesCount = salesRecordRepository.countEmployeeSales();
        Long customerSalesCount = salesRecordRepository.countCustomerSales();

        Map<String, Long> salesBreakdown = new HashMap<>();
        salesBreakdown.put("employeeSales", employeeSalesCount != null ? employeeSalesCount : 0);
        salesBreakdown.put("customerSales", customerSalesCount != null ? customerSalesCount : 0);

        return salesBreakdown;
    }
}

