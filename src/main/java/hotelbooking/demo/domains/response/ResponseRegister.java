package hotelbooking.demo.domains.response;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseRegister {
    private String message;
    private String token;
}
