package hotelbooking.demo.controllers;

import hotelbooking.demo.domains.Room;
import hotelbooking.demo.domains.request.RoomRequest;
import hotelbooking.demo.domains.response.RoomResponse;
import hotelbooking.demo.services.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${hotelbooking.api-prefix}/rooms")
@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_HOTEL_OWNER')")
@Tag(name = "Room Management", description = "Endpoints for managing rooms. Requires ADMIN or HOTEL_OWNER role.")
public class RoomController {
    private final RoomService roomService;
    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }
    @PostMapping
    @Operation(summary = "Create a new room", description = "Adds a new room to a specific hotel. Requires Admin or Hotel Owner privileges.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Room created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data (e.g., missing hotel ID)"),
            @ApiResponse(responseCode = "403", description = "Forbidden - User does not have the required role")
    })
    public ResponseEntity<RoomResponse> addRoom(@Valid @RequestBody RoomRequest room) {
        return ResponseEntity.ok(roomService.createRoom(room));
    }
    @PutMapping("/{roomId}")
    @Operation(summary = "Update an existing room", description = "Updates details of a specific room by its ID. Requires Admin or Hotel Owner privileges.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Room updated successfully"),
            @ApiResponse(responseCode = "404", description = "Room not found"),
            @ApiResponse(responseCode = "403", description = "Forbidden - User does not have the required role")
    })
    public ResponseEntity<RoomResponse> updateRoom(@RequestBody RoomRequest room,
                                                   @PathVariable Long roomId) {
        RoomResponse updatedRoom = roomService.updateRoom(roomId, room);
        return ResponseEntity.ok(updatedRoom);
    }
    @DeleteMapping("/{roomId}")
    @Operation(summary = "Delete a room", description = "Removes a room from the system. Requires Admin or Hotel Owner privileges.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Room deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Room not found"),
            @ApiResponse(responseCode = "403", description = "Forbidden - User does not have the required role")
    })
    public ResponseEntity<Void> deleteRoom(@PathVariable Long roomId) {
        roomService.deleteRoom(roomId);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/{id}")
    @Operation(summary = "Get room details", description = "Retrieves full information of a specific room by its ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Room details retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Room not found")
    })
    public ResponseEntity<RoomResponse> getRoomDetail(@PathVariable Long id) {
        return ResponseEntity.ok(roomService.getRoomDetail(id));
    }
}
