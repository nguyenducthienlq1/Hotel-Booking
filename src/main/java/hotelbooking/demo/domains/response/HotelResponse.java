package hotelbooking.demo.domains.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class HotelResponse {
    private Long id;
    private String name;
    private String description;
    private String city;
    private String address;
    private String country;
    private String latitude;
    private String longitude;
    private List<AmenityResDTO> amenities;
}
