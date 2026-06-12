package org.optipace.authmanager.controller;

import com.google.code.kaptcha.impl.DefaultKaptcha;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.optipace.authmanager.DTO.RequestDTO.ChangePasswordRequest;
import org.optipace.authmanager.DTO.RequestDTO.LoginRequest;
import org.optipace.authmanager.DTO.RequestDTO.RefreshTokenRequest;
import org.optipace.authmanager.DTO.ResponseDTO.BaseResponse;
import org.optipace.authmanager.DTO.ResponseDTO.TokenResponse;
import org.optipace.authmanager.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class loginController {

    private final DefaultKaptcha defaultKaptcha;
    private final AuthService authService;

    @GetMapping("/v1/captcha")
    public void getCaptcha(HttpServletRequest request,
                           HttpServletResponse response) throws Exception {
        log.info("Captcha request received");
        String captchaText = defaultKaptcha.createText();
        request.getSession().setAttribute("captcha", captchaText);
        BufferedImage image = defaultKaptcha.createImage(captchaText);
        response.setHeader("Cache-Control", "no-store, no-cache");
        response.setContentType("image/jpeg");
        log.info("Captcha added ");
        ImageIO.write(image, "jpg", response.getOutputStream());
    }


    @PostMapping("/v1/login")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest request,
 HttpSession session, HttpServletRequest httprequest) {
        return authService.login(request, session,httprequest);
    }

    @PostMapping("/v1/logout")
    public ResponseEntity<BaseResponse> logout( @RequestHeader("X-User-Id") String userId) {
        return   authService.logout(userId);
    }

    @PostMapping("/v1/refresh-token")
    public ResponseEntity<TokenResponse> updateRefershToken(@RequestBody RefreshTokenRequest request) {
        return authService.updateRefershToken(request);
    }

    @PostMapping("/v1/change-password")
    public ResponseEntity<BaseResponse> changePassword(
            @RequestBody ChangePasswordRequest request,
            @RequestHeader("X-User-Id") String userId) {

        return authService.changePassword(
                request,
                userId);
    }



}