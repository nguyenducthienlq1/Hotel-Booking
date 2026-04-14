package hotelbooking.demo.controllers;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import hotelbooking.demo.domains.User;
import hotelbooking.demo.domains.request.LoginDTO;
import hotelbooking.demo.domains.request.RegisterDTO;
import hotelbooking.demo.domains.request.ResLoginDTO;
import hotelbooking.demo.domains.request.Verify2FADTO;
import hotelbooking.demo.domains.response.ResponseMessage;
import hotelbooking.demo.domains.response.ResponseRegister;
import hotelbooking.demo.domains.response.TwoFactorSetupDTO;
import hotelbooking.demo.services.*;
import hotelbooking.demo.utils.ApiMessage;
import hotelbooking.demo.utils.RequestUtil;
import hotelbooking.demo.utils.SecurityUtil;
import hotelbooking.demo.utils.exception.IdInvalidException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RestController
@RequestMapping("${hotelbooking.api-prefix}/auth")
@Tag(name = "Authentication", description = "Endpoints for user registration, login, 2FA management, and token refresh")public class AuthController {

    private final UserService userService;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final SecurityUtil securityUtil;
    private final LoginAttemptService loginAttemptService;
    private final EmailService emailService;
    private final BaseRedisService redisService;
    private final SessionService sessionService;
    private final JwtDecoder jwtDecoder;
    public AuthController(UserService userService,
                          AuthenticationManagerBuilder authenticationManagerBuilder,
                          SecurityUtil securityUtil,
                          LoginAttemptService loginAttemptService,
                          EmailService emailService,
                          BaseRedisService redisService,
                          SessionService sessionService,
                          JwtDecoder jwtDecoder) {
        this.userService = userService;
        this.jwtDecoder = jwtDecoder;
        this.securityUtil = securityUtil;
        this.authenticationManagerBuilder = authenticationManagerBuilder;
        this.loginAttemptService = loginAttemptService;
        this.emailService = emailService;
        this.redisService = redisService;
        this.sessionService = sessionService;
    }
    public static final String REFRESH_TOKEN_COOKIE_NAME = "refresh_token";
    public static final String REFRESH_TOKEN_ENDPOINT = "http://localhost:8080/api/v1/auth/refresh";

    @Value("${ducthien.jwt.refresh-token-validity-in-seconds}")
    private long refreshTokenExpiration;

    @PostMapping("/register")
    @ApiMessage("Register Account")
    @Operation(summary = "Register a new user", description = "Creates a new user account and sends a verification email to the provided address.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User registered successfully, verification email sent"),
            @ApiResponse(responseCode = "400", description = "Invalid input or email already exists")
    })
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
    @Operation(summary = "Verify user email", description = "Verifies the user's account using the token sent via email.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Email verified successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid or expired token")
    })
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
    @Operation(summary = "Authenticate user", description = "Logs in the user. Returns an Access Token or requests 2FA verification if enabled.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful or 2FA required"),
            @ApiResponse(responseCode = "400", description = "Account not activated or blocked"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    public ResponseEntity<ResLoginDTO> login(@Valid @RequestBody LoginDTO loginDTO,
                                             HttpServletRequest request) throws IdInvalidException {

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
            String ip = RequestUtil.clientIp(request);
            String ua = RequestUtil.userAgent(request);
            sessionService.createSession(user, refreshToken, ua, ip, refreshTokenExpiration);

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
    @PostMapping("/logout")
    @Operation(summary = "Logout user", description = "Blacklists the current access token and revokes the refresh token cookie.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Logged out successfully")
    })
    public ResponseEntity<Void> logout(@CookieValue(name = REFRESH_TOKEN_COOKIE_NAME, required = false) String rtCookieVal,
                                       @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String accessToken = authHeader.substring(7);
            try {
                Jwt jwt = jwtDecoder.decode(accessToken);
                long remainingSeconds = Duration.between(Instant.now(), jwt.getExpiresAt()).getSeconds();

                if (remainingSeconds > 0) {
                    redisService.blacklistToken(accessToken, remainingSeconds);
                }
            } catch (Exception e) {
            }
        }
        if (rtCookieVal != null && !rtCookieVal.isBlank()) {
            sessionService.revokeByRefreshToken(rtCookieVal);
        }
        ResponseCookie del = ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, "")
                .httpOnly(true).secure(true).sameSite("None")
                .path(REFRESH_TOKEN_ENDPOINT).maxAge(0)
                .build();
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, del.toString())
                .build();
    }
    @GetMapping("/refresh")
    @ApiMessage("Refresh Token")
    @Operation(summary = "Refresh access token", description = "Issues a new Access Token using a valid Refresh Token from cookies.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tokens refreshed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid or missing refresh token")
    })
    public ResponseEntity<ResLoginDTO> refreshToken(@CookieValue (name = REFRESH_TOKEN_COOKIE_NAME,
            required = false) String refreshTokenCookieVal) throws IdInvalidException {
        if (refreshTokenCookieVal == null || refreshTokenCookieVal.isBlank()){
            throw new IdInvalidException("Refresh token is empty!");
        }
        Jwt jwt = securityUtil.checkValidRefreshToken(refreshTokenCookieVal);
        String email = jwt.getSubject();
        User currentUser = this.userService.getUserByEmail(email);
        if (currentUser == null) {
            throw new IdInvalidException("Refresh Token không hợp lệ (User không tồn tại)");
        }
        var session = sessionService
                .findByRefreshToken(refreshTokenCookieVal)
                .orElseThrow(() -> new IdInvalidException("Refresh session not found"));
        if (session.getExpiresAt().isBefore(java.time.Instant.now())) {
            sessionService.revokeByRefreshToken(refreshTokenCookieVal);
            throw new IdInvalidException("Refresh token expired");
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
        sessionService.rotateRefreshToken(session, newRefreshToken, refreshTokenExpiration);

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

    @PostMapping("/2fa/verify-2fa")
    @ApiMessage("2FA Login")
    @Operation(summary = "Verify 2FA TOTP Code", description = "Validates the Google Authenticator code. Issues JWT tokens upon success.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "2FA verified successfully, tokens issued"),
            @ApiResponse(responseCode = "401", description = "Invalid 2FA code")
    })
    public ResponseEntity<?> verify2FA(@RequestBody Verify2FADTO request,
                                       HttpServletRequest httpRequest) {
        User user = userService.getUserByEmail(request.getEmail());

        boolean isValid = userService.validateCode(user.getTwoFactorSecret(), request.getCode());

        if (isValid) {
            if (!user.isTwoFactorEnabled()) {
                user.setTwoFactorEnabled(true);
                userService.save(user);
            }
            List<GrantedAuthority> authorities = user.getRoles().stream()
                    .map(role -> new SimpleGrantedAuthority(role.getName().name()))
                    .collect(Collectors.toList());
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    user.getEmail(),
                    null,
                    authorities
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            ResLoginDTO.UserLogin userLogin = new ResLoginDTO.UserLogin(
                    user.getId(),
                    user.getEmail(),
                    user.getFullname(),
                    user.getImageUrl()
            );
            ResLoginDTO dummyDtoForTokenGen = new ResLoginDTO();
            dummyDtoForTokenGen.setUserLogin(userLogin);
            //Tạo accessToken và refreshToken
            String accessToken = securityUtil.createToken(authentication, dummyDtoForTokenGen);
            String refreshToken = securityUtil.createRefreshToken(user.getEmail(), dummyDtoForTokenGen);

            //Tạo cookie
            ResponseCookie resCookies = ResponseCookie
                    .from(REFRESH_TOKEN_COOKIE_NAME, refreshToken)
                    .httpOnly(true)
                    .secure(true) // Nhớ đổi thành false nếu test local http
                    .path(REFRESH_TOKEN_ENDPOINT)
                    .maxAge(Duration.ofSeconds(refreshTokenExpiration))
                    .build();

            ResLoginDTO finalResponse = new ResLoginDTO();
            finalResponse.setAccessToken(accessToken);
            finalResponse.setUserLogin(userLogin);
            finalResponse.setMfaRequired(false);
            finalResponse.setMessage("Xác thực 2 lớp thành công");

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, resCookies.toString())
                    .body(finalResponse);
        } else {
            return ResponseEntity.status(401).body("Mã xác thực không đúng!");
        }
    }

    @GetMapping("/2fa/setup")
    @ApiMessage("Get QR Code for 2FA Setup")
    @Operation(summary = "Generate 2FA Setup QR Code", description = "Generates a TOTP secret and returns a QR code URL for apps like Google Authenticator.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "QR Code generated successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized user")
    })
    public ResponseEntity<TwoFactorSetupDTO> setup2FA() {

        String email = SecurityUtil.getCurrentUserLogin().orElseThrow(() -> new RuntimeException("Chưa đăng nhập"));
        User user = userService.getUserByEmail(email);

        String secret = userService.generateSecret();

        user.setTwoFactorSecret(secret);
        userService.save(user);

        String qrCodeUrl = userService.getQrCodeUrl(secret,user.getEmail());

        return ResponseEntity.ok(TwoFactorSetupDTO.builder()
                .secret(secret)
                .qrCodeUrl(qrCodeUrl)
                .build());
    }

}
