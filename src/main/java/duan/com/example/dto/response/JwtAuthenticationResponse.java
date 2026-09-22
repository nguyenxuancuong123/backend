package duan.com.example.dto.response;

import lombok.Data;

@Data
public class JwtAuthenticationResponse {

    private String token;

    private String refreshToken;

    private String hoTen;

    private Boolean isGuest;
}
