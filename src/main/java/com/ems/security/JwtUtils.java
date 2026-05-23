package com.ems.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtils {

    // A secure 256-bit key encoded in Base64
    private final String jwtSecret = "Y2hvb25hLW15LXNlY3JldC1rZXktZm9yLWVtcGxveWVlLW1hbmFnZW1lbnQtc3lzdGVtLTMxMjM0NTY3ODkw";
    private final int jwtExpirationMs = 86400000; // 24 hours

    private final Key key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));

    public String generateJwtToken(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long employeeId = null;
        if (userPrincipal.getUser().getEmployee() != null) {
            employeeId = userPrincipal.getUser().getEmployee().getId();
        }
        return Jwts.builder()
                .setSubject(userPrincipal.getUsername())
                .claim("role", userPrincipal.getRole())
                .claim("userId", userPrincipal.getId())
                .claim("employeeId", employeeId)
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String getUsernameFromJwtToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token).getBody().getSubject();
    }

    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(authToken);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            // Token is invalid or expired
        }
        return false;
    }
}
