package hotelbooking.demo.services;

import hotelbooking.demo.domains.Hotel;
import hotelbooking.demo.domains.Room;
import hotelbooking.demo.domains.RoomType;
import hotelbooking.demo.domains.request.RoomRequest;
import hotelbooking.demo.domains.response.RoomResponse;
import hotelbooking.demo.repositories.HotelRepository;
import hotelbooking.demo.repositories.RoomRepository;
import hotelbooking.demo.repositories.RoomTypeRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RoomService {
    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;
    private final RoomTypeRepository roomTypeRepository;
    public RoomService(RoomRepository roomRepository,
                       HotelRepository hotelRepository,
                       RoomTypeRepository roomTypeRepository) {
        this.roomRepository = roomRepository;
        this.hotelRepository = hotelRepository;
        this.roomTypeRepository = roomTypeRepository;
    }
    public RoomResponse createRoom(RoomRequest room) {
        Hotel hotel = hotelRepository.findById(room.getHotelId())
                .orElseThrow(() -> new RuntimeException("Hotel not found"));
        Room newRoom = new Room();
        newRoom.setRoomNumber(room.getRoomNumber());
        newRoom.setFloor(room.getFloor());
        newRoom.setStatus(room.getStatus());
        newRoom.setHotel(hotel);

        Room savedRoom = roomRepository.save(newRoom);
        return RoomResponse.builder()
                .id(savedRoom.getId())
                .roomNumber(savedRoom.getRoomNumber())
                .floor(savedRoom.getFloor())
                .status(savedRoom.getStatus())
                .createdAt(savedRoom.getCreatedAt())
                .createdBy(savedRoom.getCreatedBy())
                .hotel(new RoomResponse.HotelInfo(hotel.getId(), hotel.getName()))
                .build();
    }
    public RoomResponse getRoomDetail(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + id));

        return mapToResDTO(room);
    }
    public RoomResponse updateRoom(Long id, RoomRequest req ) {
        Room oldRoom = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + id));
        if (req.getRoomNumber() != null && !req.getRoomNumber().isEmpty()) {
            oldRoom.setRoomNumber(req.getRoomNumber());
        }

        if (req.getFloor() != null) { // Nhờ đổi sang Integer nên check null được
            oldRoom.setFloor(req.getFloor());
        }

        if (req.getStatus() != null) {
            oldRoom.setStatus(req.getStatus());
        }

        if (req.getHotelId() != null && req.getHotelId() != oldRoom.getHotel().getId()) {
            Hotel newHotel = hotelRepository.findById(req.getHotelId())
                    .orElseThrow(() -> new RuntimeException("Hotel not found"));
            oldRoom.setHotel(newHotel);
        }

        if (req.getRoomTypeId() != null) {
            // Nếu roomType hiện tại null HOẶC khác ID mới thì mới query DB tìm cái mới
            if (oldRoom.getRoomType() == null || oldRoom.getRoomType().getId() != req.getRoomTypeId()) {
                RoomType newType = roomTypeRepository.findById(req.getRoomTypeId())
                        .orElseThrow(() -> new RuntimeException("RoomType not found"));
                oldRoom.setRoomType(newType);
            }
        }
        Room updatedRoom = roomRepository.save(oldRoom);
        return mapToResDTO(updatedRoom);
    }
    public void deleteRoom(long id) {
        if (!roomRepository.existsById(id)) {
            throw new RuntimeException("Cannot delete. Room not found with id: " + id);
        }
        roomRepository.deleteById(id);
    }

    public List<RoomResponse> getAllRoomsByHotelId(Long hotelId) {
        // 1. Lấy danh sách Entity từ DB
        List<Room> rooms = roomRepository.findByHotelId(hotelId);

        // 2. Duyệt qua list và map sang DTO (Dùng lại hàm mapToResDTO mình đã viết cho bạn)
        return rooms.stream()
                .map(room -> this.mapToResDTO(room))
                .collect(Collectors.toList());
    }

    private RoomResponse mapToResDTO(Room room) {
        // 1. Map Hotel sang Info (xử lý null cho an toàn)
        RoomResponse.HotelInfo hotelInfo = null;
        if (room.getHotel() != null) {
            hotelInfo = new RoomResponse.HotelInfo(room.getHotel().getId(), room.getHotel().getName());
        }

        // 2. Map RoomType sang Info (xử lý null vì có thể phòng chưa gán loại)
        RoomResponse.RoomTypeInfo roomTypeInfo = null;
        if (room.getRoomType() != null) {
            roomTypeInfo = new RoomResponse.RoomTypeInfo(room.getRoomType().getId(),
                    room.getRoomType().getName(),
                    room.getRoomType().getDescription(),
                    room.getRoomType().getBasicPrice(),
                    room.getRoomType().getMaxGuests(),
                    room.getRoomType().getBedCount(),
                    room.getRoomType().getSizeSquareM());
        }

        // 3. Build Response hoàn chỉnh
        return RoomResponse.builder()
                .id(room.getId())
                .roomNumber(room.getRoomNumber())
                .floor(room.getFloor())
                .status(room.getStatus())
                // Map thông tin quan hệ
                .hotel(hotelInfo)
                .roomType(roomTypeInfo)
                // Map thông tin Audit từ BaseEntity
                .createdAt(room.getCreatedAt())
                .createdBy(room.getCreatedBy())
                .updatedAt(room.getUpdatedAt())
                .updatedBy(room.getUpdatedBy())
                .build();
    }
}
