package hotelbooking.demo.controllers;

import hotelbooking.demo.domains.request.AmenityReqDTO;
import hotelbooking.demo.domains.response.AmenityResDTO;
import hotelbooking.demo.services.AmenityService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${hotelbooking.api-prefix}/amenities")
public class AmenityController {
    private final AmenityService amenityService;
    public AmenityController(AmenityService amenityService) {
        this.amenityService = amenityService;
    }
    @GetMapping
    public ResponseEntity<List<AmenityResDTO>> getAllAmenities() {
        return ResponseEntity.ok(amenityService.getAllAmenities());
    }
    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<AmenityResDTO> createAmenity(@Valid @RequestBody AmenityReqDTO req) {
        return ResponseEntity.status(201).body(amenityService.createAmenity(req));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<AmenityResDTO> updateAmenity(@PathVariable Long id, @Valid @RequestBody AmenityReqDTO req) {
        return ResponseEntity.ok(amenityService.updateAmenity(id, req));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteAmenity(@PathVariable Long id) {
        amenityService.deleteAmenity(id);
        return ResponseEntity.noContent().build();
    }
}
