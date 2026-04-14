package hotelbooking.demo.domains.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RoomTypeReqDTO {

    @NotBlank(message = "Tên loại phòng không được để trống")
    private String name;

    private String description;

    @Min(value = 0, message = "Giá không được âm")
    private double basicPrice;

    @Min(value = 1, message = "Số người tối đa phải ít nhất là 1")
    private int maxGuests;

    @Min(value = 1, message = "Số giường phải ít nhất là 1")
    private int bedCount;

    @Min(value = 1, message = "Diện tích phải lớn hơn 0")
    private int sizeSquareM;

    private Boolean isActive; // Có thể null, service sẽ handle

    @NotNull(message = "Phải thuộc về một khách sạn (hotelId)")
    private Long hotelId;
}