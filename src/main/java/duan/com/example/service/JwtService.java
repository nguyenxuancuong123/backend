package duan.com.example.service;

import org.springframework.security.core.userdetails.UserDetails;

import java.util.Map;

public interface JwtService {

    String extracUserName(String token);

    String generateToken(UserDetails userDetails);

    boolean isTokenValid(String token, UserDetails userDetails);

    String generaRefreshToken(Map<String, Object> extracClaim, UserDetails userDetails);

    boolean isTokenBlacklisted(String token);
}
