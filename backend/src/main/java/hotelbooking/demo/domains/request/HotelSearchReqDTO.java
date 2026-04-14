package hotelbooking.demo.domains.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.List;

@Data
public class HotelSearchReqDTO {

    @Schema(description = "Filter by city", example = "Da Nang", nullable = true)
    private String city;

    @Schema(description = "Search by hotel name (partial match supported)", example = "Plaza", nullable = true)
    private String hotelName;

    @Schema(description = "Minimum price per night", example = "500000", nullable = true)
    private Long minPrice;

    @Schema(description = "Maximum price per night", example = "5000000", nullable = true)
    private Long maxPrice;

    @Schema(description = "Filter by specific amenities (must contain all selected)", example = "[1, 3]", nullable = true)
    private List<Long> amenityIds;

    @Schema(description = "Page number for pagination (starts from 1)", example = "1", defaultValue = "1")
    private int page = 1;

    @Schema(description = "Number of records per page", example = "10", defaultValue = "10")
    private int size = 10;

    @Schema(description = "Sorting criteria (format: property,direction). Examples: 'name,asc' or 'minPrice,desc'", example = "minPrice,asc", nullable = true)
    private String sort;
}