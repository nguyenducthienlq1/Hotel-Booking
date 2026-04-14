package hotelbooking.demo.domains.request;

import lombok.Data;
import java.util.List;

@Data
public class HotelSearchReqDTO {
    // Các tiêu chí tìm kiếm
    private String city;
    private String hotelName;

    private Long minPrice;
    private Long maxPrice;

    private List<Long> amenityIds;

    // Phân trang & Sắp xếp
    private int page = 1;
    private int size = 10;
    private String sort;

}