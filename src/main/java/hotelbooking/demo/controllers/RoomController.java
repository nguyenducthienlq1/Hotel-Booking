package hotelbooking.demo.controllers;

import hotelbooking.demo.domains.Room;
import hotelbooking.demo.domains.request.RoomRequest;
import hotelbooking.demo.domains.response.RoomResponse;
import hotelbooking.demo.services.RoomService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${hotelbooking.api-prefix}/rooms")
@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_HOTEL_OWNER')")
public class RoomController {
    private final RoomService roomService;
    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }
    @PostMapping
    public ResponseEntity<RoomResponse> addRoom(@Valid @RequestBody RoomRequest room) {
        return ResponseEntity.ok(roomService.createRoom(room));
    }
    @PutMapping("/{roomId}")
    public ResponseEntity<RoomResponse> updateRoom(@RequestBody RoomRequest room,
                                                   @PathVariable Long roomId) {
        RoomResponse updatedRoom = roomService.updateRoom(roomId, room);
        return ResponseEntity.ok(updatedRoom);
    }
    @DeleteMapping("/{roomId}")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long roomId) {
        roomService.deleteRoom(roomId);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/{id}")
    public ResponseEntity<RoomResponse> getRoomDetail(@PathVariable Long id) {
        return ResponseEntity.ok(roomService.getRoomDetail(id));
    }
}
