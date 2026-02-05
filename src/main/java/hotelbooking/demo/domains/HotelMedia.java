package hotelbooking.demo.domains;

import hotelbooking.demo.domains.enums.MediaCategory;
import hotelbooking.demo.domains.enums.MediaType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "hotel_media")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HotelMedia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false, length = 500)
    private String url;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private MediaType type;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private MediaCategory category;

    @Column(nullable = true)
    private String caption;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id", nullable = false)
    private Hotel hotel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_type_id", nullable = true)
    private RoomType roomType;
}
