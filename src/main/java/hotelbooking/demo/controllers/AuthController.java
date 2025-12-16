package hotelbooking.demo.controllers;

import hotelbooking.demo.domains.User;
import hotelbooking.demo.domains.request.LoginDTO;
import hotelbooking.demo.domains.request.RegisterDTO;
import hotelbooking.demo.domains.request.ResLoginDTO;
import hotelbooking.demo.domains.request.Verify2FADTO;
import hotelbooking.demo.domains.response.ResponseMessage;
import hotelbooking.demo.domains.response.ResponseRegister;
import hotelbooking.demo.services.BaseRedisService;
import hotelbooking.demo.services.EmailService;
import hotelbooking.demo.services.LoginAttemptService;
import hotelbooking.demo.services.UserService;
import hotelbooking.demo.utils.ApiMessage;
import hotelbooking.demo.utils.SecurityUtil;
import hotelbooking.demo.utils.exception.IdInvalidException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("${hotelbooking.api-prefix}/auth")
public class AuthController {

    private final UserService userService;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final SecurityUtil securityUtil;
    private final LoginAttemptService loginAttemptService;
    private final EmailService emailService;
    private final BaseRedisService redisService;
    public AuthController(UserService userService,
                          AuthenticationManagerBuilder authenticationManagerBuilder,
                          SecurityUtil securityUtil,
                          LoginAttemptService loginAttemptService,
                          EmailService emailService,
                          BaseRedisService redisService) {
        this.userService = userService;
        this.securityUtil = securityUtil;
        this.authenticationManagerBuilder = authenticationManagerBuilder;
        this.loginAttemptService = loginAttemptService;
        this.emailService = emailService;
        this.redisService = redisService;
    }
    public static final String REFRESH_TOKEN_COOKIE_NAME = "refresh_token";
    public static final String REFRESH_TOKEN_ENDPOINT = "http://localhost:8080/api/v1/auth/refresh";

    @Value("${ducthien.jwt.refresh-token-validity-in-seconds}")
    private long refreshTokenExpiration;

    @PostMapping("/register")
    @ApiMessage("Register Account")
    public ResponseEntity<ResponseRegister> register(@Valid @RequestBody RegisterDTO registerDTO) throws IdInvalidException {
        if(userService.getUserByEmail(registerDTO.getEmail())!=null){
            throw new IdInvalidException("User has been exists!");
        }
        User user= userService.createUser(registerDTO);
        String token = UUID.randomUUID().toString();
        redisService.set(token, user.getEmail(),24, TimeUnit.HOURS);
        emailService.sendVerifyEmail("nguyenducthienlq1@gmail.com", user.getEmail(), token);
        ResponseRegister res = ResponseRegister.builder()
                .message("Đăng ký thành công, hãy kiểm tra Email để kích hoạt tài khoản")
                .token(token)
                .build();
        return ResponseEntity.ok().body(res);
    }

    @GetMapping("/verify")
    @ApiMessage("Verify Email")
    public ResponseEntity<ResponseMessage> verifyEmail(@RequestParam("token") String token) {
        ResponseMessage res = new ResponseMessage();
        if (!redisService.hasKey(token)) {
            res.setMessage("Không thể xác thực email hoặc token đã hết hạn");
            return ResponseEntity.badRequest().body(res);
        }
        String email = (String) redisService.get(token);
        User user = userService.getUserByEmail(email);
        if (user != null) {
            user.setActive(true);
            userService.save(user);
            redisService.delete(token);
            res.setMessage("Xác thực thành công! Bạn có thể đăng nhập ngay bây giờ.");
            return ResponseEntity.ok(res);
        }
        res.setMessage("Lỗi xác thực: Không tìm thấy người dùng.");
        return ResponseEntity.badRequest().body(res);
    }

    @PostMapping("/login")
    @ApiMessage("Login Account")
    public ResponseEntity<ResLoginDTO> login(@Valid @RequestBody LoginDTO loginDTO) throws IdInvalidException {

        if (loginAttemptService.isBlocked(loginDTO.getEmail())) {
            long seconds = loginAttemptService.getTimeRemaining(loginDTO.getEmail());
            throw new IdInvalidException("Tài khoản tạm thời bị khóa do nhập sai quá 5 lần. Vui lòng quay lại sau " + seconds + " giây.");
        }

        try {
            User user = userService.getUserByEmail(loginDTO.getEmail());
            // Kiểm tra tài khoản tồn tại
            if (user == null) {
                loginAttemptService.loginFailed(loginDTO.getEmail());
                throw new IdInvalidException("Tài khoản không tồn tại!");
            }
            // Kiểm tra tài khoản kích hoạt
            if (!user.isActive()){
                throw new IdInvalidException("Tài khoản chưa được kích hoạt. Vui lòng kiểm tra email!");
            }

            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(loginDTO.getEmail(), loginDTO.getPassword());
            Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);


            loginAttemptService.loginSucceeded(loginDTO.getEmail());
            SecurityContextHolder.getContext().setAuthentication(authentication);

            var res = new ResLoginDTO();

            if (user.isTwoFactorEnabled()){
                res.setMfaRequired(true);
                res.setMessage("Tài khoản đang được bảo mật 2 lớp, vui lòng nhập mã otp");
                return ResponseEntity.ok(res);
            }

            res.setUserLogin(new ResLoginDTO.UserLogin(user.getId(), user.getEmail(), user.getFullname(), user.getImageUrl()));

            String accessToken = this.securityUtil.createToken(authentication, res);
            res.setAccessToken(accessToken);
            res.setMfaRequired(false);


            String refreshToken = this.securityUtil.createRefreshToken(user.getEmail(), res);

            ResponseCookie resCookies = ResponseCookie
                    .from(REFRESH_TOKEN_COOKIE_NAME, refreshToken)
                    .httpOnly(true)
                    .secure(true) // Nhớ đổi thành false nếu test local http
                    .path(REFRESH_TOKEN_ENDPOINT)
                    .maxAge(Duration.ofSeconds(refreshTokenExpiration))
                    .build();

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, resCookies.toString())
                    .body(res);

        } catch (BadCredentialsException e) {
            loginAttemptService.loginFailed(loginDTO.getEmail());
            throw new IdInvalidException("Email hoặc mật khẩu không chính xác!");
        }
    }
    @GetMapping("/refresh")
    @ApiMessage("Refresh Token")
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

    @PostMapping("/verify-2fa")
    @ApiMessage("2FA Login")
    public ResponseEntity<?> verify2FA(@RequestBody Verify2FADTO request) {
        User user = userService.getUserByEmail(request.getEmail());

        boolean isValid = userService.validateCode(user.getTwoFactorSecret(), request.getCode());

        if (isValid) {
            String accessToken = securityUtil.createToken(user);
            return ResponseEntity.ok(new LoginResponse("SUCCESS", accessToken));
        } else {
            return ResponseEntity.status(401).body("Mã xác thực không đúng!");
        }
    }
}
