package agileavengers.southwest_dashpass.repository;

import agileavengers.southwest_dashpass.models.Customer;
import agileavengers.southwest_dashpass.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByUser(User user);

    @Query(value = "SELECT c.* " +
            "FROM customer c " +
            "JOIN users u ON c.user_id = u.id " +
            "WHERE LOWER(u.firstName) LIKE LOWER(:name) " +
            "OR LOWER(u.lastName) LIKE LOWER(:name)",
            nativeQuery = true)
    List<Customer> findCustomersByName(@Param("name") String name);

    @Query(value = "SELECT c.* " +
            "FROM customer c " +
            "JOIN users u ON c.user_id = u.id " +
            "WHERE LOWER(u.firstName) LIKE :firstName " +
            "OR LOWER(u.lastName) LIKE :lastName",
            nativeQuery = true)
    List<Customer> findCustomersByFirstNameOrLastName(@Param("firstName") String firstName,
                                                      @Param("lastName") String lastName);





}
