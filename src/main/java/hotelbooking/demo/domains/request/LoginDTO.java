package hotelbooking.demo.domains.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginDTO {

    @NotBlank(message = "Khong duoc de trong UserName")
    private String email;

    @NotBlank(message = "Khong duoc de trong password")
    private String password;

    @NotBlank(message = "Khong duoc de trong fullname")
    private String fullname;

    @NotBlank(message = "Khong duoc de trong SDT")
    private String phone;
}