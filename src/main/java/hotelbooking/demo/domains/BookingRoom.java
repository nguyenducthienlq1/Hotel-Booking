package hotelbooking.demo.domains;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "booking_rooms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "price_per_night", nullable = false)
    private double pricePerNight;

    @Column(nullable = false)
    private int nights;

    @Column(name = "total_price", nullable = false)
    private double totalPrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private Room room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id")
    private Booking booking;
}
