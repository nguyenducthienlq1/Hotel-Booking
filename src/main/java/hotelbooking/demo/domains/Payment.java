package hotelbooking.demo.domains;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private double amount;
    private String currency;
    private String status;
    private String provider;

    @Column(name = "transaction_id", length = 255)
    private String transactionId;

    private Instant paidAt;
    private Instant createdAt;
    private Instant updatedAt;

    @Column(name = "booking_id", nullable = false)
    private long bookingId;
}
