package hotelbooking.demo.domains.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginDTO {

    @NotBlank(message = "Khong duoc de trong username")
    @Email
    private String email;

    @NotBlank(message = "Khong duoc de trong password")
    @Size(min = 8, message = "Mat khau qua ngan, it nhat 8 ki tu tro len")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).*$",
            message = "password phai co chu so, chu thuong va chu hoa")
    private String password;
}
