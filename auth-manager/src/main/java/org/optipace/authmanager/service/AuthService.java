package org.optipace.authmanager.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.optipace.authmanager.DTO.RequestDTO.ChangePasswordRequest;
import org.optipace.authmanager.DTO.RequestDTO.LoginRequest;
import org.optipace.authmanager.DTO.RequestDTO.RefreshTokenRequest;
import org.optipace.authmanager.DTO.ResponseDTO.BaseResponse;
import org.optipace.authmanager.DTO.ResponseDTO.TokenResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

public interface AuthService {
    public ResponseEntity<TokenResponse> login(LoginRequest request, HttpSession session, HttpServletRequest req) ;

    ResponseEntity<TokenResponse> updateRefershToken(RefreshTokenRequest request);

    ResponseEntity<BaseResponse> changePassword(ChangePasswordRequest request, String username);

    ResponseEntity<BaseResponse> logout(String userId);
}
