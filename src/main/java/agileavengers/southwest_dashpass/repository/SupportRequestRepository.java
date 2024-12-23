package agileavengers.southwest_dashpass.repository;

import agileavengers.southwest_dashpass.models.Customer;
import agileavengers.southwest_dashpass.models.SupportRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.awt.print.Pageable;
import java.util.List;
import java.util.Optional;

public interface SupportRequestRepository extends JpaRepository<SupportRequest, Long> {
    // Fetch the most recent support requests in descending order of creation date
    @Query(value = "SELECT * FROM SupportRequest ORDER BY createdDate DESC LIMIT :limit", nativeQuery = true)
    List<SupportRequest> findRecentSupportRequests(@Param("limit") int limit);

    List<SupportRequest> findByCustomerOrderByCreatedDateDesc(Customer customer);

    Optional<SupportRequest> findById(Long id);

    // Repository method to retrieve only open or escalated support requests
    List<SupportRequest> findByStatusNot(SupportRequest.Status status);

    List<SupportRequest> findByStatus(SupportRequest.Status status);

}
