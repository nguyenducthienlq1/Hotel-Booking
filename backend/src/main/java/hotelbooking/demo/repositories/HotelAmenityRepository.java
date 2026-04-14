package hotelbooking.demo.repositories;

import hotelbooking.demo.domains.HotelAmenity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HotelAmenityRepository extends JpaRepository<HotelAmenity, Long> {
}
