package agileavengers.southwest_dashpass.repository;

import agileavengers.southwest_dashpass.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional <User> findByUsername(String username);

    // This method will check if a user with the given username exists in the database
    boolean existsByUsername(String username);
}
