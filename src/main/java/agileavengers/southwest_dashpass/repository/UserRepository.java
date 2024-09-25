package agileavengers.southwest_dashpass.repository;

import agileavengers.southwest_dashpass.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
