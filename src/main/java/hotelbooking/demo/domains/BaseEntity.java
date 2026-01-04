package hotelbooking.demo.domains;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@MappedSuperclass // Đánh dấu đây là class cha, không tạo bảng riêng
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class) // Lắng nghe sự kiện save/update
public abstract class BaseEntity {

    @CreatedDate // Tự động điền ngày tạo
    @Column(updatable = false) // Không cho phép update trường này
    private Instant createdAt;

    @LastModifiedDate // Tự động cập nhật ngày sửa
    private Instant updatedAt;

    @CreatedBy // Tự động điền người tạo (Lấy từ AuditorAware ở bước 1)
    @Column(updatable = false)
    private String createdBy;

    @LastModifiedBy // Tự động cập nhật người sửa
    private String updatedBy;
}