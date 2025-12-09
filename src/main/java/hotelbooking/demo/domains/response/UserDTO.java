package hotelbooking.demo.domains.response;

import lombok.*;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDTO {
    private long id;
    private String email;
    private String fullname;
    private String RoleName;
    private String imageUrl;
    private String phone;
    private Friend friend;
    private LastMessageDTO lastMessage;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Friend {
        private long sumUser;
        private List<UserDTO> friends;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class LastMessageDTO {
        private String content;
        private Instant createdAt;
        private Long senderId;

    }
}
