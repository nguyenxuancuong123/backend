package duan.com.example.service.impl;

import duan.com.example.entity.Users;
import duan.com.example.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;

@Service
public class JwtServiceImpl implements JwtService {

    @Value("${jwt.secret-key:394850752C294B4428472B4B6250655368566B5970337336763979034982059A}")
    private String secretKey;

    // 1. Đã bổ sung Claims chứa Role cho Frontend giải mã
    @Override
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();

        // Lưu danh sách GrantedAuthorities (VD: ["ROLE_ADMIN"])
        claims.put("roles", userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));

        // Lưu tên vai trò trực tiếp từ Entity Users (VD: "ADMIN", "EMPLOYEE", "USER")
        if (userDetails instanceof Users) {
            Users users = (Users) userDetails;
            if (users.getVaiTro() != null) {
                claims.put("role", users.getVaiTro().name());
            }
        }

        return Jwts.builder()
                .claims(claims) // Nạp role vào token payload
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000)) // 1 ngày
                .signWith(getSiginKey())
                .compact();
    }

    // 2. Cập nhật Refresh Token theo chuẩn JJWT mới nhất
    @Override
    public String generaRefreshToken(Map<String, Object> extracClaim, UserDetails userDetails) {
        return Jwts.builder()
                .claims(extracClaim)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 604800000L))
                .signWith(getSiginKey())
                .compact();
    }

    @Override
    public boolean isTokenBlacklisted(String token) {
        return false;
    }

    @Override
    public String extracUserName(String token) {
        return extracClaim(token, Claims::getSubject);
    }

    private <T> T extracClaim(String token, Function<Claims, T> claimsResolvers) {
        final Claims claims = extracAllClaims(token);
        return claimsResolvers.apply(claims);
    }

    private SecretKey getSiginKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private Claims extracAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSiginKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    @Override
    public boolean isTokenValid(String token, UserDetails userDetails){
        final String username = extracUserName(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        return extracClaim(token, Claims::getExpiration).before(new Date());
    }
}