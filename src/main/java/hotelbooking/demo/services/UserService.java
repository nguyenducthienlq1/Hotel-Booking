package hotelbooking.demo.services;

import hotelbooking.demo.domains.User;
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
    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional
    public UserDTO CreateUser(User user) {
        user.setPassword(this.passwordEncoder.encode(user.getPassword()));
        User UserDTO= this.userRepository.save(user);
        UserDTO userDTO = new UserDTO();
        userDTO.setId(UserDTO.getId());
        userDTO.setEmail(UserDTO.getEmail());
        userDTO.setFullname(UserDTO.getFullname());
        userDTO.setImageUrl(UserDTO.getImageUrl());
        return userDTO;
    }
}
