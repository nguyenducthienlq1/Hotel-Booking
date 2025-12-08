package hotelbooking.demo.domains;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "room_types")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "basic_price", nullable = false)
    private double basicPrice;

    @Column(name = "max_guests", nullable = false)
    private int maxGuests;

    @Column(name = "bed_count", nullable = false)
    private int bedCount;

    @Column(name = "size_square_m", nullable = false)
    private int sizeSquareM;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    private Instant createdAt;
    private Instant updatedAt;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id")
    private Hotel hotel;
}
