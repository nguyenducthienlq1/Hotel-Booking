package hotelbooking.demo.controllers;

import hotelbooking.demo.domains.request.AmenityReqDTO;
import hotelbooking.demo.domains.response.AmenityResDTO;
import hotelbooking.demo.services.AmenityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${hotelbooking.api-prefix}/amenities")
@Tag(name = "Amenity Management", description = "Endpoints for managing global hotel amenities (e.g., WiFi, Pool, Gym). Requires Admin role for modifications.")
public class AmenityController {

    private final AmenityService amenityService;

    public AmenityController(AmenityService amenityService) {
        this.amenityService = amenityService;
    }

    @GetMapping
    @Operation(summary = "Get all amenities", description = "Public endpoint to retrieve a list of all master amenities available in the system.")
    @ApiResponse(responseCode = "200", description = "List of amenities retrieved successfully")
    public ResponseEntity<List<AmenityResDTO>> getAllAmenities() {
        return ResponseEntity.ok(amenityService.getAllAmenities());
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Create a new amenity", description = "Adds a new master amenity to the system. Requires Admin privileges.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Amenity created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input payload"),
            @ApiResponse(responseCode = "403", description = "Forbidden - User is not an Admin")
    })
    public ResponseEntity<AmenityResDTO> createAmenity(
            @Parameter(description = "Amenity details payload", required = true)
            @Valid @RequestBody AmenityReqDTO req) {
        return ResponseEntity.status(201).body(amenityService.createAmenity(req));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Update an amenity", description = "Updates details of an existing amenity. Requires Admin privileges.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Amenity updated successfully"),
            @ApiResponse(responseCode = "404", description = "Amenity not found"),
            @ApiResponse(responseCode = "403", description = "Forbidden - User is not an Admin")
    })
    public ResponseEntity<AmenityResDTO> updateAmenity(
            @Parameter(description = "ID of the amenity to update", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "Updated amenity payload", required = true)
            @Valid @RequestBody AmenityReqDTO req) {
        return ResponseEntity.ok(amenityService.updateAmenity(id, req));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Delete an amenity", description = "Removes an amenity from the system. Requires Admin privileges.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Amenity deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Amenity not found"),
            @ApiResponse(responseCode = "403", description = "Forbidden - User is not an Admin")
    })
    public ResponseEntity<Void> deleteAmenity(
            @Parameter(description = "ID of the amenity to delete", required = true, example = "1")
            @PathVariable Long id) {
        amenityService.deleteAmenity(id);
        return ResponseEntity.noContent().build();
    }
}