package hotelbooking.demo.services;

import hotelbooking.demo.domains.Hotel;
import hotelbooking.demo.domains.RoomType;
import hotelbooking.demo.domains.request.RoomTypeReqDTO;
import hotelbooking.demo.domains.response.RoomTypeResDTO;
import hotelbooking.demo.repositories.HotelRepository;
import hotelbooking.demo.repositories.RoomTypeRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoomTypeService {
    private final RoomTypeRepository roomTypeRepository;
    private final HotelRepository hotelRepository;
    public RoomTypeService(RoomTypeRepository roomTypeRepository,
                           HotelRepository hotelRepository) {
        this.roomTypeRepository = roomTypeRepository;
        this.hotelRepository = hotelRepository;
    }
    @Transactional
    public RoomTypeResDTO createRoomType(RoomTypeReqDTO req) {
        Hotel hotel = hotelRepository.findById(req.getHotelId())
                .orElseThrow(() -> new RuntimeException("Hotel not found with id: " + req.getHotelId()));

        RoomType roomType = RoomType.builder()
                .name(req.getName())
                .description(req.getDescription())
                .basicPrice(req.getBasicPrice())
                .maxGuests(req.getMaxGuests())
                .bedCount(req.getBedCount())
                .sizeSquareM(req.getSizeSquareM())
                .isActive(req.getIsActive() != null ? req.getIsActive() : true) // Mặc định là true
                .hotel(hotel)
                .build();

        return mapToResDTO(roomTypeRepository.save(roomType));
    }
    @Transactional
    public RoomTypeResDTO updateRoomType(Long id, RoomTypeReqDTO req) {
        RoomType roomType = roomTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("RoomType not found with id: " + id));

        roomType.setName(req.getName());
        roomType.setDescription(req.getDescription());
        roomType.setBasicPrice(req.getBasicPrice());
        roomType.setMaxGuests(req.getMaxGuests());
        roomType.setBedCount(req.getBedCount());
        roomType.setSizeSquareM(req.getSizeSquareM());
        if (req.getIsActive() != null) {
            roomType.setIsActive(req.getIsActive());
        }
        return mapToResDTO(roomTypeRepository.save(roomType));
    }
    @Transactional
    public void deleteRoomType(Long id) {
        // Lưu ý: Nếu RoomType này đã có Room hoặc Booking, xóa có thể lỗi Foreign Key.
        // Nên dùng Soft Delete (is_active = false) hoặc check ràng buộc trước.
        RoomType roomType = roomTypeRepository.findById(id).orElseThrow(
                () -> new RuntimeException("RoomType not found with id: " + id));
        roomType.setIsActive(true);
        roomTypeRepository.save(roomType);
    }
    @Transactional
    public void undeleteRoomType(Long id) {
        RoomType roomType = roomTypeRepository.findById(id).orElseThrow(
                () -> new RuntimeException("RoomType not found with id: " + id));
        roomType.setIsActive(true);
        roomTypeRepository.save(roomType);
    }
    public RoomTypeResDTO getRoomTypeById(Long id) {
        RoomType roomType = roomTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("RoomType not found with id: " + id));
        return mapToResDTO(roomType);
    }
    public List<RoomTypeResDTO> getAllRoomTypesByHotel(Long hotelId) {
        return roomTypeRepository.findByHotelId(hotelId).stream()
                .map(this::mapToResDTO)
                .collect(Collectors.toList());
    }
    private RoomTypeResDTO mapToResDTO(RoomType entity) {
        RoomTypeResDTO.HotelInfo hotelInfo = null;
        if (entity.getHotel() != null) {
            hotelInfo = new RoomTypeResDTO.HotelInfo(
                    entity.getHotel().getId(),
                    entity.getHotel().getName()
            );
        }
        return RoomTypeResDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .basicPrice(entity.getBasicPrice())
                .maxGuests(entity.getMaxGuests())
                .bedCount(entity.getBedCount())
                .sizeSquareM(entity.getSizeSquareM())
                .isActive(entity.getIsActive())
                .hotel(hotelInfo)
                .build();
    }
}
