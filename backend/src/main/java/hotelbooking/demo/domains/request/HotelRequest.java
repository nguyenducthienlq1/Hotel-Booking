package hotelbooking.demo.domains.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HotelRequest {

    @Schema(description = "Name of the hotel", example = "Grand Plaza Luxury Hotel")
    private String name;

    @Schema(description = "Detailed description of the hotel and its services", example = "A 5-star luxury hotel located in the heart of the city with a beautiful ocean view.")
    private String description;

    @Schema(description = "City where the hotel is located", example = "Da Nang")
    private String city;

    @Schema(description = "Full street address of the hotel", example = "123 Vo Nguyen Giap Street")
    private String address;

    @Schema(description = "Country where the hotel is located", example = "Vietnam")
    private String country;

    @Schema(description = "Latitude coordinate for map integration", example = "16.0544")
    private String latitude;

    @Schema(description = "Longitude coordinate for map integration", example = "108.2022")
    private String longitude;

    @Schema(description = "List of amenity IDs available at this hotel (e.g., WiFi, Pool, Gym)", example = "[1, 2, 5]")
    private List<Long> amenityId;

    @Schema(description = "List of media files (images/videos) associated with the hotel")
    private List<MediaReqDTO> media;
}