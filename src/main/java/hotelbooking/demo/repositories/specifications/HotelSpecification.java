package hotelbooking.demo.repositories.specifications;

import hotelbooking.demo.domains.Hotel;
import hotelbooking.demo.domains.RoomType;
import hotelbooking.demo.domains.request.HotelSearchReqDTO;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class HotelSpecification {
    public static Specification<Hotel> getSpecification(HotelSearchReqDTO hotelSearchReqDTO) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (hotelSearchReqDTO.getHotelName() != null && !hotelSearchReqDTO.getHotelName().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                   criteriaBuilder.lower(root.get("name")),
                        "%" + hotelSearchReqDTO.getHotelName().toLowerCase() + "%"
                ));
            }
            if (hotelSearchReqDTO.getCity() != null && !hotelSearchReqDTO.getCity().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("city")),
                        "%" + hotelSearchReqDTO.getCity().toLowerCase() + "%"
                ));
            }
            if (hotelSearchReqDTO.getMinPrice() != null || hotelSearchReqDTO.getMaxPrice() != null) {
                // Join bảng Hotel với RoomType
                Join<Hotel, RoomType> roomTypeJoin = root.join("roomTypes", JoinType.INNER);

                if (hotelSearchReqDTO.getMinPrice() != null) {
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(roomTypeJoin.get("basicPrice"), hotelSearchReqDTO.getMinPrice()));
                }
                if (hotelSearchReqDTO.getMaxPrice() != null) {
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(roomTypeJoin.get("basicPrice"), hotelSearchReqDTO.getMaxPrice()));
                }
            }
            if (hotelSearchReqDTO.getAmenityIds() != null && !hotelSearchReqDTO.getAmenityIds().isEmpty()) {
                Join<Object, Object> amenityJoin = root.join("hotelAmenities").join("amenity");
                predicates.add(amenityJoin.get("id").in(hotelSearchReqDTO.getAmenityIds()));
            }
            query.distinct(true);
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
