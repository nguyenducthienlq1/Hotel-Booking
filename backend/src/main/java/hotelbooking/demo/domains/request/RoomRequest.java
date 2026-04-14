package hotelbooking.demo.domains.request;


import hotelbooking.demo.domains.enums.RoomStatus;
import io.swagger.v3.oas.annotations.media.Schema;
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
    @Schema(description = "Room number or identifier assigned to the room", example = "VIP-101")
    private String roomNumber;

    @Min(value = 0, message = "Tầng phải lớn hơn hoặc bằng 0")
    @Schema(description = "Floor number where the room is located", example = "1")
    private Integer floor;

    @NotNull(message = "Trạng thái phòng không được để trống")
    @Schema(description = "Current operational status of the room", example = "AVAILABLE")
    private RoomStatus status;

    @NotNull(message = "Phải chọn khách sạn")
    @Schema(description = "Unique identifier of the hotel this room belongs to", example = "1")
    private Long hotelId;

    @Schema(description = "Unique identifier of the room type. Can be null if not assigned yet.", example = "2", nullable = true)
    private Long roomTypeId;
}
