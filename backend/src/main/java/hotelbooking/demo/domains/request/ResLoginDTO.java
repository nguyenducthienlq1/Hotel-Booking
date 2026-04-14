package hotelbooking.demo.domains.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
public class ResLoginDTO {

    private UserLogin userLogin;

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("mfa_required")
    private boolean mfaRequired = false; // Mặc định là false

    private String message; // Để thông báo: "Vui lòng nhập mã OTP"

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserLogin{
        private long id;
        private String email;
        private String name;
        private String imageUrl;

    }
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserGetAccount{
        private UserLogin user;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserInsideToken{
        private long id;
        private String email;
        private String name;
    }
}