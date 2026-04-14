package hotelbooking.demo.controllers;

import hotelbooking.demo.domains.request.RoomTypeReqDTO;
import hotelbooking.demo.domains.response.RoomTypeResDTO;
import hotelbooking.demo.services.RoomTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("${hotelbooking.api-prefix}/room-types")
@Tag(name = "Room Type Management", description = "Endpoints for managing room categories (e.g., Standard, Deluxe, Suite).")
public class RoomTypeController {

    private final RoomTypeService roomTypeService;

    public RoomTypeController(RoomTypeService roomTypeService) {
        this.roomTypeService = roomTypeService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_HOTEL_OWNER')")
    @Operation(summary = "Create a new room type", description = "Adds a new room category to a specific hotel. Requires Admin or Hotel Owner privileges.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Room type created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input payload"),
            @ApiResponse(responseCode = "403", description = "Forbidden - User lacks required roles")
    })
    public ResponseEntity<RoomTypeResDTO> createRoomType(
            @Parameter(description = "Room type details payload", required = true)
            @Valid @RequestBody RoomTypeReqDTO req) {
        return ResponseEntity.status(201).body(roomTypeService.createRoomType(req));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_HOTEL_OWNER')")
    @Operation(summary = "Update a room type", description = "Updates details of an existing room type. Requires Admin or Hotel Owner privileges.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Room type updated successfully"),
            @ApiResponse(responseCode = "404", description = "Room type not found"),
            @ApiResponse(responseCode = "403", description = "Forbidden - User lacks required roles")
    })
    public ResponseEntity<RoomTypeResDTO> updateRoomType(
            @Parameter(description = "ID of the room type to update", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "Updated room type payload", required = true)
            @Valid @RequestBody RoomTypeReqDTO req) {
        return ResponseEntity.ok(roomTypeService.updateRoomType(id, req));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_HOTEL_OWNER')")
    @Operation(summary = "Delete a room type", description = "Removes a room type from the system. Requires Admin or Hotel Owner privileges.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Room type deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Room type not found"),
            @ApiResponse(responseCode = "403", description = "Forbidden - User lacks required roles")
    })
    public ResponseEntity<Void> deleteRoomType(
            @Parameter(description = "ID of the room type to delete", required = true, example = "1")
            @PathVariable Long id) {
        roomTypeService.deleteRoomType(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get room type details", description = "Public endpoint to retrieve full details of a specific room type.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Room type details retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Room type not found")
    })
    public ResponseEntity<RoomTypeResDTO> getRoomTypeById(
            @Parameter(description = "ID of the room type to retrieve", required = true, example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(roomTypeService.getRoomTypeById(id));
    }
}
