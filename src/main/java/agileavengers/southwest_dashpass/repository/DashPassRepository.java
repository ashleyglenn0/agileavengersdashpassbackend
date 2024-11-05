package agileavengers.southwest_dashpass.repository;

import agileavengers.southwest_dashpass.models.Customer;
import agileavengers.southwest_dashpass.models.DashPass;
import agileavengers.southwest_dashpass.models.DashPassReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DashPassRepository extends JpaRepository<DashPass, Long > {
    Optional<DashPass> findByDashpassId(Long dashpassId);

    @Query("SELECT d FROM DashPass d " +
            "LEFT JOIN FETCH d.dashPassReservation dr " +
            "LEFT JOIN FETCH d.customer c " +
            "LEFT JOIN FETCH c.user u " +
            "WHERE d.id = :dashPassId")
    DashPass findDashPassByIdWithCustomerUserAndReservation(@Param("dashPassId") Long dashPassId);

    @Query("SELECT d FROM DashPass d WHERE d.customer = :customer")
    List<DashPass> findByCustomer(@Param("customer") Customer customer);


}
