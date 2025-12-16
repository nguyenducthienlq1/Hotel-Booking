package hotelbooking.demo.domains.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TwoFactorSetupDTO {
    private String secret;
    private String qrCodeUrl;
}