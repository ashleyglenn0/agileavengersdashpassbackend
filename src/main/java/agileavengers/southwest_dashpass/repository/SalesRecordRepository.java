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
}
