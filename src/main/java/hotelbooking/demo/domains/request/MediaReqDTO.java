package hotelbooking.demo.domains.request;

import hotelbooking.demo.domains.enums.MediaCategory;
import hotelbooking.demo.domains.enums.MediaType;
import lombok.Data;

@Data
public class MediaReqDTO {
    private String url;
    private MediaType type;
    private MediaCategory category;
    private String caption;
    private Long roomTypeId;
}