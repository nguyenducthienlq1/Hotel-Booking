package hotelbooking.demo.domains.request;


import hotelbooking.demo.domains.enums.RoomStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Data
public class RoomRequest {

    @NotBlank(message = "Số phòng không được để trống")
    private String roomNumber;

    @Min(value = 0, message = "Tầng phải lớn hơn hoặc bằng 0")
    private Integer floor;

    @NotNull(message = "Trạng thái phòng không được để trống")
    private RoomStatus status;

    @NotNull(message = "Phải chọn khách sạn")
    private Long hotelId;

    private Long roomTypeId; // Có thể null nếu bạn chưa làm phần này
}
