package hotelbooking.demo.domains.response;

import hotelbooking.demo.domains.enums.MediaCategory;
import hotelbooking.demo.domains.enums.MediaType;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MediaResDTO {
    private Long id;
    private String url;
    private MediaType type;
    private MediaCategory category;
    private String caption;
}