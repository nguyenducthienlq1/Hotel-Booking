package hotelbooking.demo.domains.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AmenityReqDTO {

    @NotBlank(message = "Mã tiện ích (Code) không được để trống")
    private String code; // Ví dụ: WIFI, POOL

    @NotBlank(message = "Tên tiện ích không được để trống")
    private String name;

    private String description;
}
