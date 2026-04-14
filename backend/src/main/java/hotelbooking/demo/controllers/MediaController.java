package hotelbooking.demo.controllers;

import hotelbooking.demo.domains.User;
import hotelbooking.demo.domains.response.FileDTO;
import hotelbooking.demo.services.CloudinaryService;
import hotelbooking.demo.services.UserService;
import hotelbooking.demo.utils.ApiMessage;
import hotelbooking.demo.utils.SecurityUtil;
import hotelbooking.demo.utils.exception.IdInvalidException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@RequestMapping("${hotelbooking.api-prefix}/media")
@RestController
@Tag(name = "Media Management", description = "Endpoints for uploading and managing media files (Images, Videos) via Cloudinary.")
public class MediaController {

    private final CloudinaryService cloudinaryService;
    private final UserService userService;
    private final ExecutorService executor = Executors.newFixedThreadPool(5);

    public MediaController(CloudinaryService cloudinaryService,
                           UserService userService) {
        this.cloudinaryService = cloudinaryService;
        this.userService = userService;
    }

    @PostMapping(value = "/upload-avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ApiMessage("Upload User Avatar")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "Upload user avatar", description = "Uploads a new profile picture for the currently authenticated user. The old avatar will be deleted automatically in the background. Requires USER role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Avatar uploaded and updated successfully"),
            @ApiResponse(responseCode = "400", description = "File is empty or invalid format"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid Access Token")
    })
    public ResponseEntity<FileDTO> uploadAvatar(
            @Parameter(description = "Image file to upload", required = true)
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

        // Lưu URL vào Database
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

    @PostMapping(value = "/upload-batch", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Batch upload media files", description = "Uploads multiple files (images or videos) concurrently to a specified Cloudinary folder.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "All files uploaded successfully"),
            @ApiResponse(responseCode = "500", description = "Error occurred during file upload")
    })
    public ResponseEntity<List<MediaResponse>> uploadBatch(
            @Parameter(description = "List of media files to upload", required = true)
            @RequestParam("files") List<MultipartFile> files,
            @Parameter(description = "Target folder in Cloudinary", example = "hotel_general_media")
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
    @Schema(description = "Response object containing uploaded media details")
    public static class MediaResponse {

        @Schema(description = "Secure URL of the uploaded file", example = "https://res.cloudinary.com/demo/image/upload/v1234567/sample.jpg")
        private String url;

        @Schema(description = "Type of the resource", example = "image")
        private String resourceType; // image / video

        @Schema(description = "Format of the file", example = "jpg")
        private String format;
    }

}