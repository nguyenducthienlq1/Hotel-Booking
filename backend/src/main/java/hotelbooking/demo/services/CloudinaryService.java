package hotelbooking.demo.services;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {
    private Cloudinary cloudinary;
    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public Map uploadFile(MultipartFile file, String folderName) throws IOException {
        // upload(file_bytes, options_map)
        // folderName: Để gom nhóm ảnh (ví dụ: "avatar", "hotel-room")
        return cloudinary.uploader().upload(file.getBytes(),
                ObjectUtils.asMap(
                        "folder", folderName,
                        "resource_type", "auto"

                ));
    }
    public void deleteFile(String publicId) throws IOException {
        if (publicId != null && !publicId.isEmpty()) {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        }
    }
    public String getPublicIdFromUrl(String url) {
        if (url == null || url.isEmpty()) return null;
        try {
            int startIndex = url.lastIndexOf("upload/") + 7; // Bỏ qua đoạn 'upload/'
            String path = url.substring(startIndex); // Lấy từ v123456... trở đi

            int lastSlashIndex = path.lastIndexOf("/");
            int folderIndex = path.indexOf("/") + 1;

            String[] parts = url.split("/");
            String filenameWithExt = parts[parts.length - 1]; // filename.jpg
            String folder = parts[parts.length - 2]; // folder name
            String filename = filenameWithExt.substring(0, filenameWithExt.lastIndexOf("."));

            return folder + "/" + filename;
        } catch (Exception e) {
            return null;
        }
    }
}
