package hotelbooking.demo.domains;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

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
    private long id;

    @Column(nullable = false, length = 200)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 200)
    private String fullname;

    @Column(nullable = false, length = 11)
    private String phone;

    private boolean isActive;

    @Column(name = "image_url", columnDefinition = "TEXT")
    private String urlAvatar;

    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
}
