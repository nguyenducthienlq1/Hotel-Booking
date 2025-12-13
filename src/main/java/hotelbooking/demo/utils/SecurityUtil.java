package hotelbooking.demo.utils;

import com.nimbusds.jose.util.Base64;
import hotelbooking.demo.domains.request.ResLoginDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
public class SecurityUtil {

    private final JwtEncoder jwtEncoder;
    public SecurityUtil(final JwtEncoder jwtEncoder) {
        this.jwtEncoder = jwtEncoder;
    }

    public static final MacAlgorithm JWT_ALGORITHM = MacAlgorithm.HS512;

    @Value("${ducthien.jwt.base64-secret}")
    private String jwtKey;

    @Value("${ducthien.jwt.access-token-validity-in-seconds}")
    private long accessTokenExpiration;

    @Value("${ducthien.jwt.refresh-token-validity-in-seconds}")
    private String refreshTokenExpiration;

    private SecretKey getSecretKey() {
        byte[] keyBytes = Base64.from(jwtKey).decode();
        return new SecretKeySpec(keyBytes, 0, keyBytes.length, JWT_ALGORITHM.getName());
    }

    public String createToken(Authentication auth, ResLoginDTO dto){

        ResLoginDTO.UserInsideToken userInsideToken=new ResLoginDTO.UserInsideToken();
        userInsideToken.setId(dto.getUserLogin().getId());
        userInsideToken.setEmail(dto.getUserLogin().getEmail());
        userInsideToken.setName(dto.getUserLogin().getName());
        Instant now = Instant.now();
        Instant validity = now.plus(this.accessTokenExpiration, ChronoUnit.SECONDS);

        // @formatter:off
        List<String> listAuthority=new ArrayList<>();
        listAuthority.add("ROLE_USER_CREATE");
        listAuthority.add("ROLE_USER_UPDATE");
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuedAt(now)
                .expiresAt(validity)
                .subject(auth.getName())
                .claim("ducthien", listAuthority)
                .build();

        JwsHeader jwsHeader = JwsHeader.with(JWT_ALGORITHM).build();
        return this.jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).getTokenValue();

    }

}
