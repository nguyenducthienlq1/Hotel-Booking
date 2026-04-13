package hotelbooking.demo.repositories;

import hotelbooking.demo.domains.Session;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SessionRepository extends JpaRepository<Session, Long> {
    Optional<Session> findByRefreshToken(String refreshToken);
    void deleteByRefreshToken(String refreshToken);
}