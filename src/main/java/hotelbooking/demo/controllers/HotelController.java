package hotelbooking.demo.controllers;


import hotelbooking.demo.domains.Hotel;
import hotelbooking.demo.domains.request.HotelRequest;
import hotelbooking.demo.domains.response.HotelResponse;
import hotelbooking.demo.domains.response.RoomResponse;
import hotelbooking.demo.domains.response.RoomTypeResDTO;
import hotelbooking.demo.services.HotelService;
import hotelbooking.demo.services.RoomService;
import hotelbooking.demo.services.RoomTypeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${hotelbooking.api-prefix}/hotel")
public class HotelController {
    private final HotelService hotelService;
    private final RoomService roomService;
    private final RoomTypeService roomTypeService;
    public HotelController(HotelService hotelService,
                           RoomService roomService,
                           RoomTypeService roomTypeService) {
        this.hotelService = hotelService;
        this.roomService = roomService;
        this.roomTypeService = roomTypeService;
    }

    @GetMapping
    public ResponseEntity<List<HotelResponse>> getAllHotels() {
        return ResponseEntity.ok(hotelService.getAllHotels());
    }
    @GetMapping("/{hotelId}")
    public ResponseEntity<HotelResponse> getHotelById(@PathVariable Long hotelId) {
        return ResponseEntity.ok(hotelService.getHotelById(hotelId));
    }
    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_HOTEL_OWNER')")
    public ResponseEntity<HotelResponse> createHotel(@RequestBody HotelRequest hotelRq) {
        return ResponseEntity.ok(hotelService.createHotel(hotelRq));
    }
    @PutMapping("/{hotelId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_HOTEL_OWNER')")
    public ResponseEntity<HotelResponse> updateHotel(@PathVariable Long hotelId,
                                                     @RequestBody HotelRequest hotelRq) {
        HotelResponse hotel = hotelService.getHotelById(hotelId);
        if (hotel == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(hotelService.updateHotel(hotelId, hotelRq));
    }
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_HOTEL_OWNER')")
    @DeleteMapping("/{hotelId}")
    public ResponseEntity<Void> deleteHotel(@PathVariable Long hotelId) {
        hotelService.deleteHotel(hotelId);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/{hotelId}/rooms")
    public ResponseEntity<List<RoomResponse>> getAllRooms(@PathVariable Long hotelId) {
        return ResponseEntity.ok(roomService.getAllRoomsByHotelId(hotelId));
    }
    @GetMapping("/{hotelId}/roomtypes")
    public ResponseEntity<List<RoomTypeResDTO>> getByHotel(@PathVariable Long hotelId) {
        return ResponseEntity.ok(roomTypeService.getAllRoomTypesByHotel(hotelId));
    }
}
