package hotelbooking.demo.controllers;

import hotelbooking.demo.domains.User;
import hotelbooking.demo.domains.request.LoginDTO;
import hotelbooking.demo.domains.response.UserDTO;
import hotelbooking.demo.services.UserService;
import hotelbooking.demo.utils.ApiMessage;
import hotelbooking.demo.utils.exception.IdInvalidException;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final UserService userService;
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    @ApiMessage("Register Account")
    public ResponseEntity<UserDTO> register(@Valid @RequestBody LoginDTO loginDTO) throws IdInvalidException {
        if(userService.getUserByEmail(loginDTO.getEmail())!=null){
            throw new IdInvalidException("User has been exists!");
        }
        UserDTO userDTO= userService.createUser(loginDTO);
        return ResponseEntity.ok().body(userDTO);
    }
}
