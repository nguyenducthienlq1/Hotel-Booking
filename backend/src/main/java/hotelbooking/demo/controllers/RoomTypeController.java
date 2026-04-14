package hotelbooking.demo.controllers;

import hotelbooking.demo.domains.request.RoomTypeReqDTO;
import hotelbooking.demo.domains.response.RoomTypeResDTO;
import hotelbooking.demo.services.RoomTypeService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("${hotelbooking.api-prefix}/room-types")
public class RoomTypeController {
    private final RoomTypeService roomTypeService;
    public RoomTypeController(RoomTypeService roomTypeService) {
        this.roomTypeService = roomTypeService;
    }
    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_HOTEL_OWNER')")
    public ResponseEntity<RoomTypeResDTO> createRoomType(@Valid @RequestBody RoomTypeReqDTO req) {
        return ResponseEntity.status(201).body(roomTypeService.createRoomType(req));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_HOTEL_OWNER')")
    public ResponseEntity<RoomTypeResDTO> updateRoomType(@PathVariable Long id,
                                                         @Valid @RequestBody RoomTypeReqDTO req) {
        return ResponseEntity.ok(roomTypeService.updateRoomType(id, req));
    }
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_HOTEL_OWNER')")
    public ResponseEntity<Void> deleteRoomType(@PathVariable Long id) {
        roomTypeService.deleteRoomType(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoomTypeResDTO> getRoomTypeById(@PathVariable Long id) {
        return ResponseEntity.ok(roomTypeService.getRoomTypeById(id));
    }

}
