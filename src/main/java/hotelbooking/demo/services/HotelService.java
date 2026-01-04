package hotelbooking.demo.services;

import hotelbooking.demo.domains.Hotel;
import hotelbooking.demo.domains.User;
import hotelbooking.demo.domains.request.HotelRequest;
import hotelbooking.demo.domains.response.HotelResponse;
import hotelbooking.demo.repositories.HotelRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class HotelService {
    private final HotelRepository hotelRepository;
    private final UserService userService;
    public HotelService(HotelRepository hotelRepository,
                        UserService userService) {
        this.hotelRepository = hotelRepository;
        this.userService = userService;
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
                .isActive(true)
                .build();
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
        return HotelResponse.builder()
                .id(hotel.getId())
                .name(hotel.getName())
                .city(hotel.getCity())
                .address(hotel.getAddress())
                .latitude(hotel.getLatitude())
                .longitude(hotel.getLongitude())
                .country(hotel.getCountry())
                .build();
    }
}
