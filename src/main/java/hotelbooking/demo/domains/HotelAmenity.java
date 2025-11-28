package hotelbooking.demo.domains;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "hotel_amenities")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HotelAmenity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "hotel_id", nullable = false)
    private long hotelId;

    @Column(name = "amenity_id", nullable = false)
    private long amenityId;
}
