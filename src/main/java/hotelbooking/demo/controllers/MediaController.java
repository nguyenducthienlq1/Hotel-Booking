package hotelbooking.demo.controllers;

import hotelbooking.demo.domains.User;
import hotelbooking.demo.domains.response.FileDTO;
import hotelbooking.demo.services.CloudinaryService;
import hotelbooking.demo.services.UserService;
import hotelbooking.demo.utils.ApiMessage;
import hotelbooking.demo.utils.SecurityUtil;
import hotelbooking.demo.utils.exception.IdInvalidException;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@RequestMapping("${hotelbooking.api-prefix}/media")
@RestController
public class MediaController {
     private final CloudinaryService cloudinaryService;
     private final UserService userService;
     private final ExecutorService executor = Executors.newFixedThreadPool(5);
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

    @PostMapping("/upload-batch")
    public ResponseEntity<List<MediaResponse>> uploadBatch(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam(value = "folder", defaultValue = "hotel_general_media") String folder
    ) {


        List<CompletableFuture<MediaResponse>> futures = files.stream()
                .map(file -> CompletableFuture.supplyAsync(() -> {
                    try {
                        Map data = this.cloudinaryService.uploadFile(file, folder);
                        return new MediaResponse(
                                (String) data.get("secure_url"),
                                (String) data.get("resource_type"),
                                (String) data.get("format")
                        );
                    } catch (IOException e) {
                        throw new RuntimeException("Upload failed");
                    }
                }, executor))
                .collect(Collectors.toList());

        List<MediaResponse> uploadedFiles = futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
        return ResponseEntity.ok(uploadedFiles);
    }
    @Data
    @AllArgsConstructor
    public static class MediaResponse {
        private String url;
        private String resourceType; // image / video
        private String format;
    }

}

