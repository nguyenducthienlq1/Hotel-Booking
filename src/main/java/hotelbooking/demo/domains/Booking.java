package hotelbooking.demo.domains;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bookings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private String status;

    private Instant checkinDate;
    private Instant checkoutDate;

    @Column(name = "total_price", nullable = false)
    private double totalPrice;

    private String currency;

    @Column(name = "number_of_guests")
    private int numberOfGuests;

    @Column(name = "special_requests", columnDefinition = "TEXT")
    private String specialRequests;

    private Instant createdAt;
    private Instant updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    private Hotel hotel;

    @OneToMany(mappedBy = "booking_id", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BookingRoom> bookingRoom = new ArrayList<>();

    @OneToOne(fetch = FetchType.LAZY)
    private Payment payment;

}
