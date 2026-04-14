package hotelbooking.demo.domains.response;

import hotelbooking.demo.domains.enums.RoomStatus;
import lombok.*;

import java.time.Instant;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Data
@Setter
@Builder
public class RoomResponse {
    private long id;
    private String roomNumber;
    private int floor;
    private RoomStatus status;

    private HotelInfo hotel;

    private RoomTypeInfo roomType;

    // Thông tin audit từ BaseEntity
    private Instant createdAt;
    private String createdBy;
    private Instant updatedAt;
    private String updatedBy;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class HotelInfo {
        private long id;
        private String name;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RoomTypeInfo {
        private long id;
        private String name;
        private String description;
        private double basicPrice;
        private int maxGuests;
        private int bedCount;
        private int sizeSquareM;
    }
}
