package hotelbooking.demo.controllers;


import hotelbooking.demo.domains.Hotel;
import hotelbooking.demo.domains.request.HotelRequest;
import hotelbooking.demo.domains.response.HotelResponse;
import hotelbooking.demo.services.HotelService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${hotelbooking.api-prefix}/hotel")
public class HotelController {
    private final HotelService hotelService;
    public HotelController(HotelService hotelService) {
        this.hotelService = hotelService;
    }

    @GetMapping
    public ResponseEntity<List<HotelResponse>> getAllHotels() {
        return ResponseEntity.ok(hotelService.getAllHotels());
    }
    @GetMapping("/{hotel-id}")
    public ResponseEntity<HotelResponse> getHotelById(@PathVariable Long hotelId) {
        return ResponseEntity.ok(hotelService.getHotelById(hotelId));
    }
    @PostMapping
    public ResponseEntity<HotelResponse> createHotel(@RequestBody HotelRequest hotelRq) {
        return ResponseEntity.ok(hotelService.createHotel(hotelRq));
    }
    @PutMapping("/{hotel-id}")
    public ResponseEntity<HotelResponse> updateHotel(@PathVariable Long hotelId,
                                                     @RequestBody HotelRequest hotelRq) {
        HotelResponse hotel = hotelService.getHotelById(hotelId);
        if (hotel == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(hotelService.updateHotel(hotelId, hotelRq));
    }
    @DeleteMapping("/{hotel-id}")
    public ResponseEntity<Void> deleteHotel(@PathVariable Long hotelId) {
        hotelService.deleteHotel(hotelId);
        return ResponseEntity.noContent().build();
    }
}
