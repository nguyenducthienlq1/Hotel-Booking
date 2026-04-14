package hotelbooking.demo.services;

import hotelbooking.demo.domains.Amenity;
import hotelbooking.demo.domains.request.AmenityReqDTO;
import hotelbooking.demo.domains.response.AmenityResDTO;
import hotelbooking.demo.repositories.AmenityRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AmenityService {
    private final AmenityRepository amenityRepository;
    public AmenityService(AmenityRepository amenityRepository) {
        this.amenityRepository = amenityRepository;
    }
    public List<AmenityResDTO> getAllAmenities() {
        return amenityRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    public AmenityResDTO createAmenity(AmenityReqDTO req) {
        // Chuẩn hóa code: Viết hoa, bỏ khoảng trắng thừa
        String cleanCode = req.getCode().trim().toUpperCase();

        if (amenityRepository.existsByCode(cleanCode)) {
            throw new RuntimeException("Amenity code '" + cleanCode + "' already exists!");
        }

        Amenity amenity = Amenity.builder()
                .code(cleanCode)
                .name(req.getName())
                .description(req.getDescription())
                .build();

        return mapToDTO(amenityRepository.save(amenity));
    }
    public AmenityResDTO updateAmenity(Long id, AmenityReqDTO req) {
        Amenity amenity = amenityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Amenity not found"));

        amenity.setName(req.getName());
        amenity.setDescription(req.getDescription());

        return mapToDTO(amenityRepository.save(amenity));
    }
    public void deleteAmenity(Long id) {
        if (!amenityRepository.existsById(id)) {
            throw new RuntimeException("Amenity not found");
        }
        amenityRepository.deleteById(id);
    }

    private AmenityResDTO mapToDTO(Amenity amenity) {
        return AmenityResDTO.builder()
                .id(amenity.getId())
                .code(amenity.getCode())
                .name(amenity.getName())
                .description(amenity.getDescription())
                .build();
    }
}
