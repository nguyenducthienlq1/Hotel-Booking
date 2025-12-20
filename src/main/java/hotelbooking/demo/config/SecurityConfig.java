package hotelbooking.demo.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.util.Base64;
import hotelbooking.demo.services.BaseRedisService;
import hotelbooking.demo.utils.SecurityUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.oauth2.server.resource.web.access.BearerTokenAccessDeniedHandler;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

@Configuration
@EnableMethodSecurity(securedEnabled = true)
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
public class SecurityConfig {

    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final BaseRedisService baseRedisService;
    public SecurityConfig(CustomAuthenticationEntryPoint customAuthenticationEntryPoint,
                          BaseRedisService baseRedisService) {
        this.customAuthenticationEntryPoint = customAuthenticationEntryPoint;
        this.baseRedisService = baseRedisService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Value("${ducthien.jwt.base64-secret}")
    private String jwtKey;

    private SecretKey getSecretKey(){
        byte[] keyBytes = Base64.from(jwtKey).decode();
        return new SecretKeySpec(keyBytes, 0, keyBytes.length, SecurityUtil.JWT_ALGORITHM.getName());
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new
                JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthorityPrefix("");
        //set quyen han co ten la permission vao context
        grantedAuthoritiesConverter.setAuthoritiesClaimName("permission");

        JwtAuthenticationConverter jwtAuthenticationConverter = new
                JwtAuthenticationConverter();

        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        return jwtAuthenticationConverter;
    }
    @Bean
    public JwtDecoder jwtDecoder(){
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withSecretKey(getSecretKey())
                .macAlgorithm(SecurityUtil.JWT_ALGORITHM).build();
        return token -> {
            try {
                if (baseRedisService.isTokenBlacklisted(token)){
                    throw new BadCredentialsException("Token is blacklisted");
                }else{
                    return jwtDecoder.decode(token);
                }
            } catch(Exception e){
                System.out.println(">>> JWT error: " + e.getMessage());
                throw e;
            }
        };
    }

    @Bean
    public JwtEncoder jwtEncoder() {
        return new NimbusJwtEncoder(new ImmutableSecret<>(getSecretKey()));
    }

    @Bean
    public SecurityFilterChain configure(HttpSecurity http) throws Exception {
        http.
                csrf(c->c.disable())
                .authorizeHttpRequests(
                        authz -> authz
                                .requestMatchers("/api/v1/auth/register",
                                        "/api/v1/auth/login",
                                        "/api/v1/auth/refresh",
                                        "/api/v1/auth/verify").permitAll()
                                .anyRequest().authenticated())
                .oauth2ResourceServer((oauth2 -> oauth2.jwt(Customizer.withDefaults())
                        .authenticationEntryPoint(this.customAuthenticationEntryPoint)))
                .exceptionHandling(
                        exceptions -> exceptions
                                .authenticationEntryPoint(new BearerTokenAuthenticationEntryPoint())//401
                                .accessDeniedHandler(new BearerTokenAccessDeniedHandler()) //403
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }
}
