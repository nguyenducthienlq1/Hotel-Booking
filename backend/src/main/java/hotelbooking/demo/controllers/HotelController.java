package hotelbooking.demo.controllers;


import hotelbooking.demo.domains.Hotel;
import hotelbooking.demo.domains.request.HotelRequest;
import hotelbooking.demo.domains.request.HotelSearchReqDTO;
import hotelbooking.demo.domains.response.HotelResponse;
import hotelbooking.demo.domains.response.RoomResponse;
import hotelbooking.demo.domains.response.RoomTypeResDTO;
import hotelbooking.demo.services.HotelService;
import hotelbooking.demo.services.RoomService;
import hotelbooking.demo.services.RoomTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${hotelbooking.api-prefix}/hotel")
@Tag(name = "Hotel Management", description = "Endpoints for managing hotels, searching, and retrieving hotel-related resources (rooms, room types).")
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
    @Operation(summary = "Get all hotels", description = "Public endpoint to retrieve a list of all available hotels.")
    @ApiResponse(responseCode = "200", description = "List of hotels retrieved successfully")
    public ResponseEntity<List<HotelResponse>> getAllHotels() {
        return ResponseEntity.ok(hotelService.getAllHotels());
    }

    @GetMapping("/{hotelId}")
    @Operation(summary = "Get hotel details", description = "Public endpoint to retrieve full information of a specific hotel by its ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Hotel details retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Hotel not found")
    })
    public ResponseEntity<HotelResponse> getHotelById(
            @Parameter(description = "ID of the hotel", required = true, example = "1")
            @PathVariable Long hotelId) {
        return ResponseEntity.ok(hotelService.getHotelById(hotelId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_HOTEL_OWNER')")
    @Operation(summary = "Create a new hotel", description = "Adds a new hotel. Requires Admin or Hotel Owner privileges.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Hotel created successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - User lacks required roles")
    })
    public ResponseEntity<HotelResponse> createHotel(@RequestBody HotelRequest hotelRq) {
        return ResponseEntity.ok(hotelService.createHotel(hotelRq));
    }

    @PutMapping("/{hotelId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_HOTEL_OWNER')")
    @Operation(summary = "Update a hotel", description = "Updates details of an existing hotel. Requires Admin or Hotel Owner privileges.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Hotel updated successfully"),
            @ApiResponse(responseCode = "404", description = "Hotel not found"),
            @ApiResponse(responseCode = "403", description = "Forbidden - User lacks required roles")
    })
    public ResponseEntity<HotelResponse> updateHotel(
            @Parameter(description = "ID of the hotel to update", required = true, example = "1")
            @PathVariable Long hotelId,
            @RequestBody HotelRequest hotelRq) {
        HotelResponse hotel = hotelService.getHotelById(hotelId);
        if (hotel == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(hotelService.updateHotel(hotelId, hotelRq));
    }

    @DeleteMapping("/{hotelId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_HOTEL_OWNER')")
    @Operation(summary = "Delete a hotel", description = "Removes a hotel from the system. Requires Admin or Hotel Owner privileges.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Hotel deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - User lacks required roles")
    })
    public ResponseEntity<Void> deleteHotel(
            @Parameter(description = "ID of the hotel to delete", required = true, example = "1")
            @PathVariable Long hotelId) {
        hotelService.deleteHotel(hotelId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{hotelId}/rooms")
    @Operation(summary = "Get all rooms by hotel", description = "Public endpoint to retrieve all physical rooms belonging to a specific hotel.")
    @ApiResponse(responseCode = "200", description = "List of rooms retrieved successfully")
    public ResponseEntity<List<RoomResponse>> getAllRooms(
            @Parameter(description = "ID of the hotel", required = true, example = "1")
            @PathVariable Long hotelId) {
        return ResponseEntity.ok(roomService.getAllRoomsByHotelId(hotelId));
    }

    @GetMapping("/{hotelId}/roomtypes")
    @Operation(summary = "Get all room types by hotel", description = "Public endpoint to retrieve all room categories (e.g., Deluxe, Suite) available at a specific hotel.")
    @ApiResponse(responseCode = "200", description = "List of room types retrieved successfully")
    public ResponseEntity<List<RoomTypeResDTO>> getByHotel(
            @Parameter(description = "ID of the hotel", required = true, example = "1")
            @PathVariable Long hotelId) {
        return ResponseEntity.ok(roomTypeService.getAllRoomTypesByHotel(hotelId));
    }

    @GetMapping("/search")
    @Operation(summary = "Search and filter hotels", description = "Public endpoint to search hotels dynamically by city, name, price range, and amenities with pagination and sorting.")
    @ApiResponse(responseCode = "200", description = "Search results retrieved successfully")
    public ResponseEntity<?> searchHotels(@ModelAttribute HotelSearchReqDTO req) {
        return ResponseEntity.ok(hotelService.searchHotel(req));
    }
}
