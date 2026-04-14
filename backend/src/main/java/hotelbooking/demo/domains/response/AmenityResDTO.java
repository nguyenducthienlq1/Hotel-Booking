package hotelbooking.demo.domains.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AmenityResDTO {
    private long id;
    private String code;
    private String name;
    private String description;
}