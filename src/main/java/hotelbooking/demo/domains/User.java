package hotelbooking.demo.domains;

import hotelbooking.demo.utils.SecurityUtil;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@Getter
@Table(name = "users", indexes = {
        @Index(name = "idx_users_email_unique", columnList = "email", unique = true)
})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = true, length = 200)
    private String fullname;

    @Column(length = 11)
    private String phone;

    @Column(nullable = false)
    @Builder.Default
    private boolean isActive = false;

    @Builder.Default
    @Column(name = "is_two_factor_enabled", columnDefinition = "TINYINT(1)")
    private boolean isTwoFactorEnabled = false;

    private String twoFactorSecret;

    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;

    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;

    @OneToMany(mappedBy = "user",cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AuditLog> auditLogs = new ArrayList<>();

    @OneToMany
    private List<UserRole> userRoles = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Booking> bookings = new ArrayList<>();

    @PrePersist
    public void beforeCreate() {
        this.createdBy = SecurityUtil.getCurrentUserLogin().orElse("");
        this.createdAt = Instant.now();
    }

    @PreUpdate
    public void beforeUpdate() {
        this.updatedAt = Instant.now();
        this.updatedBy = SecurityUtil.getCurrentUserLogin().orElse("");
    }
}
