package hotelbooking.demo.services;

import hotelbooking.demo.domains.Amenity;
import hotelbooking.demo.domains.Hotel;
import hotelbooking.demo.domains.HotelAmenity;
import hotelbooking.demo.domains.User;
import hotelbooking.demo.domains.request.HotelRequest;
import hotelbooking.demo.domains.response.AmenityResDTO;
import hotelbooking.demo.domains.response.HotelResponse;
import hotelbooking.demo.repositories.AmenityRepository;
import hotelbooking.demo.repositories.HotelRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class HotelService {
    private final HotelRepository hotelRepository;
    private final UserService userService;
    private final AmenityRepository amenityRepository;
    public HotelService(HotelRepository hotelRepository,
                        UserService userService,
                        AmenityRepository amenityRepository) {
        this.hotelRepository = hotelRepository;
        this.userService = userService;
        this.amenityRepository = amenityRepository;
    }
    public List<HotelResponse> getAllHotels() {
        return hotelRepository.findAll().stream()
                .map(this::mapToHotelResDTO)
                .collect(Collectors.toList());
    }
    public HotelResponse getHotelById(Long id) {
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách sạn với ID: " + id));
        return mapToHotelResDTO(hotel);
    }

    public HotelResponse createHotel(HotelRequest req) {
        if (hotelRepository.existsByNameAndCity(req.getName(), req.getCity())) {
            throw new RuntimeException("Khách sạn này đã tồn tại tại " + req.getCity());
        }
        Hotel hotel = Hotel.builder()
                .name(req.getName())
                .city(req.getCity())
                .address(req.getAddress())
                .description(req.getDescription())
                .longitude(req.getLongitude())
                .latitude(req.getLatitude())
                .country(req.getCountry())
                .isActive(true)
                .build();

        if (req.getAmenityId() != null && !req.getAmenityId().isEmpty()) {
            List<Amenity> selectedAmenities = amenityRepository.findAllById(req.getAmenityId());
            if (selectedAmenities.size() < req.getAmenityId().size()) {
                throw new RuntimeException("Một số tiện ích không tồn tại");
            }

            // Tạo các đối tượng HotelAmenity (Bảng trung gian)
            List<HotelAmenity> hotelAmenities = selectedAmenities.stream()
                    .map(amenity -> {
                        HotelAmenity ha = new HotelAmenity();
                        ha.setHotel(hotel);    // Link với Hotel mới
                        ha.setAmenity(amenity); // Link với Amenity tìm được
                        return ha;
                    })
                    .collect(Collectors.toList());
        }
        Hotel savedHotel = hotelRepository.save(hotel);
        return mapToHotelResDTO(savedHotel);
    }
    public HotelResponse updateHotel(Long idHotel, HotelRequest req) {
        Hotel hotel = hotelRepository.findById(idHotel)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách sạn với ID: " + idHotel));

        hotel.setName(req.getName());
        hotel.setCity(req.getCity());
        hotel.setAddress(req.getAddress());
        hotel.setDescription(req.getDescription());
        hotel.setLongitude(req.getLongitude());
        hotel.setLatitude(req.getLatitude());

        Hotel updatedHotel = hotelRepository.save(hotel);
        return mapToHotelResDTO(updatedHotel);
    }
    public void deleteHotel(Long idHotel) {
        if (!hotelRepository.existsById(idHotel)) {
            throw new RuntimeException("Không tìm thấy khách sạn với ID: " + idHotel);
        }
        hotelRepository.deleteById(idHotel);
    }
    private HotelResponse mapToHotelResDTO(Hotel hotel) {
        List<AmenityResDTO> amenityResDTOS = new ArrayList<>();

        if (hotel.getHotelAmenities() != null) {
            amenityResDTOS = hotel.getHotelAmenities().stream()
                    .map(hotelAmenity -> {
                        Amenity amenity = hotelAmenity.getAmenity();

                        return AmenityResDTO.builder()
                                .id(amenity.getId())
                                .code(amenity.getCode())
                                .name(amenity.getName())
                                .description(amenity.getDescription())
                                .build();
                    })
                    .collect(Collectors.toList());
        }
        return HotelResponse.builder()
                .id(hotel.getId())
                .name(hotel.getName())
                .description(hotel.getDescription())
                .city(hotel.getCity())
                .address(hotel.getAddress())
                .latitude(hotel.getLatitude())
                .longitude(hotel.getLongitude())
                .country(hotel.getCountry())
                .amenities(amenityResDTOS)
                .build();
    }
}
