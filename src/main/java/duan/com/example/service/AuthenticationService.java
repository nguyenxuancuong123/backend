package duan.com.example.service;

import duan.com.example.dto.response.JwtAuthenticationResponse;
import duan.com.example.dto.request.RefreshTokenRequest;
import duan.com.example.dto.request.SigninRequest;
import duan.com.example.dto.request.SignUpRequest;
import duan.com.example.entity.Users;

import java.util.Map;

public interface AuthenticationService {

    Users Signup(SignUpRequest signUpRequest);

    JwtAuthenticationResponse Signin(SigninRequest signinRequest);

    JwtAuthenticationResponse refreshToken(RefreshTokenRequest refreshTokenRequest);

    void logout(String token);

    JwtAuthenticationResponse loginAsGuest();

    Map<String, Object> mergeGuest(String realUserEmail, String guestToken);
}