package hotelbooking.demo.domains;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "rooms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private String roomNumber;

    @Column(nullable = false)
    private int floor;

    @Column(nullable = false)
    private String status;

    private Instant createdAt;
    private Instant updatedAt;

    @Column(name = "hotel_id", nullable = false)
    private long hotelId;

    @Column(name = "room_type_id", nullable = false)
    private long roomTypeId;
}
