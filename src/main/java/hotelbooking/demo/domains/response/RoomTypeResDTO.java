package hotelbooking.demo.domains.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomTypeResDTO {
    private long id;
    private String name;
    private String description;
    private double basicPrice;
    private int maxGuests;
    private int bedCount;
    private int sizeSquareM;
    private Boolean isActive;

    // Thông tin khách sạn gọn nhẹ
    private HotelInfo hotel;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class HotelInfo {
        private long id;
        private String name;
    }
}