package hotelbooking.demo.domains;

import hotelbooking.demo.utils.SecurityUtil;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@Getter
@Table(name = "users", indexes = {
        @Index(name = "idx_users_email_unique", columnList = "email", unique = true)
})
public class User extends BaseEntity {

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

    @OneToMany(mappedBy = "user",cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AuditLog> auditLogs = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Booking> bookings = new ArrayList<>();

    @ManyToMany(fetch = FetchType.EAGER
            ,cascade = {CascadeType.DETACH,
            CascadeType.MERGE,
            CascadeType.PERSIST,
            CascadeType.REFRESH}) // EAGER để khi load User là lấy luôn quyền
    @JoinTable(
            name = "user_roles", // <--- Tên bảng trung gian trong DB (UserRole)
            joinColumns = @JoinColumn(name = "user_id"), // Khóa ngoại trỏ về bảng User
            inverseJoinColumns = @JoinColumn(name = "role_id") // Khóa ngoại trỏ về bảng Role
    )
    private Set<Role> roles = new HashSet<>();

}
