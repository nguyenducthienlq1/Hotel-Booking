package hotelbooking.demo.repositories;

import hotelbooking.demo.domains.Amenity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AmenityRepository extends JpaRepository<Amenity, Long> {
    boolean existsByCode(String code);
}
