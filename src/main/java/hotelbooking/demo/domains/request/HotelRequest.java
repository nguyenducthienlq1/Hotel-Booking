package hotelbooking.demo.domains.request;

import hotelbooking.demo.domains.response.MediaResDTO;
import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class HotelRequest {
    private String name;
    private String description;
    private String city;
    private String address;
    private String country;
    private String latitude;
    private String longitude;

    private List<Long> amenityId;
    private List<MediaReqDTO> media;
}
