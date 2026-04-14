package hotelbooking.demo.domains.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AmenityReqDTO {

    @NotBlank(message = "Mã tiện ích (Code) không được để trống")
    @Schema(description = "Unique short code representing the amenity", example = "WIFI")
    private String code; // Ví dụ: WIFI, POOL

    @NotBlank(message = "Tên tiện ích không được để trống")
    @Schema(description = "Display name of the amenity", example = "Free High-Speed Wi-Fi")
    private String name;

    @Schema(description = "Detailed description of the amenity", example = "Complimentary wireless internet access available in all rooms and public areas.")
    private String description;
}