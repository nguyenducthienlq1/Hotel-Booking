package hotelbooking.demo.services;

import hotelbooking.demo.domains.User;
import hotelbooking.demo.domains.request.RegisterDTO;
import hotelbooking.demo.domains.response.UserDTO;
import hotelbooking.demo.repositories.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    public User save(User user) {
        return userRepository.save(user);
    }
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    @Transactional
    public User createUser(RegisterDTO loginDTO) {
        User user = User.builder()
                .email(loginDTO.getEmail())
                .fullname(loginDTO.getFullname())
                .phone(loginDTO.getPhone())
                .password(passwordEncoder.encode(loginDTO.getPassword()))
                .isActive(false)
                .build();
        this.userRepository.save(user);
//        UserDTO userDTO = UserDTO.builder()
//                .id(userCurrent.getId())
//                .fullname(userCurrent.getFullname())
//                .email(userCurrent.getEmail())
//                .imageUrl(userCurrent.getImageUrl())
//                .build();
        return user;
    }

    public boolean updateAvatarUser(String email, String imageUrl) {
        User user = getUserByEmail(email);
        if (user != null) {
            user.setImageUrl(imageUrl);
            save(user);
            return true;
        }
        return false;
    }
}
