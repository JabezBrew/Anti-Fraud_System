package antifraud.repo;

import antifraud.entity.Role;
import antifraud.entity.User;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    List<User> findByOrderById();

    @Transactional
    void deleteByUsername(String username);

    Optional<User> findByRoles(Role role);
}
