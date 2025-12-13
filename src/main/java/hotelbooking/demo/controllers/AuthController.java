package hotelbooking.demo.controllers;

import hotelbooking.demo.domains.User;
import hotelbooking.demo.domains.request.LoginDTO;
import hotelbooking.demo.domains.request.RegisterDTO;
import hotelbooking.demo.domains.request.ResLoginDTO;
import hotelbooking.demo.domains.response.UserDTO;
import hotelbooking.demo.services.UserService;
import hotelbooking.demo.utils.ApiMessage;
import hotelbooking.demo.utils.SecurityUtil;
import hotelbooking.demo.utils.exception.IdInvalidException;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final UserService userService;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final SecurityUtil securityUtil;

    public AuthController(UserService userService,
                          AuthenticationManagerBuilder authenticationManagerBuilder,
                          SecurityUtil securityUtil) {
        this.userService = userService;
        this.securityUtil = securityUtil;
        this.authenticationManagerBuilder = authenticationManagerBuilder;
    }

    @PostMapping("/register")
    @ApiMessage("Register Account")
    public ResponseEntity<UserDTO> register(@Valid @RequestBody RegisterDTO registerDTO) throws IdInvalidException {
        if(userService.getUserByEmail(registerDTO.getEmail())!=null){
            throw new IdInvalidException("User has been exists!");
        }
        UserDTO userDTO= userService.createUser(registerDTO);
        return ResponseEntity.ok().body(userDTO);
    }
    @PostMapping("/login")
    @ApiMessage("Login")
    public ResponseEntity<ResLoginDTO> login(@Valid @RequestBody LoginDTO loginDTO) throws IdInvalidException {

        User user = userService.getUserByEmail(loginDTO.getEmail());
        if (user == null) throw new IdInvalidException("User hasn't exists!");

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginDTO.getEmail(), loginDTO.getPassword());
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        var res = new ResLoginDTO();
        res.setUserLogin(new ResLoginDTO.UserLogin(user.getId(), user.getEmail(), user.getFullname(), user.getImageUrl()));

        String accessToken = this.securityUtil.createToken(authentication, res);
        res.setAccessToken(accessToken);

        return ResponseEntity.ok().body(res);
    }
}
