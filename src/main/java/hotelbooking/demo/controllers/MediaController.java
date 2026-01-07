package hotelbooking.demo.controllers;

import hotelbooking.demo.domains.User;
import hotelbooking.demo.domains.request.ResLoginDTO;
import hotelbooking.demo.domains.response.FileDTO;
import hotelbooking.demo.services.CloudinaryService;
import hotelbooking.demo.services.UserService;
import hotelbooking.demo.utils.ApiMessage;
import hotelbooking.demo.utils.SecurityUtil;
import hotelbooking.demo.utils.exception.IdInvalidException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RequestMapping("${hotelbooking.api-prefix}/media")
@RestController
public class MediaController {
     private final CloudinaryService cloudinaryService;
     private final UserService userService;
     public MediaController(CloudinaryService cloudinaryService,
                            UserService userService) {
         this.cloudinaryService = cloudinaryService;
         this.userService = userService;
     }

    @PostMapping("/upload-avatar")
    @ApiMessage("Upload User Avatar")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<FileDTO> uploadAvatar(
            @RequestParam("file") MultipartFile file
    ) throws IdInvalidException, IOException {

        if (file.isEmpty()) {
            throw new IdInvalidException("File không được để trống!");
        }

        String email = SecurityUtil.getCurrentUserLogin().get();
        if (email.equals("")) {
            throw new IdInvalidException("Access Token không hợp lệ");
        }

        Map data = this.cloudinaryService.uploadFile(file, "Booking_hotel_avatar");

        String avatarUrl = (String) data.get("secure_url");

        User currentUser = this.userService.getUserByEmail(email);
        String oldAvatarUrl = currentUser.getImageUrl();

        // 5. Lưu URL vào Database
        boolean success = userService.updateAvatarUser(email, avatarUrl);


        if (success) {
            FileDTO fileDTO = FileDTO.builder()
                    .fileName("Image of: " + email)
                    .url(avatarUrl)
                    .build();
            if (oldAvatarUrl != null && !oldAvatarUrl.isEmpty()) {
                CompletableFuture.runAsync(() -> {
                    try {
                        String publicId = cloudinaryService.getPublicIdFromUrl(oldAvatarUrl);
                        this.cloudinaryService.deleteFile(publicId);
                        System.out.println("Đã xóa ảnh cũ trong background: " + publicId);
                    } catch (Exception e) {
                        System.out.println("Lỗi xóa ảnh background: " + e.getMessage());
                    }
                });
            }

            return ResponseEntity.ok(fileDTO);
        }
        else{
            throw new IdInvalidException("Update User Avatar Failed");
        }
    }
}
