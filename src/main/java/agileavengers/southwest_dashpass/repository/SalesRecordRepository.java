package agileavengers.southwest_dashpass.repository;

import agileavengers.southwest_dashpass.models.SalesRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface SalesRecordRepository extends JpaRepository<SalesRecord, Long> {

    @Query("SELECT sr FROM SalesRecord sr WHERE sr.flight IS NOT NULL AND sr.dashPass IS NULL")
    List<SalesRecord> findFlightSalesOnly();

    @Query("SELECT sr FROM SalesRecord sr WHERE sr.dashPass IS NOT NULL AND sr.flight IS NULL")
    List<SalesRecord> findDashPassSalesOnly();

    @Query("SELECT sr FROM SalesRecord sr WHERE sr.dashPass IS NOT NULL AND sr.flight IS NOT NULL")
    List<SalesRecord> findSalesWithBothDashPassAndFlight();

    @Query("SELECT sr FROM SalesRecord sr WHERE sr.dashPass IS NOT NULL AND sr.flight IS NOT NULL AND sr.employee.id = :employeeId")
    List<SalesRecord> findSalesWithBothDashPassAndFlightByEmployee(@Param("employeeId") Long employeeId);

    @Query("SELECT sr FROM SalesRecord sr WHERE sr.flight IS NOT NULL AND sr.saleDate BETWEEN :startDate AND :endDate")
    List<SalesRecord> findFlightSalesWithinDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT sr FROM SalesRecord sr WHERE sr.customer.id = :customerId")
    List<SalesRecord> findSalesByCustomer(@Param("customerId") Long customerId);

    @Query("SELECT s FROM SalesRecord s WHERE s.employee.id = :employeeId AND s.saleDate BETWEEN :startDate AND :endDate")
    List<SalesRecord> findByEmployeeIdAndDateRange(@Param("employeeId") Long employeeId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT s FROM SalesRecord s WHERE s.customer.id = :customerId AND s.saleDate BETWEEN :startDate AND :endDate")
    List<SalesRecord> findByCustomerIdAndDateRange(@Param("customerId") Long customerId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT s FROM SalesRecord s WHERE s.saleDate BETWEEN :startDate AND :endDate")
    List<SalesRecord> findByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);


    @Query("SELECT sr.employee.id AS employeeId, COUNT(sr) AS totalSales " +
            "FROM SalesRecord sr " +
            "GROUP BY sr.employee.id " +
            "ORDER BY totalSales DESC")
    List<Object[]> findTopPerformers();

    // Retrieves all sales records by employee ID
    @Query("SELECT s FROM SalesRecord s WHERE s.employee.id = :employeeId")
    List<SalesRecord> findByEmployeeId(@Param("employeeId") Long employeeId);

    @Query("SELECT COUNT(sr) FROM SalesRecord sr")
    Integer calculateTotalSales();

    // Get total sales count for records with a non-null employee (employee sales)
    @Query("SELECT COUNT(sr) FROM SalesRecord sr WHERE sr.employee IS NOT NULL")
    Long countEmployeeSales();

    // Get total sales count for records with a null employee (customer sales)
    @Query("SELECT COUNT(sr) FROM SalesRecord sr WHERE sr.employee IS NULL")
    Long countCustomerSales();

    // Fetch top performing employee based on sales count
    @Query("SELECT sr.employee.id, COUNT(sr) AS salesCount FROM SalesRecord sr WHERE sr.employee IS NOT NULL GROUP BY sr.employee.id ORDER BY salesCount DESC")
    List<Object[]> findTopPerformingEmployees();

}
