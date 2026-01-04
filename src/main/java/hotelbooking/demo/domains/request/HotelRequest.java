package hotelbooking.demo.domains.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HotelRequest {
    private String name;
    private String description;
    private String city;
    private String address;
    private String country;
    private String latitude;
    private String longitude;
}
