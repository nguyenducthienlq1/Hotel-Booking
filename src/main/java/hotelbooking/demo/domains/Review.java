package hotelbooking.demo.domains;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "reviews")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private int rating;

    @Column(columnDefinition = "TEXT")
    private String comment;

    private Instant createdAt;

    @Column(name = "is_visible", nullable = false)
    private boolean isVisible;

    // relationship fields must be at the bottom
    @Column(name = "booking_id")
    private Long bookingId;

    @Column(name = "user_id", nullable = false)
    private long userId;

    @Column(name = "hotel_id", nullable = false)
    private long hotelId;
}
