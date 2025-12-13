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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("${hotelbooking.api-prefix/auth")
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
    public static final String REFRESH_TOKEN_COOKIE_NAME = "refresh_token";
    public static final String REFRESH_TOKEN_ENDPOINT = "${hotelbooking.api-prefix/auth/refresh";

    @Value("${ducthien.jwt.refresh-token-validity-in-seconds}")
    private long refreshTokenExpiration;

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
    @ApiMessage("Login Account")
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

        String refreshToken = this.securityUtil.createRefreshToken(user.getEmail(), res);


        ResponseCookie resCookies = ResponseCookie
                .from(REFRESH_TOKEN_COOKIE_NAME, refreshToken)
                .httpOnly(true)
                .secure(true)
                .path(REFRESH_TOKEN_ENDPOINT)
                .maxAge(Duration.ofSeconds(refreshTokenExpiration))
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, resCookies.toString())
                .body(res);
    }

    public ResponseEntity<ResLoginDTO> refreshToken(@CookieValue (name = REFRESH_TOKEN_COOKIE_NAME, required = false) String refreshTokenCookieVal) throws IdInvalidException {
        if (refreshTokenCookieVal == null || refreshTokenCookieVal.isBlank()){
            throw new IdInvalidException("Refresh token is empty!");
        }
        Jwt jwt = securityUtil.checkValidRefreshToken(refreshTokenCookieVal);
        String email = jwt.getSubject();
        User currentUser = this.userService.getUserByEmail(email);
        if (currentUser == null) {
            throw new IdInvalidException("Refresh Token không hợp lệ (User không tồn tại)");
        }
        ResLoginDTO res = new ResLoginDTO();
        ResLoginDTO.UserLogin userLogin = new ResLoginDTO.UserLogin(
                currentUser.getId(),
                currentUser.getEmail(),
                currentUser.getFullname(),
                currentUser.getImageUrl());
        res.setUserLogin(userLogin);

        String newAccessToken = this.securityUtil.createRefreshToken(currentUser.getEmail(), res);
        res.setAccessToken(newAccessToken);
        String newRefreshToken = this.securityUtil.createRefreshToken(currentUser.getEmail(), res);

        ResponseCookie resCookies = ResponseCookie
                .from(REFRESH_TOKEN_COOKIE_NAME, newRefreshToken)
                .httpOnly(true)
                .secure(true)
                .path(REFRESH_TOKEN_ENDPOINT)
                .maxAge(Duration.ofSeconds(refreshTokenExpiration))
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, resCookies.toString())
                .body(res);
    }
}
