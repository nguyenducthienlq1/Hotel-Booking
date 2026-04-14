package hotelbooking.demo.domains;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bookings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private String status;

    private LocalDate checkinDate;
    private LocalDate checkoutDate;

    @Column(name = "total_price", nullable = false)
    private double totalPrice;

    private String currency;

    @Column(name = "number_of_guests")
    private int numberOfGuests;

    @Column(name = "special_requests", columnDefinition = "TEXT")
    private String specialRequests;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    private Hotel hotel;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BookingRoom> bookingRooms = new ArrayList<>();

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment")
    private Payment payment;

}
