package backend.academy.scrapper.Repository;

import backend.academy.scrapper.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
