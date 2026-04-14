package hotelbooking.demo.domains.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RoomTypeReqDTO {

    @NotBlank(message = "Tên loại phòng không được để trống")
    @Schema(description = "Name of the room category/type", example = "Superior Double Room")
    private String name;

    @Schema(description = "Detailed description of the room type, including amenities and views", example = "A spacious room with a king-size bed and a stunning ocean view.")
    private String description;

    @Min(value = 0, message = "Giá không được âm")
    @Schema(description = "Base price per night in the local currency", example = "500000.0")
    private double basicPrice;

    @Min(value = 1, message = "Số người tối đa phải ít nhất là 1")
    @Schema(description = "Maximum number of adult guests allowed in this room type", example = "2")
    private int maxGuests;

    @Min(value = 1, message = "Số giường phải ít nhất là 1")
    @Schema(description = "Total number of beds available in the room", example = "1")
    private int bedCount;

    @Min(value = 1, message = "Diện tích phải lớn hơn 0")
    @Schema(description = "Total area of the room in square meters", example = "35")
    private int sizeSquareM;

    @Schema(description = "Flag to determine if this room type is actively available for booking", example = "true", nullable = true)
    private Boolean isActive; // Có thể null, service sẽ handle


    @NotNull(message = "Phải thuộc về một khách sạn (hotelId)")
    @Schema(description = "Unique identifier of the hotel offering this room type", example = "1")
    private Long hotelId;
}