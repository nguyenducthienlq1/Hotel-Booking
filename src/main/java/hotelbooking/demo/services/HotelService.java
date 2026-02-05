package hotelbooking.demo.services;

import hotelbooking.demo.domains.*;

import hotelbooking.demo.domains.request.HotelRequest;
import hotelbooking.demo.domains.request.MediaReqDTO;
import hotelbooking.demo.domains.response.AmenityResDTO;
import hotelbooking.demo.domains.response.HotelResponse;
import hotelbooking.demo.domains.response.MediaResDTO;
import hotelbooking.demo.repositories.AmenityRepository;
import hotelbooking.demo.repositories.HotelRepository;
import hotelbooking.demo.repositories.RoomTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class HotelService {
    private final HotelRepository hotelRepository;
    private final AmenityRepository amenityRepository;
    private final RoomTypeRepository roomTypeRepository;
    public HotelService(HotelRepository hotelRepository,
                        RoomTypeRepository roomTypeRepository,
                        AmenityRepository amenityRepository) {
        this.hotelRepository = hotelRepository;
        this.amenityRepository = amenityRepository;
        this.roomTypeRepository = roomTypeRepository;
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
    @Transactional
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
                .hotelAmenities(new ArrayList<>())
                .media(new ArrayList<>())
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
            hotel.setHotelAmenities(hotelAmenities);
        }
        processMediaList(hotel, req.getMedia());
        Hotel savedHotel = hotelRepository.save(hotel);
        return mapToHotelResDTO(savedHotel);
    }
    @Transactional
    public HotelResponse updateHotel(Long idHotel, HotelRequest req) {
        Hotel hotel = hotelRepository.findById(idHotel)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách sạn với ID: " + idHotel));

        hotel.setName(req.getName());
        hotel.setCity(req.getCity());
        hotel.setAddress(req.getAddress());
        hotel.setDescription(req.getDescription());
        hotel.setLongitude(req.getLongitude());
        hotel.setLatitude(req.getLatitude());
        if (req.getAmenityId() != null) {
            hotel.getHotelAmenities().clear(); // Xóa hết cái cũ đi

            if (!req.getAmenityId().isEmpty()) {
                List<Amenity> newAmenities = amenityRepository.findAllById(req.getAmenityId());
                for (Amenity amenity : newAmenities) {
                    HotelAmenity ha = new HotelAmenity();
                    ha.setHotel(hotel);
                    ha.setAmenity(amenity);
                    hotel.getHotelAmenities().add(ha);
                }
            }
        }
        if (req.getMedia() != null) {
            processMediaList(hotel, req.getMedia());
        }
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
        List<MediaResDTO> mediaResDTOS = new ArrayList<>();
        if (hotel.getMedia() != null) {
            mediaResDTOS = hotel.getMedia().stream()
                    .map(m -> MediaResDTO.builder()
                            .id(m.getId())
                            .url(m.getUrl())
                            .type(m.getType())     // IMAGE hoặc VIDEO
                            .category(m.getCategory()) // POOL, ROOM, LOBBY...
                            .caption(m.getCaption())
                            .build())
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
                .media(mediaResDTOS)
                .build();
    }
    private void processMediaList(Hotel hotel, List<MediaReqDTO> mediaReqs) {
        if (mediaReqs == null || mediaReqs.isEmpty()) return;

        // Nếu update thì clear cũ (hoặc làm logic thông minh hơn tùy bạn)
        if (hotel.getMedia() != null) {
            hotel.getMedia().clear();
        }

        for (MediaReqDTO req : mediaReqs) {
            HotelMedia mediaEntity = new HotelMedia();
            mediaEntity.setHotel(hotel);
            mediaEntity.setUrl(req.getUrl());
            mediaEntity.setType(req.getType());
            mediaEntity.setCategory(req.getCategory());
            mediaEntity.setCaption(req.getCaption());

            // LOGIC QUAN TRỌNG: Kiểm tra RoomType
            if (req.getRoomTypeId() != null) {
                RoomType roomType = roomTypeRepository.findById(req.getRoomTypeId())
                        .orElseThrow(() -> new RuntimeException("RoomType not found ID: " + req.getRoomTypeId()));
                mediaEntity.setRoomType(roomType);
            } else {
                mediaEntity.setRoomType(null);
            }
            hotel.getMedia().add(mediaEntity);
        }
    }
}
