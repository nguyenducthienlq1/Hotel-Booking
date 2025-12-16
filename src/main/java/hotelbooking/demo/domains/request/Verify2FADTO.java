package hotelbooking.demo.domains.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class Verify2FADTO {

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    private String email;

    @NotBlank(message = "Mã OTP không được để trống")
    @Pattern(regexp = "\\d{6}", message = "Mã OTP phải bao gồm 6 chữ số")
    private int code;
}